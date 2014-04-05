/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import java.util.ArrayList
import java.util.HashSet
import java.util.Map
import javax.servlet.forward.*

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import net.nosegrind.apitoolkit.*

import org.springframework.ui.ModelMap

import grails.plugin.cache.GrailsCacheManager
import org.springframework.cache.Cache

import grails.spring.BeanBuilder

class ApiToolkitService{

	def grailsApplication
	def springSecurityService
	def apiCacheService
	ApiStatuses error = new ApiStatuses()
	GrailsCacheManager grailsCacheManager
	
	static transactional = false

	Long responseCode
	String responseMessage
	
	def getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}

	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	GrailsParameterMap getParams(){
		GrailsParameterMap params = RCH.currentRequestAttributes().params
		def request = getRequest()
		List formats = ['text/html','application/json','application/xml']
		List tempType = request.getHeader('Content-Type')?.split(';')
		String type = (tempType)?tempType[0]:request.getHeader('Content-Type')
		type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
		switch(type){
			case 'application/json':
				def json = request.JSON
				json.each() { key,value ->
					params.put(key,value)
				}
				break
			case 'application/xml':
				def xml = request.JSON
				xml.each() { key,value ->
					params.put(key,value)
				}
				break
		}
		return params
	}

	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		def request = getRequest()
		GrailsParameterMap params = getParams()
		String queryString = request.'javax.servlet.forward.query_string'

		String uri
		if(request.isRedirected()){
			if(params.action=='index'){
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI+'/'+params.action
			}else{
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
			}
		}else{
			uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
		}
		
		String apiPrefix
		if(grailsApplication.config.apitoolkit.apiName){
			apiPrefix = "${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String
		}else{
			apiPrefix = "v${grailsApplication.metadata['app.version']}" as String
		}

		String api
		Map type = ['XML':'text/xml','JSON':'application/json','HTML':'application/html'].findAll{ request.getHeader('Content-Type')?.startsWith(it.getValue()) }

		if(grailsApplication.config.grails.app.context=='/'){
			api = "/${apiPrefix}/" as String
		}else if(grailsApplication.config?.grails?.app?.context){
			api = "/${grailsApplication.config.grails.app.context}/${apiPrefix}/" as String
		}else if(!grailsApplication.config?.grails?.app?.context){
			api = "/${grailsApplication.metadata['app.name']}/${apiPrefix}/" as String
		}

		api += "${params.controller}/${params.action}" as String
		api += (params.id)?"/${params.id}" as String:""
		api += (queryString)?"?${queryString}" as String:""

		return uri==api
	}

	boolean isRequestMatch(String protocol){
		def request = getRequest()
		String method = net.nosegrind.apitoolkit.Method["${request.method.toString()}"].toString()
		if(protocol == method){
			return true
		}else{
			return false
		}
		return false
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
		if(roles.size()==1 && roles[0]=='permitAll'){
			return true
		}else{
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
	}
	
	void callHook(String service, Map data, String state) {
		send(data, state, service)
	}
	
	void callHook(String service, Object data, String state) {
		data = formatDomainObject(data)
		send(data, state, service)
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
			def roles= cache["${state}"]['roles']
			def temp = roles.intersect(userRoles)

			if(temp.size()>0){
				String format = hook.format.toLowerCase()
				if(hook.attempts>=grailsApplication.config.apitoolkit.attempts){
					data = 	[message:'Number of attempts exceeded. Please reset hook via web interface']
				}
				String hookData
	
				try{
					def url = new URL(hook.callback)
					def conn = url.openConnection()
					conn.setRequestMethod("POST")
					conn.setRequestProperty("User-Agent",'Mozilla/5.0')
					conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
					conn.setDoOutput(true)
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
					
					def writer = new OutputStreamWriter(conn.getOutputStream())
					writer.write(queryString)
					writer.flush()
					writer.close()
					String output = conn.content.text
					conn.connect()
				}catch(Exception e){
					// ignore missing GSP/JSP exception
					if(!(e in java.io.FileNotFoundException)){
						hook.attempts+=1
						hook.save()
						log.info("[Hook] net.nosegrind.apitoolkit.ApiToolkitService : " + e)
					}
				}
			}else{
				hook.delete(flush:true)
			}
		}
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
	
	private ArrayList processDocValues(List<ParamsDescriptor> value){
		List val2 = []
		value.each{ v ->
			Map val = [:]
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
				val["mockData"] = "${value.mockData}"
			}
			if(v.values){
				val["values"] = processDocValues(v.values)
			}
			if(v.roles){
				val["roles"] = v.roles
			}
			val2.add(val)
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
	
	private String processJson(ArrayList returns){
		def json = [:]
		returns.each{ p ->
			def j = [:]
			if(p?.values){
				j["${p.name}"]=[]
			}else{
				def dataName=(['PKEY','FKEY','INDEX'].contains(p.paramType.toString()))?'ID':p.paramType
				j = (p?.mockData?.trim())?["${p.name}":"${p.mockData}"]:["${p.name}":"${dataName}"]
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
		if(json){
			json = json as JSON
		}
		return json
	}
	
	Map getApiDoc(){
		def params = getParams()
		def newDoc = [:]
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
		if(controller){
			def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
			if(cache){
				if(cache["${params.action}"]){

					def doc = cache["${params.action}"].doc
					def path = doc["${params.action}"].path
					def method = doc["${params.action}"].method
					def description = doc["${params.action}"].description
					
					newDoc["${params.action}"] = ["path":"${path}","method":method,"description":"${description}"]
					if(doc["${params.action}"].receives){
						newDoc["${params.action}"].receives = doc["${params.action}"].receives
					}
			
					if(doc["${params.action}"].returns){
						newDoc["${params.action}"].returns = []
						doc["${params.action}"].returns.each(){ v ->
							if(springSecurityService.principal.authorities*.authority.any { v.roles.contains(it) }){
								newDoc["${params.action}"].returns.add(v)
							}
						}
						newDoc["${params.action}"].json = processJson(newDoc.returns)
					}
					
					if(doc["${params.action}"].errorcodes){
						newDoc["${params.action}"].errorcodes = doc["${params.action}"].errorcodes
					}
					
					def links = generateLinkRels(params.controller, params.action,doc)
					if(links){
						newDoc["${params.action}"].links = []
						links.each(){ role ->
							role.each(){ v ->
								if(springSecurityService.principal.authorities*.authority.any { v.key }){
									newDoc["${params.action}"].links.add(v.value)
								}
							}
						}
						newDoc["${params.action}"].links.unique()
					}

					return newDoc
				}
			}
		}
		return [:]
	}
	
	Map generateApiDoc(String controllername, String actionname){
		Map doc = [:]
		def cont = apiCacheService.getApiCache(controllername)
		String apiPrefix
		if(grailsApplication.config.apitoolkit.apiName){
			apiPrefix = "${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String
		}else{
			apiPrefix = "v${grailsApplication.metadata['app.version']}" as String
		}
		
		if(cont){
			def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
			for (Method method : controller.getClazz().getDeclaredMethod(actionname)){
				if(method.isAnnotationPresent(Api)) {
					
					String path = "/${apiPrefix}/${controllername}/${actionname}"
					doc = ["path":"${path}","method":cont[("${actionname}".toString())]["method"],"description":cont[("${actionname}".toString())]["description"]]
					
					// if(springSecurityService.principal.authorities*.authority.any { receiveVal.key==it }){
					if(cont["${actionname}"]["receives"]){
						doc["receives"] = [:]
						for(receiveVal in cont["${actionname}"]["receives"]){
							doc["receives"]["${receiveVal.key}"] = receiveVal.value
						}
					}
					
					if(cont["${actionname}"]["returns"]){
						doc["returns"] = [:]
						for(returnVal in cont["${actionname}"]["returns"]){
							println("returnval [${returnVal.key}] : ${returnVal}")
							doc["returns"]["${returnVal.key}"] = returnVal.value
						}
					}
					
					if(cont["${actionname}"]["errorcodes"]){
						doc["errorcodes"] = processDocErrorCodes(cont[("${actionname}".toString())]["errorcodes"] as HashSet)
					}
					//List links = generateLinkRels(controllername,actionname,doc)
					//doc["links"] = links
				}else{
					// ERROR: method at '${controllername}/${actionname}' does not have API annotation
				}
			}
		}

		return doc
	}
	
	def Map generateDoc(String controllerName, String actionName){
		def newDoc = [:]
		String authority = springSecurityService.principal.authorities*.authority[0]
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllerName)
		def cache = (params.controller)?apiCacheService.getApiCache(controllerName):null
		if(cache["${actionName}"]?.doc){
			def doc = cache["${actionName}"].doc
			def path = doc.path
			def method = doc.method
			def description = doc.description
			
			newDoc["${actionName}"] = ["path":"${path}","method":method,"description":"${description}"]
			if(doc.receives){
				if(!newDoc["${actionName}"].receives){
					newDoc["${actionName}"].receives = []
				}
				if(doc.receives?."${authority}"){
					doc.receives["${authority}"].each{ it ->
						newDoc["${actionName}"].receives.add(it)
					}
				}else{
					doc.receives["permitAll"].each{ it ->
						newDoc["${actionName}"].receives.add(it)
					}
				}
			}
	
			if(doc.returns){
				if(!newDoc["${actionName}"].returns){
					newDoc["${actionName}"].returns = []
				}
				if(doc.returns?."${authority}"){
					doc.returns["${authority}"].each{ it ->
						newDoc["${actionName}"].returns.add(it)
					}
				}else{
					doc.returns["permitAll"].each{ it ->
						newDoc["${actionName}"].returns.add(it)
					}
				}
				newDoc["${actionName}"].json = doc.json
			}
			
			if(doc.errorcodes){
				newDoc["${actionName}"].errorcodes = doc.errorcodes
			}
			
			/*
			def links = generateLinkRels(controllerName, actionName,doc)
			if(links){
				newDoc["${actionName}"].links = []
				links.each(){ role ->
					role.each(){ v ->
						if(springSecurityService.principal.authorities*.authority.any { v.key }){
							newDoc["${actionName}"].links.add(v.value)
						}
					}
				}
				
				newDoc["${actionName}"].links.unique()
			}
			*/
		}
		return newDoc
	}
	
	/*
	List generateLinkRels(String controllerName, String actionName,Map apidoc){
		List links = []
		int inc = 0

		def receives = apidoc.receives
		def path = apidoc.path
		if(receives){
			receives.each() { param ->
				def paramType = param?.paramType
				if(paramType){
					switch(paramType){
						case 'PKEY':
							String uri = "${path}/[ID]?null=${param?.name}"
							def endChains = getPostChainUri(controllerName,actionName,uri)
							endChains.each(){ end ->
								links.add(inc,end)
								inc++
							}
							break
						case 'FKEY':
							String uri = "${path}/[ID]?null=${param?.name}"
							def endChains = getBlankChainUri(controllerName,actionName,uri)
							endChains.each(){ end ->
								links.add(inc,end)
								inc++
							}
							break
					}
				}
			}
		}

		return links
	}
	*/
	
	private List getBlankChainUri(String controllername,String actionname,String uri){
		def paths = []
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		Map methods = [:]
		controller.getClazz().methods.each { Method method ->
			String action = method.getName()
			if(action!=actionname){
				if(method.isAnnotationPresent(Api)) {
					def api = method.getAnnotation(Api)
					if(api.method() == 'GET'){
						def roles = api.apiRoles() as List
						roles.each(){
							if(!path["${it}"]){
								path["${it}"] = [:]
							}
							path["${it}"] = "${uri}&${controllername}/${action}=return"
						}
						paths.add(path)
					}
				}
			}
		}
		return paths
	}
	
	private List getPostChainUri(String controllername,String actionname,String uri){
		def paths = []
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		Map methods = [:]
		Map path = [:]
		controller.getClazz().methods.each { Method method ->
			String action = method.getName()
			if(action!=actionname){
				if(method.isAnnotationPresent(Api)) {
					def api = method.getAnnotation(Api)
					if(api.method() == 'POST' || api.method() == 'PUT' || api.method() == 'DELETE'){
						def roles = api.roles() as List
						roles.each(){
							if(!path["${it}"]){
								path["${it}"] = [:]
							}
							path["${it}"] = "${uri}&${controllername}/${action}=return"
						}
						paths.add(path)
					}
				}
			}
		}
		return paths
	}
	
	Map isChainedApi(Map map,List path){
		println(map)
		println(path)
		def pathSize = path.size()
		//String uri = ''
		Map uri = [:]
		for (int i = 0; i < path.size(); i++) {
			if(path[i]){
				def val=path[i]
				def temp = val.split('=')
				String pathKey = temp[0]
				String pathVal = (temp.size()>1)?temp[1]:null

				if(pathKey=='null'){
					pathVal = pathVal.split('/').join('.')
						if(map["${pathVal}"]){
							if(map["${pathVal}"] in java.util.Collection){
								map = map["${pathVal}"]
							}else{
								if(map["${pathVal}"].toString().isInteger()){
									if(i==(pathSize-1)){
										def newMap = ["${pathVal}":map["${pathVal}"]]
										map = newMap
									}else{
										params.id = map["${pathVal}"]
									}
								}else{
									def newMap = ["${pathVal}":map["${pathVal}"]]
									map = newMap
								}
							}
						}else{
							return uri
						}
				}else{
					if(!uri['id']){
						uri['id'] = map["${pathVal}"]
					}
					uri['controller'] = pathKey.split('/')[0]
					uri['action'] = pathKey.split('/')[1]
					if(i+1<=path.size()-1){
						uri['params'] = [:]
						path[i+1..path.size()-1].each{
							 def tmp = it.split('=')
							 uri['params'][tmp[0]] = tmp[1]
						}
					}
					return uri
					break
				}
			}
		}
	}
	
	/*
	 * Returns chainType
	 * 1 = prechain
	 * 2 = postchain
	 * 3 = illegal combination
	 */
	int checkChainedMethodPosition(List uri,List path){
		boolean preMatch = false
		boolean postMatch = false
		boolean pathMatch = false
		Long pathSize = path.size()
		
		// prematch check
		def request = getRequest()
		String method = net.nosegrind.apitoolkit.Method["${request.method.toString()}"].toString()
		def cache = apiCacheService.getApiCache(uri[0])
		//def methods = cache["${uri[1]}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
		def methods = cache["${uri[1]}"]['method'].trim()
		if(method=='GET'){
			if(!methods == method){ preMatch = true }
		}else{
			if(methods == method){ preMatch = true }
		}
		
		// postmatch check
		if(pathSize>1){
			def last=path.last()?.split('=')
			if(last[0] && last[0]!='null'){
				List last2 = last[0].split('/')
				cache = apiCacheService.getApiCache(last2[0])
				//methods = cache["${last2[1]}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
				methods = cache["${last2[1]}"]['method'].trim()
				if(method=='GET'){
					if(!methods == method){ postMatch = true }
				}else{
					if(methods == method){ postMatch = true }
				}
			}
		}
		
		// path check
		int start = 1
		int end = pathSize-2
		if(start<end){
			path(start..end).each{ val ->
				if(val){
					List temp=val.split('=')
					List temp2 = temp[0].split('/')
					cache = apiCacheService.getApiCache(temp2[0])
					//methods = cache["${temp2[1]}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
					methods = cache["${temp2[1]}"]['method'].trim() as List
					if(method=='GET'){
						if(methods != method){ 
							pathMatch = true 
						}
					}else{
						if(methods == method){ 
							pathMatch = true 
						}
					}
				}
			}
		}
		
		if(pathMatch || (preMatch && postMatch)){
			return 3
		}else{
			if(preMatch){
				return 1
			}else if(postMatch){
				return 2
			}
		}
		
		// path but but no position
		// could be trying to make api call and url encode data
		// if so, not restful; does not comply
		return 3

	}
	
	Map convertModel(Map map){
		Map newMap = [:]
		String k = map.entrySet().toList().first().key
		
		if(map && (!map?.response && !map?.metaClass && !map?.params)){
			if(grailsApplication.isDomainClass(map[k].getClass())){
				newMap = formatDomainObject(map[k])
			}else{
				map[k].each{ key, val ->
					if(val){
						if(grailsApplication.isDomainClass(val.getClass())){
							newMap[key]=formatDomainObject(val)
						}else{
							if(val in java.util.ArrayList || val in java.util.List){
								newMap[key] = val
							}else if(val in java.util.Map){
								newMap[key]= val
							}else{
								newMap[key]=val.toString()
							}
						}
					}
				}
			}
		}
		return newMap
	}

	Map formatDomainObject(Object data){
		def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass","maps"]
		def newMap = [:]
		
		if(data?.id){
			newMap['id'] = data.id
		}
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
	
	def setApiCache(String controllername,Map apidoc){
		apiCacheService.setApiCache(controllername,apidoc)
		//def cache = grailsCacheManager.getCache('ApiCache').get(controllername).get()

		apidoc.each{ k,v ->
			if(v){
				def doc = generateApiDoc(controllername, k)
				apiCacheService.setApiDocCache(controllername,k,doc)
				//cache["${k}"]['doc'] = generateApiDoc(controllername, k,doc)
			}
		}
	}
	
	
	/*
	 * Validates Api Command > apiRoles
	 */
	def apiRoles(List list) {
		if(springSecurityService.principal.authorities*.authority.any { list.contains(it) }){
			return true
		}
		return ['validation.customRuntimeMessage', 'ApiCommandObject does not validate. Check that your data validates or that requesting user has access to api method and all fields in api command object.']
	}
}