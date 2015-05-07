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

//import grails.util.Holders as HOLDER

import javax.servlet.ServletContext

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes


import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.web.util.WebUtils

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
		
		//boolean chain = grailsApplication.config.apitoolkit.chaining.enabled
		//apiRequestService.chain
		//boolean batch = grailsApplication.config.apitoolkit.batching.enabled
		//apiRequestService.batch)


		apierrors(controller:"errors"){
			before = {
				//println("##### FILTER (ERRORS)")
				render(status: request.'javax.servlet.error.status_code')
				return false
			}
		}

		
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
						println('has request')
						def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]
println(params.controller)
println(cache)
println(apiCacheService.getCacheNames())
						if(cache){
println('has cache')
							params.apiObject = (params.apiObjectVersion)?params.apiObjectVersion:cache['currentStable']['value']
							if(!params.action){ 
println('NO action')
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
								// SET PARAMS AND TEST ENDPOINT ACCESS (PER APIOBJECT)
								if(result){
									def model
									switch(methods[request.method.toLowerCase()]){
										case 'show':
											model = apiDomainService.showInstance(cache,params)
											break
										case 'update':
											model = apiDomainService.updateInstance(cache,params)
											break
										case 'create':
											model = apiDomainService.createInstance(cache,params)
											break
										case 'delete':
											model = apiDomainService.deleteInstance(cache,params)
											model = [:]
											break
									}


									if(!model && request.method.toLowerCase()!='delete'){
										render(status:HttpServletResponse.SC_BAD_REQUEST)
										return false
									}
									
									if(params?.apiCombine==true){
										model = params.apiCombine
									}
									def newModel = apiResponseService.formatDomainObject(model)
									
									LinkedHashMap content
									if(apiRequestService.chain && params?.apiChain?.order){
										boolean result2 = apiResponseService.handleApiChain(cache, request,response ,newModel,params)
										forward(controller:params.controller,action:params.action,id:params.id)
										return false
									}else if(apiRequestService.batch && params?.apiBatch){
										forward(controller:params.controller,action:params.action,params:params)
										return false
									}else{
										content = apiResponseService.handleApiResponse(cache,request,response,newModel,params)
									}
									
									if(request.method.toLowerCase()=='delete' && content.apiToolkitContent==null){
										render(status:HttpServletResponse.SC_OK)
										return false
									}else{
										render(text:content.apiToolkitContent, contentType:"${content.apiToolkitType}", encoding:content.apiToolkitEncoding)
										return false
									}
								}
								//return result
							}else{
								println('returning result')
								return result
							}
						}
					}
					
					return false

				}catch(Exception e){
					//log.error("[ApiToolkitFilters :: preHandler] : Exception - full stack trace follows:", e);
					println("[ApiToolkitFilters :: preHandler] : Exception - full stack trace follows:"+e);
					return false
				}
			}
			
			after = { Map model ->
				println("##### FILTER (AFTER)")
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

					LinkedHashMap content
					
					if(apiResponseService.chain && params?.apiChain?.order){
						boolean result = apiResponseService.handleApiChain(cache, request,response,newModel,params)
						forward(controller:params.controller,action:params.action,id:params.id)
						return false
					}else if(apiResponseService.batch && params?.apiBatch){
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
