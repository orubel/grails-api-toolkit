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
import java.lang.reflect.Method
import java.util.HashSet;

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.cache.CachePut
import grails.plugin.cache.GrailsCacheManager
import org.springframework.cache.Cache

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH

import net.nosegrind.apitoolkit.*

class ApiCacheService{

	def grailsApplication
	def springSecurityService
	def apiToolkitService
	GrailsCacheManager grailsCacheManager
	
	static transactional = false
	
	def flushAllApiCache(){
		grailsApplication.controllerClasses.each { controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				flushApiCache(controllername)
			}
		}
	}
	
	@CacheEvict(value="ApiCache",key="#controllername")
	def flushApiCache(String controllername){} 
	
	@CacheEvict(value="ApiCache",key="#controllername")
	def resetApiCache(String controllername,String method,ApiDescriptor apidoc){
		setApiCache(controllername,method,apidoc)
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(String controllername,Map apidesc){
		return apidesc
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(String controllername,String methodname,ApiDescriptor apidoc){
		def cache = getApiCache(controllername)
		try{
			if(!cache){
				cache = [:]
			}
			if(!cache["${methodname}"]){
				cache["${methodname}"] = [:]
			}

			cache["${methodname}"]['name'] = apidoc.name
			cache["${methodname}"]['description'] = apidoc.description
			cache["${methodname}"]['receives'] = apidoc.receives
			cache["${methodname}"]['returns'] = apidoc.returns
			cache["${methodname}"]['errorcodes'] = apidoc.errorcodes

			return cache
		}catch(Exception e){
			log.info("[Error]: net.nosegrind.apitoolkit.ApiCacheService.setApiCache : Error caching ${controllername}/${methodname} pair : ${e}")
		}
	}

	/*
	private def setApiCache(String controllername){
		def temp = grailsCacheManager.getCache('ApiCache')
		def cache
		temp.put(controllername,[:])
		cache = temp.get(controllername).get()
		return cache
	}
	*/
	
	def getApiCache(String controllername){
		try{
			def cache = grailsCacheManager.getCache('ApiCache').get(controllername)
			if(cache){
				return cache.get()
			}
			//return cache
		}catch(Exception e){
			log.info("[Error]: net.nosegrind.apitoolkit.ApiCacheService.getApiCache : No Cache exists for controller ${controllername} : ${e}")
		}
	}
	


}
