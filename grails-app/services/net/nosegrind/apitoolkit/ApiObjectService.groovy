package net.nosegrind.apitoolkit

import org.codehaus.groovy.grails.web.json.JSONObject
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

import grails.converters.JSON
import grails.converters.XML

import java.util.HashSet;

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.cache.CachePut
import grails.plugin.cache.GrailsCacheManager
import org.springframework.cache.Cache

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH

import net.nosegrind.apitoolkit.*

class ApiObjectService{

	def grailsApplication
	def springSecurityService
	def apiToolkitService
	def apiCacheService
	GrailsCacheManager grailsCacheManager
	
	static transactional = false
	
	private JSONObject readObjectFile(){
		def filePath = "apiObjects.json"
		def text = grailsApplication.getParentContext().getResource("classpath:$filePath").getInputStream().getText()
		def json = JSON.parse(text)
		println(json)
		return json
	}
	
	def initApiCache(){
		JSONObject json = readObjectFile()
		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			Map methods = [:]
			controllerClass.getClazz().methods.each { Method method ->
				String actionname = method.getName()
				
				ApiStatuses error = new ApiStatuses()
				ApiParams param = new ApiParams()
				
				if(method.isAnnotationPresent(Api)) {
					def api = method.getAnnotation(Api)

					ApiDescriptor service = new ApiDescriptor(
						"method":"${api.method()}",
						"description":"${api.description()}",
						"doc":[:]
					)
					
					/*
					* 'required' is data required from incoming request to validate api object for PUT and POST
					* 'expose' exposes to value to the service; defaults to true on all keyTypes.else true
					* 'visible' makes data visible/invisible to user; defaults to false on all keyTypes.
					* 'roles' allows you to set prics on individual variables to return partial datasets
					* 'mockData' allows you to set default data on the variable. This can be dynamic or static
					*/
					def rules = [
						'GET':['required':false,'expose':true,'visible':]
					]
					json["${actionname}"].values.each{ val ->
						switch(val){
							case 'PKEY':
								break
							case 'FKEY':
								break
							case 'INDEX':
								break
							case 'STRING':
								break
							case 'BOOLEAN':
								break
							case 'FLOAT':
								break
							case 'BIGDECIMAL':
								break
							case 'LONG':
								break
							case 'EMAIL':
								break
							case 'URL':
								break
						}
					}
					
					service['roles'] = api.roles()
					
					methods["${actionname}"] = service
				}
			}
			if(methods){
				apiCacheService.setApiCache("${controllername}".toString(),methods)
			}
		}
	}

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
