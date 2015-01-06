/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map

import grails.util.Holders as HOLDER

import javax.servlet.ServletContext

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import grails.converters.JSON
import grails.converters.XML

import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.servlet.http.HttpServletResponse

import net.nosegrind.apitoolkit.*

class ApiToolkitFilters {
	
	ApiRequestService apiRequestService
	ApiResponseService apiResponseService
	
	ApiDomainService apiDomainService
	
	GrailsApplication grailsApplication
	ApiCacheService apiCacheService

	def filters = {
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String apinameEntrypoint = "{apiName}_v${apiVersion}"
		String versionEntrypoint = "v${apiVersion}"
		String entryPoint = (apiName)?apinameEntrypoint:versionEntrypoint
		
		boolean chain = grailsApplication.config.apitoolkit.chaining.enabled
		apiRequestService.setChain(chain)
		boolean batch = grailsApplication.config.apitoolkit.batching.enabled
		apiRequestService.setBatch(batch)

		//String apiRegex = "/${entryPoint}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**".toString()
		
		//apitoolkit(regex:apiRegex){
		apitoolkit(uri:"/${entryPoint}*/**"){
			before = {
				println("##### FILTER (BEFORE)")
				
				/*
				 * FIRST DETERMINE
				 *  - HOW ENDPOINT IS BEING CALLED, THEN...
				 *  - WHAT RESOURCE IS BEING CALLED (CONTROLLER/SERVICE/DOMAIN/ETC)
				 *  - FINALLY, RESOLVE ENDPOINT
				 */
				
				def methods = ['get':'show','put':'update','post':'create','delete':'delete']
				try{
					
					if(request.class.toString().contains('SecurityContextHolderAwareRequestWrapper')){
						def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]
						if(cache){
							params.apiObject = (params.apiObjectVersion)?params.apiObjectVersion:cache['currentStable']['value']
							if(!params.action){ 
								String methodAction = methods[request.method.toLowerCase()]
								if(!cache[params.apiObject][methodAction]){
									params.action = cache[params.apiObject]['defaultAction'].split('/')[1] 
								}else{
									params.action = methods[request.method.toLowerCase()]
									
									// FORWARD FOR REST DEFAULTS WITH NO ACTION
									def tempUri = request.getRequestURI().split("/")
									if(tempUri[2].contains('dispatch')){
										if("${params.controller}.dispatch" == tempUri[2]){
											if(!cache[params.apiObject]['domainPackage']){
												forward(controller:params.controller,action:params.action,params:params)
												return false
											}
										}
									}
								}
							}
							
							// SET PARAMS AND TEST ENDPOINT ACCESS (PER APIOBJECT)
							boolean result = apiRequestService.handleApiRequest(cache,request,params,entryPoint)
							//HANDLE DOMAIN RESOLUTION
							if(cache[params.apiObject]['domainPackage']){
								println("domain package")
								// SET PARAMS AND TEST ENDPOINT ACCESS (PER APIOBJECT)
								if(result){
									def model
									switch(methods[request.method.toLowerCase()]){
										case 'show':
											model = apiDomainService.showInstance(cache,params)
											break
										case 'update':
										println("### update")
											model = apiDomainService.updateInstance(cache,params)
											break
										case 'create':
										println("### create")
											model = apiDomainService.createInstance(cache,params)
											break
										case 'delete':
											model = apiDomainService.deleteInstance(cache,params)
											break
									}


									if(!model){
										render(status:HttpServletResponse.SC_BAD_REQUEST)
										return false
									}
									
									if(params?.apiCombine==true){
										model = params.apiCombine
									}
									println("model = "+model)
									def newModel = apiResponseService.formatDomainObject(model)
									
									LinkedHashMap content
									if(chain && params?.apiChain?.order){
										//if(!['null','return'].contains(params?.apiChain?.order["${keys.last()}"].split(':'))){
										boolean result2 = apiResponseService.handleApiChain(cache, request,response ,newModel,params)
										List uriVars = apiResponseService.parseUri(request.forwardURI,entryPoint)
										if(uriVars.size()>2){
											params.apiObject = uriVars[0]
											uriVars.drop(1)
										}
										
										forward(controller:uriVars[0],action:uriVars[1],id:params.id)
										return false
										//}
									}else if(batch && params?.apiBatch){
										forward(controller:params.controller,action:params.action,params:params)
										return false
									}else{
										content = apiResponseService.handleApiResponse(cache,request,response,newModel,params)
									}

									render(text:content.apiToolkitContent, contentType:"${content.apiToolkitType}", encoding:content.apiToolkitEncoding)
									return false
								}
								//return result
							}else{
								return result
							}
						}
					}
					
					return false

				}catch(Exception e){
					log.error("[ApiToolkitFilters :: preHandler] : Exception - full stack trace follows:", e);
					return false
				}
			}
			
			after = { Map model ->
				println("##### FILTER (AFTER)")
				println(model)
				try{
					if(!model){
						render(status:HttpServletResponse.SC_BAD_REQUEST)
						return false
					}

					if(params?.apiCombine==true){
						model = params.apiCombine
					}
					
					def newModel = (model)?apiResponseService.convertModel(model):model
					def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]

					//println(response.response.getResponse().response)
					LinkedHashMap content
					
					if(chain && params?.apiChain?.order){
						//if(!['null','return'].contains(params?.apiChain?.order["${keys.last()}"].split(':'))){
						boolean result = apiResponseService.handleApiChain(cache, request,response ,newModel,params)
						List uriVars = apiResponseService.parseUri(request.forwardURI,entryPoint)
						if(uriVars.size()>2){
							params.apiObject = uriVars[0]
							uriVars.drop(1)
						}
						
						forward(controller:uriVars[0],action:uriVars[1],id:params.id)
						return false
						//}
					}else if(batch && params?.apiBatch){
						forward(controller:params.controller,action:params.action,params:params)
						return false
					}else{
						content = apiResponseService.handleApiResponse(cache,request,response,newModel,params)
					}
						
					if(content){
						render(text:content.apiToolkitContent, contentType:"${content.apiToolkitType}", encoding:content.apiToolkitEncoding)
						return false
					}
					return null
			   }catch(Exception e){
				   log.error("[ApiToolkitFilters :: apitoolkit.after] : Exception - full stack trace follows:", e);
				   return false
			   }
			}
		}
	}
}
