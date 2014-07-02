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

	boolean checkAuthority(ArrayList role) throws Exception{
		try{
			List roles = role as List
			if(roles.size()==1 && roles.contains('permitAll')){
				return true
			}else{
				if(roles.size()>0){
					List roles2 = grailsApplication.getDomainClass(grailsApplication.config.grails.plugin.springsecurity.authority.className).clazz.list().authority
					println(roles2)
					List finalRoles = []
					List userRoles = []
					if (springSecurityService.isLoggedIn()){
						userRoles = springSecurityService.getPrincipal().getAuthorities() as List
					}
					
					if(userRoles){
						List temp = roles2.intersect(roles)
						finalRoles = temp.intersect(userRoles)
						if(finalRoles){
							return true
						}else{
							return false
						}
					}else{
						return false
					}
				}else{
					return false
				}
			}
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
}
