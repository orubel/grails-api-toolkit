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

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import org.codehaus.groovy.grails.web.json.JSONObject

import net.nosegrind.apitoolkit.Method;
import net.nosegrind.apitoolkit.ApiStatuses;
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse

import net.nosegrind.apitoolkit.*

class ApiToolkitFilters {
	
	ApiRequestService apiRequestService
	ApiResponseService apiResponseService
	
	//ApiToolkitService apiToolkitService
	GrailsApplication grailsApplication
	ApiCacheService apiCacheService
	
	def filters = {
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String entryPoint = (apiName)?"${apiName}_v${apiVersion}":"v${apiVersion}"

		//String apiRegex = "/${apiRoot}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**".toString()
		
		//apitoolkit(regex:apiRegex){
		apitoolkit(uri:"/${entryPoint}*/**"){
			before = {
				//log.error
				println("##### FILTER (BEFORE)")

				try{
					if(!request.class.toString().contains('SecurityContextHolderAwareRequestWrapper')){
						return false
					}

					params.action = (params.action)?params.action:'index'
					def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]

					if(cache){
						boolean result = apiRequestService.handleApiRequest(cache,request,params,entryPoint)
						return result
					}
					
					return false

				}catch(Exception e){
					log.error("[ApiToolkitFilters :: apitoolkit.before] : Exception - full stack trace follows:", e);
					return false
				}
			}
			
			after = { Map model ->
				 //log.error
				println("##### FILTER (AFTER)")
				 try{
				 	def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]
					 LinkedHashMap map = apiResponseService.handleApiResponse(cache,request,response,model,params)
					 if(!model){
						 response.flushBuffer()
						 return false
					 }
					 
					 if(params?.apiCombine==true){
							map = params.apiCombine
					 }
					 
					 if(params?.apiChain?.order){
						 boolean result = apiResponseService.handleApiChain(cache, request,response,model,params)
						 forward(controller:"${params.controller}",action:"${params.action}",id:"${map.id}")
						 return false
					 }else if(params?.apiBatch){
							 forward(controller:"${params.controller}",action:"${params.action}",params:params)
							 return false
					 }else{
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
									 case 'application/xml':
										 render(text:doc as XML, contentType: "${params.contentType}")
										 break
									 case 'application/json':
									 default:
										 render(text:doc as JSON, contentType: "${params.contentType}")
										 break
								 }
								 return false
								 break;
							 case 'GET':
								 if(map?.isEmpty()==false){
									 switch(params.contentType){
										 case 'application/xml':
											 if(params.encoding){
												 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as XML, contentType: "${params.contentType}")
											 }
											 break
										 case 'text/html':
											 break
										 case 'application/json':
										 default:
											 if(params.encoding){
												 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as JSON, contentType: "${params.contentType}")
											 }
											 break
									 }
									 return false
								 }
								 break
							 case 'POST':
								 if(!map.isEmpty()){
									 switch(params.contentType){
										 case 'application/xml':
											 if(params.encoding){
												 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as XML, contentType: "${params.contentType}")
											 }
											 break
										 case 'application/json':
										 default:
											 if(params.encoding){
												 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as JSON, contentType: "${params.contentType}")
											 }
											 break
									 }
									 return false
								 }
								 break
							 case 'PUT':
								 if(!map.isEmpty()){
									 switch(params.contentType){
										 case 'application/xml':
											 if(params.encoding){
												 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as XML, contentType: "${params.contentType}")
											 }
											 break
										 case 'application/json':
										 default:
											 if(params.encoding){
												 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as JSON, contentType: "${params.contentType}")
											 }
											 break
									 }
									 return false
								 }
								 break
							 case 'DELETE':
								 if(!map.isEmpty()){
									 switch(params.contentType){
										 case 'application/xml':
											 if(params.encoding){
												 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as XML, contentType: "${params.contentType}")
											 }
											 break
										 case 'application/json':
										 default:
											 if(params.encoding){
												 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
											 }else{
												 render(text:map as JSON, contentType: "${params.contentType}")
											 }
											 break
									 }
									 return false
								 }
								 break
						 }
					 }
					 return false
				}catch(Exception e){
					log.error("[ApiToolkitFilters :: apitoolkit.after] : Exception - full stack trace follows:", e);
					return false
				}
			 }

		}

	}

}
