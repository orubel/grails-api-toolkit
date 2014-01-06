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

import net.nosegrind.apitoolkit.ApiDescriptor;
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
	def setApiCache(String controllername,Map data){
		return data
	}
	
	@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(String controllername,String methodname,ApiDescriptor apidoc){
		Map output
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		for (Method method : controller.getClazz().getDeclaredMethod(methodname)){
			if(method.isAnnotationPresent(Api)) {
				def api = method.getAnnotation(Api)
				apidoc['apiRoles'] = api.apiRoles()
				if(api.hookRoles()){
					apidoc['hookRoles'] = api.hookRoles()
				}
				apidoc['doc'] = apiToolkitService.generateApiDoc(controllername,methodname)
				output = [("${methodname}".toString()):apidoc]
			}
		}
		return output
	}
	
	@Cacheable(value="ApiCache",key="#controllername")
	def getApiCache(String controllername){
		def cache = grailsCacheManager.getCache('ApiCache').get("${controllername}")
		setApiCache(controllername,cache as Map)
		println(cache)
		return cache
	}
	
	String getBelongsTo(String paramType, String controller, String belongsTo){
		return (paramType=='PKey')?controller:belongsTo
	}

}
