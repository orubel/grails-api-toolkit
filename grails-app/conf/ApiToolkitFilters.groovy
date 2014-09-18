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

	GrailsApplication grailsApplication
	ApiCacheService apiCacheService
	
	def filters = {
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String entryPoint = (apiName)?"${apiName}_v${apiVersion}":"v${apiVersion}"
		
		boolean chain = grailsApplication.config.apitoolkit.chaining.enabled
		apiRequestService.setChain(chain)
		boolean batch = grailsApplication.config.apitoolkit.batching.enabled
		apiRequestService.setBatch(batch)

		//String apiRegex = "/${apiRoot}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**".toString()
		
		//apitoolkit(regex:apiRegex){
		apitoolkit(uri:"/${entryPoint}*/**"){
			before = {
				//println("##### FILTER (BEFORE)")
				try{
					if(!request.class.toString().contains('SecurityContextHolderAwareRequestWrapper')){
						return false
					}

					def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]
					if(cache){

						params.apiObject = (params.apiObjectVersion)?params.apiObjectVersion:cache['currentStable']['value']
						
						if(!params.action){
							params.action = cache[params.apiObject]['defaultAction']
						}
						
						
						boolean result = apiRequestService.handleApiRequest(cache,request,params,entryPoint)
						return result
					}
					
					return false

				}catch(Exception e){
					log.error("[ApiToolkitFilters :: preHandler] : Exception - full stack trace follows:", e);
					return false
				}
			}
			
			after = { Map model ->
				//println("##### FILTER (AFTER)")
				try{
					if(!model){
						render(status:HttpServletResponse.SC_BAD_REQUEST)
						return null
					}
					
					if(params?.apiCombine==true){
						model = params.apiCombine
					}
					
					def newModel = (model)?apiResponseService.convertModel(model):model
					def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]

					//println(response.response.getResponse().response)
					LinkedHashMap map
					
					if(chain && params?.apiChain?.order){
						//if(!['null','return'].contains(params?.apiChain?.order["${keys.last()}"].split(':'))){
						boolean result = apiResponseService.handleApiChain(cache, request,response ,newModel,params)
						forward(controller:"${params.controller}",action:"${params.action}",id:"${params.id}")
						return false
						//}
					}else if(batch && params?.apiBatch){
						forward(controller:"${params.controller}",action:"${params.action}",params:params)
						return false
					}else{
						map = apiResponseService.handleApiResponse(cache,request,response,newModel,params)
					}
						
					if(map){
						String apiEncoding = (params.contentType)?params.contentType:"UTF-8"
						switch(request.method) {
							case 'PURGE':
								// cleans cache
								break;
							case 'TRACE':
								break;
							case 'HEAD':
								break;
							case 'OPTIONS':
								LinkedHashMap doc = apiResponseService.getApiDoc(params)
								switch(params.contentType){
									case 'text/xml':
									case 'application/xml':
										render(text:doc as XML, contentType: 'application/xml')
										return false
										break
									case 'text/json':
									case 'application/json':
									default:
										render(text:doc as JSON, contentType: 'application/json')
										return false
										break
								}
								return false
								break;
							case 'GET':
								if(map?.isEmpty()==false){
									switch(params.contentType){
										case 'text/xml':
										case 'application/xml':
											if(params.encoding){
												render(text:map as XML, contentType: 'application/xml',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as XML, contentType: 'application/xml')
												return false
											}
											break
										case 'text/html':
											break
										case 'text/json':
										case 'application/json':
										default:
											if(params.encoding){
												render(text:map as JSON, contentType: 'application/json',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as JSON, contentType: 'application/json')
												return false
											}
											break
									}
									return false
								}
								break
							case 'POST':
								if(!map.isEmpty()){
									switch(params.contentType){
										case 'text/xml':
										case 'application/xml':
											if(params.encoding){
												render(text:map as XML, contentType: 'application/xml',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as XML, contentType: 'application/xml')
												return false
											}
											break
										case 'text/json':
										case 'application/json':
										default:
											if(params.encoding){
												render(text:map as JSON, contentType: 'application/json',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as JSON, contentType: 'application/json')
												return false
											}
											break
									}
									return null
								}
								break
							case 'PUT':
								if(!map.isEmpty()){
									switch(params.contentType){
										case 'text/xml':
										case 'application/xml':
											if(params.encoding){
												render(text:map as XML, contentType: 'application/xml',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as XML, contentType: 'application/xml')
												return false
											}
											break
										case 'text/json':
										case 'application/json':
										default:
											if(params.encoding){
												render(text:map as JSON, contentType: 'application/json',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as JSON, contentType: 'application/json')
												return false
											}
											break
									}
									return false
								}
								break
							case 'DELETE':
								if(!map.isEmpty()){
									switch(params.contentType){
										case 'text/xml':
										case 'application/xml':
											if(params.encoding){
												render(text:map as XML, contentType: 'application/xml',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as XML, contentType: 'application/xml')
												rreturn false
											}
											break
										case 'text/json':
										case 'application/json':
										default:
											if(params.encoding){
												render(text:map as JSON, contentType: 'application/json',encoding:"${params.encoding}")
												return false
											}else{
												render(text:map as JSON, contentType: 'application/json')
												return false
											}
											break
									}
									return false
								}
								break
						}
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
