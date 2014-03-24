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
	
	List getRules(JSONObject values){
		
		return rules
	}
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	Map createApiParams(Map actionRule, JSONObject values, String apiObjectName){
		Map apiParams = [
			'receives':[:],
			'returns':[:]
		]
		ApiParams param = new ApiParams()
		def booleans = ['true','false']
		
		values.each{ k,v ->
			boolean required = false
			boolean visible = true
			
			v.type = (v.key)?getKeyType(v.key, v.type):v.type

			// Create Param (and edit rule defaults for keys)
			switch(v.type.toLowerCase()){
				case 'pkey':
					param._PKEY("${k}","${v.description}","${apiObjectName}")
					required = true
					break
				case 'fkey':
					param._FKEY("${k}","${v.description}","${apiObjectName}")
					visible = false
					break
				case 'index':
					param._INDEX("${k}","${v.description}","${apiObjectName}")
					visible = false
					break
				case 'long':
					param._LONG("${k}","${v.description}")
					break
				case 'string':
					param._STRING("${k}","${v.description}")
					break
				case 'boolean':
					param._BOOLEAN("${k}","${v.description}")
					break
				case 'bigdecimal':
					param._BIGDECIMAL("${k}","${v.description}")
					break
				case 'float':
					param._FLOAT("${k}","${v.description}")
					break
				case 'email':
					param._EMAIL("${k}","${v.description}")
					break
				case 'url':
					param._URL("${k}","${v.description}")
					break
			}

			/*
			 * Apply Rules
			 */
			
			
			// Mockdata Rule
			if(v.mockData){
				param.hasMockData("${v.mockData}")
			}
			
			// Roles Rule
			if(v?.roles){
				param.hasRoles(v.roles)
			}

			// Required Rule
			required = (booleans.contains(v?.required))?v.expose:((booleans.contains(actionRule?.required))?actionRule.required:required)
			param.isRequired(required)
			if(required){
				println("required : ${param.param.name}")
				if(param.param.roles){
					param.param.roles.each{ role ->
						apiParams.receives["${role}"] = param.toObject()
					}
				}else{
					apiParams.receives["permitAll"] = param.toObject()
				}
			}
			
			// Visible Rule
			visible = (booleans.contains(v?.visible))?v.expose:((booleans.contains(actionRule?.visible))?actionRule.required:visible)
			param.isVisible(visible)
			if(visible){
				//apiParams.returns.add(param.toObject())
				println("visible : ${param.param.name}")
				if(param.param.roles){
					param.param.roles.each{ role ->
						apiParams.returns["${role}"] = param.toObject()
					}
				}else{
					apiParams.returns["permitAll"] = param.toObject()
				}
			}
		}
		
		return apiParams
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
					println(api)
					if(json["${api.name().capitalize()}"]){
						def actionRule = (json["${api.name().capitalize()}"].RULES?."${actionname}")?json["${api.name().capitalize()}"].RULES."${actionname}":[:]
						apiParams = createApiParams(actionRule, json["${api.name().capitalize()}"].VALUES, api.name())
					}
					
					LinkedHashMap receives = apiParams?.receives
					LinkedHashMap returns = apiParams?.returns
println("${api.description()}")
println(receives)
println(returns)
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
