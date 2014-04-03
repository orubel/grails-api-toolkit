package net.nosegrind.apitoolkit

import org.codehaus.groovy.grails.web.json.JSONObject
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

import grails.converters.JSON
import grails.converters.XML

import java.util.HashSet;
import java.util.LinkedHashMap;

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
		return json
	}
	
	def validateRequestData(){}
	
	def validateResponseData(){}
	
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	private ApiDescriptor createApiDescriptor(String apiname, String uri, JSONObject json){
		Map apiParams = [
			"receives":[:],
			"returns":[:]
		]
		LinkedHashMap<String,ParamsDescriptor> apiObject = [:]
		LinkedHashMap<String,ParamsDescriptor> receives = [:]
		LinkedHashMap<String,ParamsDescriptor> returns = [:]

		ApiParams param = new ApiParams()
		def booleans = ['true','false']
		
		json["${apiname}"].VALUES.each{ k,v ->

			String references = ""
			String hasDescription = ""
			String hasMockData = ""
			
			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			param.setParam(v.type,"${k}")
			
			// get grails config variable data
			def configType = grailsApplication.config.apitoolkit.apiobject.type."${v.type}"
			hasDescription = (configType?.description)?configType.description:hasDescription
			references = (configType?.references)?configType.references:""
			
			// get variable data
			hasDescription = (v?.description)?v.description:hasDescription
			hasMockData = (v?.mockData)?v.mockData:hasMockData
			references = (v?.references)?v.references:references
			
			if(hasMockData){
				param.hasMockData("${hasMockData}")
			}
			
			if(hasDescription){
				param.hasDescription("${hasMockData}")
			}
			
			if(references){
				param.referencedBy(references)
			}
			
			// collect api vars into list to use in apiDescriptor
			apiObject["${param.param.name}"] = param.toObject()
		}
		def method = json["${apiname}"].URI["${uri}"].METHOD
		def description = json["${apiname}"].URI["${uri}"]?.DESCRIPTION
		def roles = json["${apiname}"].URI["${uri}"]?.ROLES
		
		// REQUEST
		def permitAll = []
		receives['permitAll'] = permitAll
		json["${apiname}"].URI["${uri}"]?.REQUEST.each{ k, v ->
			if(!receives["${k}"]){
				receives["${k}"] = (k!='permitAll')?receives['permitAll']:[]
			}
			def roleVars=v.toList()
			roleVars.each{ val ->
				receives["${k}"].add(apiObject["${val}"])
			}
		}
		
		// RESPONSE
		permitAll = []
		returns['permitAll'] = permitAll
		json["${apiname}"].URI["${uri}"]?.RESPONSE.each{ k, v ->
			if(!returns["${k}"]){
				returns["${k}"] = (k!='permitAll')?returns['permitAll']:[]
			}
			def roleVars=v.toList()
			roleVars.each{ val ->
				returns["${k}"].add(apiObject["${val}"])
			}
		}
		
		// foreach key, is role your current authority or 'permitAll'
		// if so, add param to list of receives/returns
		
		ApiDescriptor service = new ApiDescriptor(
			"method":"${method}",
			"description":"${description}",
			"doc":[:],
			"receives":receives,
			"returns":returns
		)
		service['roles'] = roles

		return service
	}
	
	private ApiParams checkRules(String restMethod, ApiParams param,String apiname, String actionname, JSONObject json,String key){
		
		def value = json["${apiname}"].VALUES["${key}"]
		def type = value.type
		
		String references = ""
		String hasDescription = ""
		String hasMockData = ""
		
		// get grails config variable data
		def configType = grailsApplication.config.apitoolkit.apiobject.type."${type}"
		hasDescription = (configType?.description)?configType.description:hasDescription
		hasMockData = (configType?.mockData)?configType.mockData:hasMockData
		references = (configType?.references)?configType.references:""
		
		// get variable data
		hasDescription = (value?.description)?value.description:hasDescription
		hasMockData = (value?.mockData)?value.mockData:hasMockData
		references = (value?.references)?value.references:references
		
		if(hasMockData){
			param.hasMockData("${hasMockData}")
		}
		
		if(hasDescription){
			param.hasDescription("${hasMockData}")
		}
		
		if(references){
			param.referencedBy(references)
		}
		
		return param
	}
	
	def initApiCache(){
		JSONObject json = readObjectFile()

		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			
			Map methods = [:]
			controllerClass.getClazz().methods.each { Method method ->
				String actionname = method.getName()
				
				ApiStatuses error = new ApiStatuses()
				
				if(method.isAnnotationPresent(Api)) {
					def api = method.getAnnotation(Api)
					ApiDescriptor apiDescriptor
					Map apiParams
					
					String apiname = (api?.name())?api.name().capitalize():controllername.capitalize()
					if(json["${apiname}"]){
						//def actionRule = (json["${apiname().capitalize()}"].RULES?."${actionname}")?json["${api.name().capitalize()}"].RULES."${actionname}":[:]
						String uri = controllername+"/"+actionname
						if(json["${apiname}"].URI?."${uri}"){
							apiDescriptor = createApiDescriptor(apiname, uri, json)
						}
					}

					methods["${actionname}"] = apiDescriptor
				}
			}
			
			if(methods){
				println("has methods")
				String controller = controllername.toString()
				apiToolkitService.setApiCache(controller,methods)
			}
			
			def cache = apiCacheService.getApiCache(controllername)

		}
	}
}
