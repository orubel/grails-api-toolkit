package net.nosegrind.restrpc

import grails.converters.JSON
import grails.converters.XML
import java.lang.reflect.Method
import java.util.HashSet;

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.cache.CachePut

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import net.nosegrind.restrpc.Api
import net.nosegrind.restrpc.*
import net.nosegrind.restrpc.ApiDescriptor;

class ApiCacheService{

	def grailsApplication
	def springSecurityService

	static transactional = false
	
	def flushAllApiCache(){
		grailsApplication.controllerClasses.each { controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
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
	def setApiCache(String controllername,String method,ApiDescriptor apidoc){
		Map api = [("${method}".toString()):apidoc]
		return api
	}
	
	@Cacheable(value="ApiCache",key="#controllername")
	def getApiCache(String controllername){
		return
	}
	
	String getBelongsTo(String paramType, String controller, String belongsTo){
		return (paramType=='PKey')?controller:belongsTo
	}

}
