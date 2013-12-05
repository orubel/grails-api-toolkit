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
	def flushApiCache(String controllername){
		// flush and reset
		// setApiCache()
		setApiCache(controllername)
	} 
	
	//@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(controllername,method){
		def apiOutput = []
		def inc = 0

		if(controllername!='aclClass'){
			
			def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
			//def methods = controller?.getClazz().metaClass.methods*.name.sort().unique()
			for (Method method : controller.getClazz().getMethods()){
					if(method.isAnnotationPresent(Api)) {
						def temp = getApiCache(controllername,method)
					}
			}
		}
	}
	
	String getBelongsTo(String paramType, String controller, String belongsTo){
		return (paramType=='PKey')?controller:belongsTo
	}
	
	@Cacheable(value="ApiCache",key="#controllername")
	def getApiCache(String controllername, Method method) {
		def action = method.getName().toString()
		def api = method.getAnnotation(Api)

		def apiList = ["${controllername}":["${action}":["api":[
						"requestMethod":"${api.method()}",
						"description":"${api.description()}",
						"receives":[],
						"returns":[],
						"errors":[]
					]
				]
			]
		]
		
		// RECEIVES
		api.receives().each{ p ->
			if (p.paramType()) {
				def belongsTo = getBelongsTo(p.paramType().toString(), controllername, p.belongsTo().toString())
				def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",mockData:"${p.mockData()}",belongsTo:"${belongsTo}",roles:[],required:"${p.required()}",values:[]]
				
				if(p?.values()){
					def params2 = p.values()
					def values = []
					params2.each{ p2 ->
						if (p2.paramType()) {
							def belongsTo2 = getBelongsTo(p2.paramType().toString(), controllername, p2.belongsTo().toString())
							def pm2 = [type:"${p2.paramType()}",name:"${p2.name()}",description:"${p2.description()}",mockData:"${p2.mockData()}",belongsTo:"${belongsTo2}",roles:[],required:"${p2.required()}"]
							values.add(pm2)
						}
					}
					list.values.add(values)
				}
				apiList.get("${controllername}").get("${action}")["api"]["receives"].push(list)
			}
		}

		
		// RETURNS
		api.returns().each{ p ->
			if (p.paramType()) {
				def belongsTo = getBelongsTo(p.paramType().toString(), controllername, p.belongsTo().toString())
				def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",mockData:"${p.mockData()}",belongsTo:"${belongsTo}",roles:[],required:"${p.required()}",values:[]]
				
				if(p?.values()){
					def values = []
					p.values().each{ p2 ->
						if (p2.paramType()) {
							def belongsTo2 = getBelongsTo(p2.paramType().toString(), controllername, p2.belongsTo().toString())
							def pm2 = [type:"${p2.paramType()}",name:"${p2.name()}",description:"${p2.description()}",mockData:"${p2.mockData()}",belongsTo:"${belongsTo2}",roles:[],required:"${p2.required()}"]
							values.add(pm2)
						}
					}
					list.values.add(values)
				}
				apiList.get("${controllername}").get("${action}")["api"]["returns"].push(list)
			}
		}
		
		// ERRORS
		api.errors().each{ p ->
			if (p.code()) {
				def list = [code:"${p.code()}",description:"${p.description()}"]
				apiList.get("${controllername}").get("${action}")["api"]["errors"].push(list)
			}
		}

		//println("apioutput >> ${apiList}")
		return (apiList)?apiList:null
	}
}
