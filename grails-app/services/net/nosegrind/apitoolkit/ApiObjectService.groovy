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
		return json
	}
	
	def validateRequestData(){}
	
	def validateResponseData(){}
	
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	private Map createApiDescriptor(String apiname, String uri, JSONObject json){
		Map apiParams = [
			"receives":[:],
			"returns":[:]
		]
		List<ParamsDescriptor> apiObject = []
		List<ParamsDescriptor> receives = []
		List<ParamsDescriptor> returns = []

		ApiParams param = new ApiParams()
		def booleans = ['true','false']
		
		json["${apiname}"].VALUES.each{ k,v ->

			String references = ""
			String hasDescription = ""
			String hasMockData = ""
			
			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			// Create Param (and edit rule defaults for keys)
			switch(v.type.toLowerCase()){
				case 'pkey':
					param._PKEY("${k}")
					break
				case 'fkey':
					param._FKEY("${k}")
					break
				case 'index':
					param._INDEX("${k}")
					break
				case 'long':
					param._LONG("${k}")
					break
				case 'string':
					param._STRING("${k}")
					break
				case 'boolean':
					param._BOOLEAN("${k}")
					break
				case 'bigdecimal':
					param._BIGDECIMAL("${k}")
					break
				case 'float':
					param._FLOAT("${k}")
					break
				case 'email':
					param._EMAIL("${k}")
					break
				case 'url':
					param._URL("${k}")
					break
			}
			
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
			apiObject.add(param.toObject())

			
		}
		def method = json["${apiname}"].URI["${uri}"].METHOD
		def description = json["${apiname}"].URI["${uri}"]?.DESCRIPTION
		def roles = json["${apiname}"].URI["${uri}"]?.ROLES
		
		
		// foreach key, is role your current authority or 'permitAll'
		// if so, add param to list of receives/returns
		
		ApiDescriptor service = new ApiDescriptor(
			"method":"${method}",
			"description":"${description}",
			"doc":[:],
			"receives":receives,
			"returns":returns
		)
		service['roles'] = api.roles()
		
		// send apiObject with json
		//ApiParams param2 = checkRules(method, param, apiname, actionname, json,k)

			// Required Rule
			/*
			if(param2.param.required){
				if(param2.param.roles){
					//receives.putAt(receives.size(),param.toObject())
					param2.param.roles.each{ role ->
						if(apiParams?.receives["${role}"]){
							receives = apiParams.receives["${role}"]
							receives[receives.size()] = param.toObject()
							apiParams.receives["${role}"] = receives
					   }else{
						   apiParams.receives["${role}"] = []
						   receives[0] = param.toObject()
						   apiParams.receives["${role}"] = receives
					   }
					}
				}else{
					//receives.putAt(receives.size(),param.toObject())
					if(apiParams?.receives["permitAll"]){
						receives = apiParams.receives["permitAll"]
						receives[receives.size()] = param.toObject()
						apiParams.receives["permitAll"] = receives
					}else{
						apiParams.receives["permitAll"] = []
						receives[0] = param.toObject()
						apiParams.receives["permitAll"] = receives
					}
				}
			}
			*/


		
		return apiParams
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
					ApiParams param
					Map apiParams
					
					String apiname = (api?.name())?api.name().capitalize():controllername.capitalize()
					if(json["${apiname}"]){
						//def actionRule = (json["${apiname().capitalize()}"].RULES?."${actionname}")?json["${api.name().capitalize()}"].RULES."${actionname}":[:]
						String uri = controllername+"/"+actionname
						if(json["${apiname}"].URI?."${uri}"){
							apiParams = createApiDescriptor(apiname, uri, json)
						}
					}
					
					LinkedHashMap<String,ParamsDescriptor> receives = apiParams?.receives
					LinkedHashMap<String,ParamsDescriptor> returns = apiParams?.returns
					
println("############## ${actionname}")
println("${api.method()}")
println("${api.description()}")
println("receives : ${receives}")
println("returns : ${returns}")

					
					ApiDescriptor service = new ApiDescriptor(
						"method":"${api.method()}",
						"description":"${api.description()}",
						"doc":[:],
						"receives":receives,
						"returns":returns
					)
					service['roles'] = api.roles()
					methods["${actionname}"] = service
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
