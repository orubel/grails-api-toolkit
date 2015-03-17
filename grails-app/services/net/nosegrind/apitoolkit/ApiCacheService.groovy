/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import java.lang.reflect.Method
import java.util.HashSet;

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.cache.CachePut
import grails.plugin.cache.GrailsValueWrapper
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService

import org.springframework.cache.Cache

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import net.nosegrind.apitoolkit.*


class ApiCacheService{

	static transactional = false
	
	GrailsApplication grailsApplication
	//SpringSecurityService springSecurityService
	ApiLayerService apiLayerService
	//ApiToolkitService apiToolkitService
	GrailsCacheManager grailsCacheManager
	
	void flushAllApiCache(){
		grailsApplication?.controllerClasses?.each { controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				flushApiCache(controllername)
			}
		}
	}
	
	@CacheEvict(value="ApiCache",key="#controllername")
	void flushApiCache(String controllername){} 
	
	@CacheEvict(value="ApiCache",key="#controllername")
	Map resetApiCache(String controllername,String method,ApiDescriptor apidoc){
		setApiCache(controllername,method,apidoc)
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	Map setApiCache(String controllername,Map apidesc){
		return apidesc
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	LinkedHashMap setApiCache(String controllername,String methodname,ApiDescriptor apidoc, String apiversion){
		try{
			def cache = getApiCache(controllername)
			if(!cache[apiversion][methodname]){
				cache[apiversion][methodname] = [:]
			}
			if(cache[apiversion][methodname]){
				cache[apiversion][methodname]['name'] = apidoc.name
				cache[apiversion][methodname]['description'] = apidoc.description
				cache[apiversion][methodname]['receives'] = apidoc.receives
				cache[apiversion][methodname]['returns'] = apidoc.returns
				cache[apiversion][methodname]['errorcodes'] = apidoc.errorcodes
				cache[apiversion][methodname]['doc'] = apiLayerService.generateApiDoc(controllername, methodname,apiversion)
			}else{
				throw new Exception("[ApiCacheService :: setApiCache] : sts for controller/action pair of ${controllername}/${methodname}")
			}
			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setApiCache] : Exception - full stack trace follows:",e)
		}
	}

	@CachePut(value="ApiCache",key="#controllername")
	LinkedHashMap setApiDocCache(String controllername,String methodname, String apiversion, Map apidoc){
		try{
			def cache = getApiCache(controllername)
			if(cache[apiversion][methodname]){
				cache[apiversion][methodname]['doc'] = apiLayerService.generateApiDoc(controllername, methodname, apiversion)
			}else{
				throw new Exception("[ApiCacheService :: setApiCache] : No Cache exists for controller/action pair of ${controllername}/${methodname}")
			}
			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setApiDocCache] : Exception - full stack trace follows:",e)
		}
	}
	
	LinkedHashMap getApiCache(String controllername){
		try{
			def cache = grailsCacheManager.getCache('ApiCache').get(controllername)

			if(cache){
				return cache.get() as LinkedHashMap
			}

		}catch(Exception e){
			throw new Exception("[ApiCacheService :: getApiCache] : Exception - full stack trace follows:",e)
		}

	}
}
