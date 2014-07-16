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
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService
//import grails.spring.BeanBuilder
//import grails.util.Holders as HOLDER

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

//import java.lang.reflect.Method
import javax.servlet.forward.*
//import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse
import org.codehaus.groovy.grails.web.util.WebUtils
//import org.codehaus.groovy.grails.validation.routines.UrlValidator

import org.springframework.cache.Cache
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.context.request.RequestContextHolder as RCH
//import org.springframework.ui.ModelMap

import net.nosegrind.apitoolkit.*


class ApiLayerService{

	static transactional = false
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	ApiCacheService apiCacheService
	
	ApiStatuses errors = new ApiStatuses()
	GrailsCacheManager grailsCacheManager
	


	
	private SecurityContextHolderAwareRequestWrapper getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}
	
	private GrailsContentBufferingResponse getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	boolean checkURIDefinitions(LinkedHashMap requestDefinitions){
		try{
			List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
			List requestList = getApiParams(requestDefinitions)
			HashMap params = getMethodParams()
	
			//GrailsParameterMap params = RCH.currentRequestAttributes().params
			List paramsList = params.post.keySet() as List
			paramsList.removeAll(optionalParams)
	
			if(paramsList.containsAll(requestList)){
				paramsList.removeAll(requestList)
				if(!paramsList){
					return true
				}
			}
			return false
		}catch(Exception ex) {
			println(ex)
			//throw ex
		}
	}
	
	List getApiParams(LinkedHashMap definitions){
		String authority = springSecurityService.principal.authorities*.authority[0]
		ParamsDescriptor[] temp = (definitions["${authority}"])?definitions["${authority}"]:definitions["permitAll"]
		List apiList = []
		temp.each{
			apiList.add(it.name)
		}
		return apiList
	}
	
	HashMap getMethodParams(){
		boolean isChain = false
		List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
		SecurityContextHolderAwareRequestWrapper request = getRequest()
		GrailsParameterMap params = RCH.currentRequestAttributes().params
		Map paramsRequest = params.findAll {
			if(it.key=='apiChain'){ isChain=true }
			return !optionalParams.contains(it.key)
		}
		
		Map paramsGet = [:]
		Map paramsPost = [:]
		if(isChain){
			paramsPost = paramsRequest
		}else{
			paramsGet = WebUtils.fromQueryString(request.getQueryString() ?: "")
			paramsPost = paramsRequest.minus(paramsGet)
		}

		return ['get':paramsGet,'post':paramsPost]
	}
	
	void setApiCache(String controllername,LinkedHashMap apidoc){
		apiCacheService.setApiCache(controllername,apidoc)
		//def cache = grailsCacheManager.getCache('ApiCache').get(controllername).get()

		apidoc.each(){ k1,v1 ->
			if(k1!='currentStable'){
				v1.each() { k2,v2 ->
					def doc = generateApiDoc(controllername, k1, k2)
					apiCacheService.setApiDocCache(controllername,k1,k2,doc)
				}
			}
		}
	}
	
	Map generateApiDoc(String controllername, String actionname, String apiversion){
		Map doc = [:]
		def cont = apiCacheService.getApiCache(controllername)
		String apiPrefix = (grailsApplication.config.apitoolkit.apiName)?"${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String:"v${grailsApplication.metadata['app.version']}" as String
		
		if(cont){

			String path = "/${apiPrefix}-${apiversion}/${controllername}/${actionname}"
			doc = ["path":"${path}","method":cont[("${actionname}".toString())][("${apiversion}".toString())]["method"],"description":cont[("${actionname}".toString())][("${apiversion}".toString())]["description"]]
			
			if(cont["${actionname}"]["${apiversion}"]["receives"]){

				doc["receives"] = [:]
				for(receiveVal in cont["${actionname}"]["${apiversion}"]["receives"]){
					doc["receives"]["${receiveVal.key}"] = receiveVal.value
				}
			}
			
			if(cont["${actionname}"]["${apiversion}"]["returns"]){
				doc["returns"] = [:]
				for(returnVal in cont["${actionname}"]["${apiversion}"]["returns"]){
					doc["returns"]["${returnVal.key}"] = returnVal.value
				}
				doc["json"] = [:]
				doc["json"] = processJson(doc["returns"])
			}
			
			if(cont["${actionname}"]["${apiversion}"]["errorcodes"]){
				doc["errorcodes"] = processDocErrorCodes(cont[("${actionname}".toString())][("${apiversion}".toString())]["errorcodes"] as HashSet)
			}

		}

		return doc
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	private String processJson(LinkedHashMap returns){
		def json = [:]
		returns.each{ p ->
				p.value.each{ it ->
					ParamsDescriptor paramDesc = it
				
					def j = [:]
					if(paramDesc?.values){
						j["${paramDesc.name}"]=[]
					}else{
						String dataName=(['PKEY','FKEY','INDEX'].contains(paramDesc.paramType.toString()))?'ID':paramDesc.paramType
						j = (paramDesc?.mockData?.trim())?["${paramDesc.name}":"${paramDesc.mockData}"]:["${paramDesc.name}":"${dataName}"]
					}
					j.each(){ key,val ->
						if(val instanceof List){
							def child = [:]
							val.each(){ it2 ->
								it2.each(){ key2,val2 ->
									child["${key2}"] ="${val2}"
								}
							}
							json["${key}"] = child
						}else{
							json["${key}"]=val
						}
					}
				}
		}

		if(json){
			json = json as JSON
		}
		return json
	}
	
	private ArrayList processDocErrorCodes(HashSet error){
		def errors = error as List
		def err = []
		errors.each{ v ->
			def code = ['code':v.code,'description':"${v.description}"]
			err.add(code)
		}
		return err
	}
	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		SecurityContextHolderAwareRequestWrapper request = getRequest()
		GrailsParameterMap params = RCH.currentRequestAttributes().params
		String uri = request.forwardURI.split('/')[1]
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String api
		if(params.apiObject){
			api = (apiName)?"${apiName}_v${apiVersion}-${params.apiObject}" as String:"v${apiVersion}-${params.apiObject}" as String
		}else{
			api = (apiName)?"${apiName}_v${apiVersion}" as String:"v${apiVersion}" as String
		}
		return uri==api
	}
}
