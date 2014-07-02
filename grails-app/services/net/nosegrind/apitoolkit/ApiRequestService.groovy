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
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

//import java.lang.reflect.Method
import javax.servlet.forward.*
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
//import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse
//import org.codehaus.groovy.grails.web.util.WebUtils
//import org.codehaus.groovy.grails.validation.routines.UrlValidator

import org.springframework.cache.Cache
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.context.request.RequestContextHolder as RCH
//import org.springframework.ui.ModelMap

import net.nosegrind.apitoolkit.*

class ApiRequestService extends ApiLayerService{

	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	ApiCacheService apiCacheService
	
	static transactional = false
	
	ApiStatuses errors = new ApiStatuses()
	GrailsCacheManager grailsCacheManager
	
	List getContentType(SecurityContextHolderAwareRequestWrapper request){
		List tempType = request.getHeader('Content-Type')?.split(';')
		
		if(tempType){
			return tempType
		}else{
			return ['application/json']
		}
	}
	
	void setApiObjectVersion(String apiDir, SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params){
		// GET APICACHE VERSION; can be improved with regex/matcher
		List temp = request.forwardURI.split('\\/')
		def cache = apiCacheService.getApiCache(temp[2])
		
		params.apiObject = cache['currentStable']
		if(temp[1].contains("_v")){
			List temp2 = temp[1]?.split('-')
			if(temp2.size()>1){
				params.apiObject = temp2[1]?.toLong()
			}
		}else{
			List temp2 = temp[1]?.split('-')
			if(temp2.size()>1){
				params.apiObject = temp2[1]?.toLong()
			}
		}
	}
	
	void setParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		List formats = ['text/json','application/json','text/xml','application/xml']
		List tempType = request.getHeader('Content-Type')?.split(';')
		String type = (tempType)?tempType[0]:request.getHeader('Content-Type')
		type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
		
		switch(type){
			case 'application/json':
				request.JSON?.each() { key,value ->
					params.put(key,value)
				}
				break
			case 'application/xml':
				request.XML?.each() { key,value ->
					params.put(key,value)
				}
				break
		}
	}
	
	private void setApiParams(SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params){
		try{
			if(!params.contentType){
				List content = getContentType(request)
				params.contentType = content[0]
				params.encoding = (content.size()>1)?content[1]:null
				
				switch(params?.contentType){
					case 'text/json':
					case 'application/json':
						if(request?.JSON){
							request?.JSON.each{ k,v ->
								if(k=='chain'){
									params.apiChain = [:]
									params.apiChain = request.JSON.chain
									request.JSON.remove("chain")
								}else if(k=='batch'){
									params.apiBatch = []
									v.each { it ->
										params.apiBatch.add(it.value)
									}
									params.apiBatch = params.apiBatch.reverse()
									request.JSON.remove("batch")
								}else{
									params[k]=v
								}
								if(params?.apiChain?.combine=='true'){
									if(!params.apiCombine){ params.apiCombine = [:] }
								}
							}
						}
						break
					case 'text/xml':
					case 'application/xml':
						if(request?.XML){
							request?.XML.each{ k,v ->
								if(k=='chain'){
									params.apiChain = [:]
									params.apiChain = request.XML.chain
									request.XML.remove("chain")
								}else if(k=='batch'){
									params.apiBatch = []
									v.each { it ->
										params.apiBatch.add(it.value)
									}
									params.apiBatch = params.apiBatch.reverse()
									request.XML.remove("batch")
								}else{
									params[k]=v
								}
								if(params?.apiChain?.combine=='true'){
									if(!params.apiCombine){ params.apiCombine = [:] }
								}
							}
						}
						break
				}
			}
		}catch(Exception e){
			log.error("[ApiRequestService :: setApiParams] : Exception - full stack trace follows:", e);
		}
	}
	
	boolean checkDeprecationDate(String deprecationDate){
		def ddate = new SimpleDateFormat("MM/dd/yyyy").parse(deprecationDate)
		def deprecated = new Date(ddate.time)
		def today = new Date()
		if(deprecated < today ) {
			return true
		}
		return false
	}
	
	boolean handleApiRequest(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params){
		try{
			ApiStatuses error = new ApiStatuses()
			setApiParams(request, params)
			println("api params set")
			// CHECK IF URI HAS CACHE
			if(cache["${params.action}"]["${params.apiObject}"]){
				println("hascache")
				// CHECK IF PRINCIPAL HAS ACCESS TO API
				if(!checkAuthority(cache["${params.action}"]["${params.apiObject}"]['roles']?.toList())){
					println("returning false")
					return false
				}
				println("has authority")
				if(cache["${params.action}"]["${params.apiObject}"]['deprecated'][0]){
					String depdate = cache["${params.action}"]["${params.apiObject}"]['deprecated'][0]
					String depMsg = cache["${params.action}"]["${params.apiObject}"]['deprecated'][1]
					if(checkDeprecationDate(depdate)){
						// replace msg with config deprecation message
						String msg = "[ERROR] ${depMsg}"
						error._400_BAD_REQUEST(msg)?.send()
						return false
					}
				}
				println("not deprecated")
				// CHECK METHOD FOR API CHAINING. DOES METHOD MATCH?
				def method = cache["${params.action}"]["${params.apiObject}"]['method']?.trim()
				
				// DOES api.methods.contains(request.method)
				if(!isRequestMatch(method,request.method.toString())){
					// check for apichain

					// TEST FOR CHAIN PATHS
					if(params?.apiChain){
						List uri = [params.controller,params.action,params.id]
						int pos = checkChainedMethodPosition(request, params,uri,params?.apiChain?.order as Map)
						if(pos==3){
							String msg = "[ERROR] Bad combination of unsafe METHODS in api chain."
							error._400_BAD_REQUEST(msg)?.send()
							return false
						}else{
							return true
						}
					}else{
						return true
					}
				}else{
				println("request matches")
					// (NON-CHAIN) CHECK WHAT TO EXPECT; CLEAN REMAINING DATA
					// RUN THIS CHECK AFTER MODELMAP FOR CHAINS
					if(!checkURIDefinitions(cache["${params.action}"]["${params.apiObject}"]['receives'])){
						println("bas uri def")
						String msg = 'Expected request variables do not match sent variables'

						error._400_BAD_REQUEST(msg)?.send()
						return false
					}else{
						return true
					}
				}

			}
		}catch(Exception e){
			log.error("[ApiRequestService :: handleApiRequest] : Exception - full stack trace follows:", e);
		}

	}
	
	boolean isRequestMatch(String protocol,String method){
		if(['OPTIONS','TRACE','HEAD'].contains(method)){
			return true
		}else{
			if(protocol == method){
				return true
			}else{
				return false
			}
		}
		return false
	}
	
	/*
	 * Returns chainType
	 * 1 = prechain
	 * 2 = postchain
	 * 3 = illegal combination
	 */
	int checkChainedMethodPosition(SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params, List uri, Map path){
		boolean preMatch = false
		boolean postMatch = false
		boolean pathMatch = false

		List keys = path.keySet() as List
		Integer pathSize = keys.size()
		
		String controller = uri[0]
		String action = uri[1]
		Long id = uri[2]
		
		// prematch check
		String method = net.nosegrind.apitoolkit.Method["${request.method.toString()}"].toString()
		def cache = apiCacheService.getApiCache(controller)
		//def methods = cache["${uri[1]}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
		String methods = cache["${action}"]["${params.apiObject}"]['method'].trim()

		if(method=='GET'){
			if(methods != method){
				preMatch = true
			}
		}else{
			if(methods == method){
				preMatch = true
			}
		}

		// postmatch check
		if(pathSize>=1){
			String last=path[keys[pathSize-1]]
			if(last && last!='return'){
				List last2 = keys[pathSize-1].split('/')
				cache = apiCacheService.getApiCache(last2[0])
				methods = cache["${last2[1]}"]["${params.apiObject}"]['method'].trim()
				if(method=='GET'){
					if(methods != method){
						postMatch = true
					}
				}else{
					if(methods == method){
						postMatch = true
					}
				}
			}else{
				postMatch = true
			}
		}

		// path check
		int start = 1
		int end = pathSize-2
		if(start<end){
			keys[0..(pathSize-1)].each{ val ->
				if(val){
					List temp2 = val.split('/')
					cache = apiCacheService.getApiCache(temp2[0])
					methods = cache["${temp2[1]}"]["${params.apiObject}"]['method'].trim() as List
					if(method=='GET'){
						if(methods != method){
							pathMatch = true
						}
					}else{
						if(methods == method){
							pathMatch = true
						}
					}
				}
			}
		}

		if(pathMatch || (preMatch && postMatch)){
			return 3
		}else{
			if(preMatch){
				setParams(request,params)
				return 1
			}else if(postMatch){
				setParams(request,params)
				return 2
			}
		}
		
		// path but but no position
		// could be trying to make api call and url encode data
		// if so, not restful; does not comply
		return 3

	}
}
