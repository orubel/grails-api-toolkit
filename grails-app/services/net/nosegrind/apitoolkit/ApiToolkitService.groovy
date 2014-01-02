package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.lang.reflect.Method
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import net.nosegrind.apitoolkit.*


class ApiToolkitService{

	def grailsApplication
	def springSecurityService
	def apiCacheService

	static transactional = false

	Long responseCode
	String responseMessage
	
	def getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}

	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	def getParams(){
		def params = RCH.currentRequestAttributes().params
		def request = getRequest()
		def json = request.JSON
		json.each() { key,value ->
			params[key] = value
		}
		return params
	}
	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		def request = getRequest()
		def params = getParams()
		def queryString = request.'javax.servlet.forward.query_string'
		
		def uri
		if(request.isRedirected()){
			if(params.action=='index'){
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI+'/'+params.action
			}else{
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
			}
		}else{
			uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
		}
		
		def api
		if(grailsApplication.config.grails.app.context=='/'){
			api = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/"
		}else if(grailsApplication.config?.grails?.app?.context){
			api = "${grailsApplication.config.grails.app.context}/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/"
		}else if(!grailsApplication.config?.grails?.app?.context){
			api = "/${grailsApplication.metadata['app.name']}/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/"
		}
		api += (params?.format)?"${params.format}/${params.controller}/${params.action}":"JSON/${params.controller}/${params.action}"
		api += (params.id)?"/${params.id}":""
		api += (queryString)?"?${queryString}":""

		//println("${uri}==${api}")
		return uri==api
	}

	boolean isRequestMatch(String protocol){
		def request = getRequest()
		return request.method.toString()==protocol.toString()
	}
	
	Integer getKey(String key){
		switch(key){
			case'FKEY':
				return 2
				break
			case 'PKEY':
				return 1
				break
			default:
				return 0
		}
	}
	
	/*
	 * Which annos declare this KEY as being 'received'.
	 * Check first in own controller then walk all others
	 */
	String createLinkRelationships(String paramType,String name,String controllername){
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		//def methods = controller?.getClazz().metaClass.methods*.name.sort().unique()
		for (Method method : controller.getClazz().getMethods()){
				if(method.isAnnotationPresent(Api)) {}
		}
	}
	
	Map formatModel(Object data){
		def newMap = [:]
		if(data && (!data?.response && !data?.metaClass && !data?.params)){
			data.each{ key, value ->
				if(value){
					if(grailsApplication.isDomainClass(value.getClass())){
						newMap[key]=value
					}else{
						if(value in java.util.Collection){
							if(value?.size()>0){
								if(grailsApplication.isDomainClass(value[0].getClass())){
									value.each{ k,v ->
										newMap[key][v.id]= v
									}
								}else{
									value = formatModel(value)
									newMap[key]= value
								}
							}
						}else{
							newMap[key]=value.toString()
						}
					}
				}else{
					//println("no value")
				}
			}
		}
		return newMap
	}

	Map formatDomainObject(Object data){
		def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass","maps"]
		def newMap = [:]
		data.getProperties().each { key, val ->
			if (!nonPersistent.contains(key)) {
				if(grailsApplication.isDomainClass(val.getClass())){
					newMap.put key, val.id
				}else{
					newMap.put key, val
				}
			}
		}
		return newMap
	}
	
	boolean validateUrl(String url){
		String[] schemes = ["http","https"]
		UrlValidator urlValidator = new UrlValidator(schemes)
		return urlValidator.isValid(url)
	}
	
	boolean checkHookAuthority(ArrayList roles){
		if (springSecurityService.isLoggedIn()){
			def userRoles = springSecurityService.getPrincipal().getAuthorities()
			if(userRoles){
				if(userRoles.intersect(roles)){
					return true
				}
			}
		}
		return false
	}
	
	boolean checkAuthority(ArrayList role){
		List roles = role as List
		if(roles.size()>0 && roles[0].trim()){
			def roles2 = grailsApplication.getDomainClass(grailsApplication.config.grails.plugin.springsecurity.authority.className).clazz.list().authority
			def finalRoles
			def userRoles
			if (springSecurityService.isLoggedIn()){
				userRoles = springSecurityService.getPrincipal().getAuthorities()
			}
			
			if(userRoles){
				def temp = roles2.intersect(roles as Set)
				finalRoles = temp.intersect(userRoles)
				if(finalRoles){
					return true
				}else{
					return false
				}
			}else{
				return false
			}
		}else{
			return false
		}
	}
	
	void callHook(String service, Map data, String state) {
		send(data, state, service)
	}
	
	void callHook(String service, Object data, String state) {
		data = formatDomainObject(data)
		send(data, state, service)
	}
	
	boolean methodCheck(List roles){
		def optionalMethods = ['OPTIONS','HEAD']
		def requiredMethods = ['GET','POST','PUT','DELETE','PATCH','TRACE']
		
		def temp = roles.removeAll(optionalMethods)
		if(requiredMethods.contains(temp)){
			if(temp.size()>1){
				// ERROR: too many non-optional methods; only one is permitted
				return false
			}
		}else{
			// ERROR: unrecognized method
			return false
		}
		return true
	}
	
	private boolean send(Map data, String state, String service) {
		def hooks = grailsApplication.getClassForName(grailsApplication.config.apitoolkit.domain).findAll("from Hook where service='${service}/${state}'")
		def cache = apiCacheService.getApiCache(service)
		
		hooks.each { hook ->
			// get cache and check each users authority for hook
			def userRoles = []
			def authorities = hook.user.getAuthorities()
			authorities.each{
				userRoles += it.authority
			}
			def roles= cache["${state}"]['hookRoles']
			def temp = roles.intersect(userRoles)
			if(temp.size()>0){
				String format = hook.format.toLowerCase()
				if(hook.attempts>=grailsApplication.config.apitoolkit.attempts){
					data = 	[message:'Number of attempts exceeded. Please reset hook via web interface']
				}
				String hookData
	
				try{
					def conn = hook.callback.toURL().openConnection()
					conn.setRequestMethod("POST")
					conn.doOutput = true
					def queryString = []
					switch(format){
						case 'xml':
							conn.setRequestProperty("Content-Type", "application/xml;charset=UTF-8")
							hookData = (data as XML).toString()
							queryString << "state=${state}&xml=${hookData}"
							break
						case 'json':
						default:
							conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
							hookData = (data as JSON).toString()
							queryString << "state=${state}&json=${hookData}"
							break
					}
					def writer = new OutputStreamWriter(conn.outputStream)
					writer.write(queryString)
					writer.flush()
					writer.close()
					conn.connect()
					if(conn.content.text!='connected'){
						hook.attempts+=1
						hook.save(flush: true)
						log.info("[Hook] net.nosegrind.apitoolkit.ApiToolkitService : Could not connect to ${hook.url}")
					}
				}catch(Exception e){
					hook.attempts+=1
					hook.save(flush: true)
					log.info("[Hook] net.nosegrind.apitoolkit.ApiToolkitService : " + e)
				}
			}else{
				hook.delete(flush:true)
			}
		}
	}
	
	/*
	 * called on detection of apicall; default headers for api calls
	 * regardless of success or failure
	 */
	void setApiHeaders(){
		
	}
	
	boolean isRequestRedirected(){
		if(request.getAttribute(GrailsApplicationAttributes.REDIRECT_ISSUED) != null){
			return true
		}else{
			return false
		}
	}
	
	List getRedirectParams(){
		// params.controller = temp[0]
		// params.action = temp[1]
		def uri = SCH.servletContext.getControllerActionUri(request)
		return uri[1..(uri.size()-1)].split('/')

	}
}
