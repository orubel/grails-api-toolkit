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
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	Map createApiParams(Map methodRule, Map actionRule, JSONObject values, String controllername){
		Map apiParams = [
			'receives':[],
			'returns':[]
		]
		ApiParams param = new ApiParams()
		
		values.each{ k,v ->
			boolean expose = true
			boolean required = false
			boolean visible = true
			
			v.type = (v.key)?getKeyType(v.key, v.type):v.type
			println "${k} -> ${v}"

			// Create Param (and edit rule defaults for keys)
			switch(v.type.toLowerCase()){
				case 'pkey':
					param._PKEY("${v.key}","${v.description}","${controllername}")
					required = "true"
					visible = "false"
					break
				case 'fkey':
					param._FKEY("${v.key}","${v.description}","${controllername}")
					visible = "false"
					break
				case 'index':
					param._INDEX("${v.key}","${v.description}","${controllername}")
					visible = "false"
					break
				case 'long':
					param._LONG("${v.key}","${v.description}")
					expose = "true"
					break
				case 'string':
					param._STRING("${v.key}","${v.description}")
					expose = "true"
					break
				case 'boolean':
					param._BOOLEAN("${v.key}","${v.description}")
					expose = "true"
					break
				case 'bigdecimal':
					param._BIGDECIMAL("${v.key}","${v.description}")
					expose = "true"
					break
				case 'float':
					param._FLOAT("${v.key}","${v.description}")
					expose = "true"
					break
				case 'email':
					param._EMAIL("${v.key}","${v.description}")
					expose = "true"
					break
				case 'url':
					param._URL("${v.key}","${v.description}")
					expose = "true"
					break
			}

			/*
			 * Apply Rules
			 */
			def booleans = ['true','false']
			
			// Mockdata Rule
			if(v.mockData){
				param.hasMockData("${v.mockData}")
			}
			
			// Roles Rule
			if(v?.roles){
				param.hasRoles(v.roles)
			}
			
			// Expose/ExposeToService Rule
			expose = (booleans.contains(v?.expose))?v.expose:((booleans.contains(actionRule?.expose))?actionRule.expose:((booleans.contains(methodRule?.expose))?methodRule.expose:true))
			param.exposeToService(expose)

			// Required Rule
			required = (booleans.contains(v?.required))?v.expose:((booleans.contains(actionRule?.required))?actionRule.required:((booleans.contains(methodRule?.required))?methodRule.required:false))
			param.isRequired(required)
			
			// Visible Rule
			visible = (booleans.contains(v?.visible))?v.expose:((booleans.contains(actionRule?.visible))?actionRule.required:((booleans.contains(methodRule?.visible))?methodRule.visible:true))
			param.isVisible(visible)
			
			ParamsDescriptor paramObject = param.toObject()
			if(required){
				apiParams.receives.add(paramObject)
			}
			if(visible){
				apiParams.returns.add(paramObject)
			}
		}
		
		return apiParams
	}
	
	def initApiCache(){
		JSONObject json = readObjectFile()
		//println(json)
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
					if(json["${controllername.capitalize()}"]){
						def methodRule = (json["${controllername.capitalize()}"].rules?.methods?."${api.method()}")?json["${controllername.capitalize()}"].rules.methods."${api.method()}":grailsApplication.config.apitoolkit.rules."${api.method()}"
						def actionRule = (json["${controllername.capitalize()}"].rules?.actions?."${actionname}")?json["${controllername.capitalize()}"].rules.actions."${actionname}":[:]
						apiParams = createApiParams(methodRule, actionRule, json["${controllername.capitalize()}"].values, controllername)
					}
					
					ApiDescriptor service = new ApiDescriptor(
						"method":"${api.method()}",
						"description":"${api.description()}",
						"doc":[:],
						"receives":(apiParams?.receives)?apiParams?.receives:[],
						"returns":(apiParams?.returns)?apiParams?.returns:[]
					)

					service['roles'] = api.roles()
					
					methods["${actionname}"] = service
				}
			}
			if(methods){
				apiCacheService.setApiCache("${controllername}".toString(),methods)
			}
		}
	}
}
