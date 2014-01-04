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
import net.nosegrind.apitoolkit.ApiDescriptor;
import net.nosegrind.apitoolkit.ParamsDescriptor;
import net.nosegrind.apitoolkit.ErrorCodeDescriptor;
import net.nosegrind.apitoolkit.ApiErrors;
import net.nosegrind.apitoolkit.ApiParams;

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
	
	private boolean checkDocAuthority(HashSet role){
		List roles = role as List
		if(roles.size()>0 && roles[0].trim()){
			def roles2 = grailsApplication.getDomainClass(grailsApplication.config.grails.plugins.springsecurity.authority.className).clazz.list().authority
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
			return true
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
							hookData = (data as XML).toString()
							queryString << "state=${state}&xml=${hookData}"
							break
						case 'json':
						default:
							hookData = (data as JSON).toString()
							queryString << "state=${state}&json=${hookData}"
							break
					}

					def writer = new OutputStreamWriter(conn.outputStream)
					writer.write(queryString)
					writer.flush()
					writer.close()
					conn.connect()
					String output = conn.content.text
					conn.close()
				}catch(Exception e){
					hook.attempts+=1
					hook.save()
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
	
	private ArrayList processDocValues(HashSet value){
		def values = value as List
		List val2 = []
		values.each{ v ->
			Map val = [:]
			if((v.roles && checkDocAuthority(v.roles)) || !v.roles){
				val = [
					"paramType":"${v.paramType}",
					"name":"${v.name}",
					"description":"${v.description}"
				]
				
				if(v.paramType=='PKEY' || v.paramType=='FKEY'){
					val["idReferences"] = "${v.idReferences}"
				}
		
				if(v.required==false){
					val["required"] = false
				}
				if(v.mockData){
					val["mockData"] = "${v.mockData}"
				}
				if(v.values){
					val["values"] = processDocValues(v.values)
				}
				val2.add(val)
			}
		}
		return val2
	}
	
	private ArrayList processDocErrorCodes(HashSet error){
		def errors = error as List
		def err = []
		errors.each{ v ->
			def code = ['code':v.code,'description':"${v.description}"]
			err.add(code)
		}
		return err
	}
	
	private Map processJson(ArrayList returns){
		def json = [:]
		returns.each{ p ->
			def j = [:]
			if(p?.values){
				j["${p.name}"]=[]
			}else{
				j = (p?.mockData?.trim())?["${p.name}":"${p.mockData}"]:["${p.name}":"${grailsApplication.config.apitoolkit.defaultData.(p.paramType)}"]
			}
			j.each(){ key,val ->
				if(val instanceof List){
					def child = [:]
					val.each(){ it ->
						it.each(){ key2,val2 ->
							child["${key2}"] ="${val2}"
						}
					}
					json["${key}"] = child
				}else{
					json["${key}"]=val
				}
			}
		}
		return json
	}
	
	Map generateApiDoc(String controllername, String actionname){
		Map doc = [:]
		
		def cont = apiCacheService.getApiCache(controllername)
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		cont.each{
			for (Method method : controller.getClazz().getDeclaredMethod(it.key)){
				if(method.isAnnotationPresent(Api)) {
					def action = method.getName()
					if(cont[("${action}".toString())]){
						String path = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/JSON/${controllername}/${action}"
						doc[("${action}".toString())] = ["path":"${path}","method":cont[("${action}".toString())]["method"],"description":cont[("${action}".toString())]["description"]]
						
						if(cont["${action}"]["receives"]){
							doc[("${action}".toString())]["receives"] = processDocValues(cont[("${action}".toString())]["receives"] as HashSet)
						}
						
						if(cont["${action}"]["returns"]){
							doc[("${action}".toString())]["returns"] = processDocValues(cont[("${action}".toString())]["returns"] as HashSet)
							doc[("${action}".toString())]["json"] = processJson(doc[("${controllername}".toString())][("${action}".toString())]["returns"])
						}
						
						if(cont["${action}"]["errorcodes"]){
							doc[("${action}".toString())]["errorcodes"] = processDocErrorCodes(cont[("${action}".toString())]["errorcodes"] as HashSet)
						}

						if(doc[("${action}".toString())]["json"]){
							def json = doc[("${action}".toString())]["json"] as JSON
							json = json.toString().replaceAll("\\{\n","\\{<br><div style='padding-left:2em;'>")
							json = json.toString().replaceAll("}"," </div>}<br>")
							json = json.toString().replaceAll(",",",<br>")
							doc[("${action}".toString())]["json"] = json
						}
					}
				}else{
					// ERROR: method at '${controllername}/${actionname}' does not have API annotation
				}
			}
		}

		return doc
	}
}
