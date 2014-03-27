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
	
	private Map createReceivesReturns(String apiname, String actionname, JSONObject json){
		Map apiParams = [
			"receives":[],
			"returns":[]
		]
		List<ParamsDescriptor> receives = []
		List<ParamsDescriptor> returns = []

		ApiParams param = new ApiParams()
		def booleans = ['true','false']
		
		json["${apiname}"].VALUES.each{ k,v ->

			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			// Create Param (and edit rule defaults for keys)
			switch(v.type.toLowerCase()){
				case 'pkey':
					param._PKEY("${k}","${apiname}")
					break
				case 'fkey':
					param._FKEY("${k}","${apiname}")
					break
				case 'index':
					param._INDEX("${k}","${apiname}")
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

			ApiParams param2 = checkRules(param, apiname, actionname, json,k)

			// Required Rule
			if(param2.param.required){
				if(param2.param.roles){
					receives.putAt(receives.size(),param.toObject())
					param2.param.roles.each{ role ->
						if(apiParams?.receives["${role}"]){
							apiParams.receives["${role}"].add(receives)
					   }else{
						   apiParams.receives["${role}"] = []
						   apiParams.receives["${role}"].add(receives)
					   }
					}
				}else{
					receives.putAt(receives.size(),param.toObject())
					if(apiParams?.receives["permitAll"]){
						apiParams.receives["permitAll"].add(receives)
					}else{
						apiParams.receives["permitAll"] = []
						apiParams.receives["permitAll"].add(receives)
					}
				}
			}
			
			// Visible Rule

			if(param2.param.visible){
				//apiParams.returns.add(param.toObject())
				if(param2.param.roles){
					returns.putAt(returns.size(),param.toObject())
					param2.param.roles.each{ role ->
						if(apiParams?.returns["${role}"]){
							 apiParams.returns["${role}"].add(returns)
						}else{
							apiParams.returns["${role}"] = []
							apiParams.returns["${role}"].add(returns)
						}
					}
				}else{
					returns.putAt(returns.size(),param.toObject())
					if(apiParams?.returns["permitAll"]){
						apiParams.returns["permitAll"].add(returns)
					}else{
						apiParams.returns["permitAll"] = []
						apiParams.returns["permitAll"].add(returns)
					}
				}
			}else{
				returns.putAt(returns.size(),param.toObject())
				if(apiParams?.returns["permitAll"]){
					apiParams.returns["permitAll"].add(returns)
				}else{
					apiParams.returns["permitAll"] = []
					apiParams.returns["permitAll"].add(returns)
				}
			}
		}
		
		return apiParams
	}
	
	private ApiParams checkRules(ApiParams param,String apiname, String actionname, JSONObject json,String key){
		
		String hasDescription = ""
		boolean isRequired = false
		boolean isVisible = true
		String hasMockData = ""
		
		// get grails config variable data
		def type = grailsApplication.config.apitoolkit.apiobject.type."${param.param.paramType}"
		hasDescription = (type?.description)?type.description:hasDescription
		isRequired = (type?.required)?type.required:isRequired
		isVisible = (type?.visible)?type.visible:isVisible
		hasMockData = (type?.mockData)?type.mockData:hasMockData
		
		// get variable data
		def value = json["${apiname}"].VALUES.key
		hasDescription = (value?.description)?value.description:hasDescription
		isRequired = (value?.required)?value.required:isRequired
		isVisible = (value?.visible)?value.visible:isVisible
		hasMockData = (value?.mockData)?value.mockData:hasMockData
		
		// get model data
		
		// get method data
		def action = json["${apiname}"].RULES?.actionname?.key
		if(action){
			hasDescription = (action?.description)?action.description:hasDescription
			isRequired = (action?.required)?action.required:isRequired
			isVisible = (action?.visible)?action.visible:isVisible
			hasMockData = (action?.mockData)?action.mockData:hasMockData
		}
		
		// get grails config method data
		def method = grailsApplication.config.apitoolkit.apiobject.method.key
		if(method){
			hasDescription = (method?.description)?method.description:hasDescription
			isRequired = (method?.required)?method.required:isRequired
			isVisible = (method?.visible)?method.visible:isVisible
			hasMockData = (method?.mockData)?method.mockData:hasMockData
		}

		if(hasMockData){
			param.hasMockData("${hasMockData}")
		}
		
		if(hasDescription){
			param.hasDescription("${hasMockData}")
		}
		
		if(isVisible){
			param.isVisible(isRequired)
		}
		
		if(isRequired){
			param.isRequired(isRequired)
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
						apiParams = createReceivesReturns(apiname, actionname, json)
					}
					
					ParamsDescriptor[] receives = apiParams?.receives
					ParamsDescriptor[] returns = apiParams?.returns
					
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
