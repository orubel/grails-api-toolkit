/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.forward.*

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper

import net.nosegrind.apitoolkit.*

class ApiRequestService extends ApiLayerService{

	static transactional = false
	
	boolean handleApiRequest(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params, String entryPoint){
		try{
			setEnv()
			
			ApiStatuses error = new ApiStatuses()
			setApiParams(request, params)
			// CHECK IF URI HAS CACHE
			if(cache[params.action][params.apiObject]){
				// CHECK ACCESS TO METHOD
				List roles = cache["${params.action}"]["${params.apiObject}"]['roles']?.toList()
				if(!checkAuth(request,roles)){
					return false
				}
				
				// CHECK VERSION DEPRECATION DATE
				if(cache[params.action][params.apiObject]['deprecated'][0]){
					String depdate = cache[params.action][params.apiObject]['deprecated'][0]
					
					if(checkDeprecationDate(depdate)){
						String depMsg = cache[params.action][params.apiObject]['deprecated'][1]
						// replace msg with config deprecation message
						String msg = "[ERROR] ${depMsg}"
						error._400_BAD_REQUEST(msg)?.send()
						return false
					}
				}
				
				// CHECK METHOD FOR API CHAINING. DOES METHOD MATCH?
				def method = cache[params.action][params.apiObject]['method']?.trim()
				
				// DOES api.methods.contains(request.method)
				if(!isRequestMatch(method,request.method.toString())){
					// check for apichain
					
					// TEST FOR CHAIN PATHS
					if(chain && params?.apiChain){
						List uri = [params.controller,params.action,params.id]
						int pos = checkChainedMethodPosition(cache,request, params,uri,params?.apiChain?.order as Map)
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
					// (NON-CHAIN) CHECK WHAT TO EXPECT; CLEAN REMAINING DATA
					// RUN THIS CHECK AFTER MODELMAP FOR CHAINS
					if(batch && params.apiBatch){
						
						def temp = params.apiBatch.remove(0)
						temp.each{ k,v ->
							params[k] = v
						}
					}
					List batchRoles = cache["${params.action}"]["${params.apiObject}"]['batchRoles']?.toList()
					if(!checkAuth(request,batchRoles)){
						return false
					}else{
						return true
					}
					if(!checkURIDefinitions(request,cache[params.action][params.apiObject]['receives'])){
						String msg = 'Expected request variables do not match sent variables'
						error._400_BAD_REQUEST(msg)?.send()
						return false
					}else{
						return true
					}
				}

			}
		}catch(Exception e){
			throw new Exception("[ApiRequestService :: handleApiRequest] : Exception - full stack trace follows:"+ e);
		}
	}
	
	protected void setApiParams(SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params){
		try{
			if(!params.contentType){
				List content = getContentType(request.getHeader('Content-Type'))
				params.contentType = content[0]
				params.encoding = (content.size()>1)?content[1]:null
				
				if(chain && params?.apiChain?.combine=='true'){
					if(!params.apiCombine){ params.apiCombine = [:] }
				}
				
				switch(params?.contentType){
					case 'text/json':
					case 'application/json':
						if(request?.JSON){
							request?.JSON.each{ k,v ->
								if(chain && k=='chain'){
									params.apiChain = [:]
									params.apiChain = request.JSON.chain
								}else if(batch && k=='batch'){
									params.apiBatch = []
									v.each { it ->
										params.apiBatch.add(it)
									}
									params.apiBatch = params.apiBatch
									//request.JSON.remove("batch")
								}else{
									params[k]=v
								}
							}
							request.JSON.remove("chain")
							request.JSON.remove("batch")
						}
						break
					case 'text/xml':
					case 'application/xml':
						if(request?.XML){
							request?.XML.each{ k,v ->
								if(chain && k=='chain'){
									params.apiChain = [:]
									params.apiChain = request.XML.chain
									//request.XML.remove("chain")
								}else if(batch && k=='batch'){
									params.apiBatch = []
									v.each { it ->
										params.apiBatch.add(it.value)
									}
									params.apiBatch = params.apiBatch.reverse()
									//request.XML.remove("batch")
								}else{
									params[k]=v
								}
							}
							request.XML.remove("chain")
							request.XML.remove("batch")
						}
						break
				}
			}
			
		}catch(Exception e){
			throw new Exception("[ApiRequestService :: setApiParams] : Exception - full stack trace follows:"+ e);
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
}
