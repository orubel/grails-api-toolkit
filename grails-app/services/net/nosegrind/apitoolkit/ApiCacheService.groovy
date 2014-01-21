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
	def setApiCache(String controllername,Map apidoc){
		return apidoc
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(String controllername,String methodname,ApiDescriptor apidoc){
		try{
			def cache = getApiCache(controllername)
			if(cache["${methodname}"]){
				cache["${methodname}"]['name'] = apidoc.name
				cache["${methodname}"]['description'] = apidoc.description
				cache["${methodname}"]['receives'] = apidoc.receives
				cache["${methodname}"]['returns'] = apidoc.returns
				cache["${methodname}"]['errorcodes'] = apidoc.errorcodes
				cache["${methodname}"]['doc'] = apiToolkitService.generateApiDoc(controllername, methodname)
			}else{
				log.info "[Error]: net.nosegrind.apitoolkit.ApiCacheService.setApiCache : No Cache exists for controller/action pair of ${controllername}/${methodname} "
			}
			return cache
		}catch(Exception e){
			log.info("[Error]: net.nosegrind.apitoolkit.ApiCacheService.setApiCache : No Cache exists for controller/action pair of ${controllername}/${methodname} ")
		}
	}

	def getApiCache(String controllername){
		try{
			def cache = grailsCacheManager.getCache('ApiCache').get(controllername).get()
			return cache
		}catch(Exception e){
			log.info("[Error]: net.nosegrind.apitoolkit.ApiCacheService.getApiCache : No Cache exists for controller ${controllername} ")
		}
	}

}
