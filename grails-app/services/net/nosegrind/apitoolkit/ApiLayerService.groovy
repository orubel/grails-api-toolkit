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

import org.codehaus.groovy.grails.web.json.JSONObject
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

import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse
import org.codehaus.groovy.grails.web.util.WebUtils

class ApiLayerService{

	def grailsApplication
	def springSecurityService
	def apiCacheService
	
	static transactional = false
	
	ApiStatuses errors = new ApiStatuses()
	GrailsCacheManager grailsCacheManager
	
	// DEPRECATED
	private SecurityContextHolderAwareRequestWrapper getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}
	
	private GrailsContentBufferingResponse getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}
	
	boolean handleApiRequest(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params){
		// SET CONTENTTYPE FOR THIS REQUEST
		if(!params.contentType){
			List content = getContentType(request)
			params.contentType = content[0]
			params.encoding = (content.size()>1)?content[1]:null
			
			switch(params.contentType){
				case 'text/json':
				case 'application/json':
					if(request.JSON?.chain){
						params.apiChain = request.JSON.chain
					}
					break
				case 'text/xml':
				case 'application/xml':
					if(request.XML?.chain){
						params.apiChain = request.XML?.chain
					}
					break
			}
		}
		
		// CHECK IF URI HAS CACHE
		if(cache["${params.action}"]){
			// CHECK IF PRINCIPAL HAS ACCESS TO API
			if(!checkAuthority(cache["${params.action}"]['roles'])){
				return false
			}
			
			// CHECK METHOD FOR API CHAINING. DOES METHOD MATCH?
			def method = cache["${params.action}"]['method']?.trim()
			def uri = [params.controller,params.action,params.id]
			// DOES api.methods.contains(request.method)
			if(!isRequestMatch(method,request.method.toString())){
				// check for apichain

				
				// TEST FOR CHAIN PATHS
				if(params?.apiChain){
					int pos = checkChainedMethodPosition(request, params,uri,params?.apiChain?.order as Map)
					if(pos==3){
						ApiStatuses error = new ApiStatuses()
						String msg = "[ERROR] Bad combination of unsafe METHODS in api chain."
						error._400_BAD_REQUEST(msg)?.send()
						return false
					}else{
						return true
					}
				}else{
					return true
				}
			}else{
				// (NON-CHAIN) CHECK WHAT TO EXPECT; CLEAN REMAINING DATA
				// RUN THIS CHECK AFTER MODELMAP FOR CHAINS
				if(!checkURIDefinitions(cache["${params.action}"]['receives'])){
					ApiStatuses error = new ApiStatuses()
					String msg = 'Expected request variables do not match sent variables'
					error._400_BAD_REQUEST(msg)?.send()
					return false
				}else{
					return true
				}
			}
		}
	}
	
	boolean handleApiChain(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsContentBufferingResponse response, Map model, GrailsParameterMap params){
		List keys = params?.apiChain?.order.keySet() as List
		def uri = [params.controller,params.action,params.id]
		
		def newModel = (model)?convertModel(model):model
		
		List uri2 = keys[0].split('/')
		String controller = uri2[0]
		String action = uri2[1]
		Long id = newModel.id
		
		if(keys[0] && (params?.apiChain?.order["${keys[0]}"]='null' && params?.apiChain?.order["${keys[0]}"]!='return')){
			int pos = checkChainedMethodPosition(request,params,uri,params?.apiChain?.order as Map)
			if(pos==3){
				log.info("[ERROR] Bad combination of unsafe METHODS in api chain.")
				return false
			}else{
				if(!uri2){
					String msg = "Path was unable to be parsed. Check your path variables and try again."
					//redirect(uri: "/")
					errors._404_NOT_FOUND(msg).send()
					return false
				}

				def currentPath = "${controller}/${action}"

				def methods = cache["${action}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
				def method = (methods.contains(request.method))?request.method:null

				if(checkAuthority(cache["${action}"]['roles'])){
					params.controller = controller
					params.action = action
					params.id = newModel.id
					
					params.apiChain.order.remove("${keys[0]}")
				}else{
					String msg = "User does not have access."
					errors._403_FORBIDDEN(msg).send()
					return false
				}

			}

		}
		return true
	}
	
	def handleApiResponse(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsContentBufferingResponse response, Map model, GrailsParameterMap params){
		String type = params.contentType

		if(cache){
			if(cache["${params.action}"]){

				
				// make 'application/json' default
				def formats = ['text/html','application/json','application/xml']
				type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null

				if(type){
						def newModel = convertModel(model)
						response.setHeader('Authorization', cache["${params.action}"]['roles'].join(', '))
						return newModel
				}else{
					//return true
					//render(view:params.action,model:model)
				}
			}else{
				//return true
				//render(view:params.action,model:model)
			}
		}

	}
	
	List getContentType(SecurityContextHolderAwareRequestWrapper request){
		def tempType = request.getHeader('Content-Type')?.split(';')
		String contentType = (tempType)?tempType[0]:request.getHeader('Content-Type')
		return tempType
	}
	
	GrailsParameterMap getParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		List formats = ['text/html','application/json','application/xml']
		List tempType = request.getHeader('Content-Type')?.split(';')
		String type = (tempType)?tempType[0]:request.getHeader('Content-Type')
		type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
		switch(type){
			case 'application/json':
				request.JSON?.each() { key,value ->
					params.put(key,value)
				}
				break
			case 'application/xml':
				request.XML?.each() { key,value ->
					params.put(key,value)
				}
				break
		}
		return params
	}
	
	void setParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		List formats = ['text/json','application/json','text/xml','application/xml']
		List tempType = request.getHeader('Content-Type')?.split(';')
		String type = (tempType)?tempType[0]:request.getHeader('Content-Type')
		type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
		
		switch(type){
			case 'application/json':
				request.JSON?.each() { key,value ->
					params.put(key,value)
				}
				break
			case 'application/xml':
				request.XML?.each() { key,value ->
					params.put(key,value)
				}
				break
		}
	}
	
	boolean isChain(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		switch(params.contentType){
			case 'text/xml':
			case 'application/xml':
				if(request.XML?.chain){
					return true
				}
				break
			case 'text/json':
			case 'application/json':
			default:
				if(request.JSON?.chain){
					return true
				}
				break
		}
		return false
	}
	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		SecurityContextHolderAwareRequestWrapper request = getRequest()
		String uri = request.forwardURI.split('/')[1]
		String api = (grailsApplication.config.apitoolkit.apiName)?"${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String:"v${grailsApplication.metadata['app.version']}" as String
		return uri==api
	}
	
	HashMap getMethodParams(){
		List optionalParams = ['action','controller','apiName_v','newPath','queryString']
		SecurityContextHolderAwareRequestWrapper request = getRequest()
		GrailsParameterMap params = RCH.currentRequestAttributes().params
		Map paramsRequest = params.findAll {
			return !optionalParams.contains(it.key)
		}
		
		Map paramsGet = WebUtils.fromQueryString(request.getQueryString() ?: "")
		Map paramsPost = paramsRequest.minus(paramsGet)

		return ['get':paramsGet,'post':paramsPost]
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	boolean checkURIDefinitions(LinkedHashMap requestDefinitions){
		String authority = springSecurityService.principal.authorities*.authority[0]
		ParamsDescriptor[] temp = (requestDefinitions["${authority}"])?requestDefinitions["${authority}"]:requestDefinitions["permitAll"]
		List requestList = []
		List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'chain']
		temp.each{
			requestList.add(it.name)
		}

		HashMap params = getMethodParams()
		//GrailsParameterMap params = RCH.currentRequestAttributes().params
		List paramsList = params.post.keySet() as List
		paramsList.removeAll(optionalParams)
		if(paramsList.containsAll(requestList)){
			paramsList.removeAll(requestList)
			if(!paramsList){
				return true
			}
		}
		return false
	}
	
	boolean isRequestMatch(String protocol,String method){
		if(['OPTIONS','TRACE','HEAD'].contains(method)){
			return true
		}else{
			if(protocol == method){
				return true
			}else{
				return false
			}
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
			List userRoles = springSecurityService.getPrincipal().getAuthorities()
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
				List roles2 = grailsApplication.getDomainClass(grailsApplication.config.grails.plugin.springsecurity.authority.className).clazz.list().authority
				List finalRoles = []
				List userRoles = []
				if (springSecurityService.isLoggedIn()){
					userRoles = springSecurityService.getPrincipal().getAuthorities() as List
				}
				
				if(userRoles){
					List temp = roles2.intersect(roles as Set)
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
	
	boolean methodCheck(List roles){
		List optionalMethods = ['OPTIONS','HEAD']
		List requiredMethods = ['GET','POST','PUT','DELETE','PATCH','TRACE']
		
		List temp = roles.removeAll(optionalMethods)
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
	
	void callHook(String service, Map data, String state) {
		send(data, state, service)
	}
	
	void callHook(String service, Object data, String state) {
		data = formatDomainObject(data)
		send(data, state, service)
	}
	
	private boolean send(Map data, String state, String service) {
		List hooks = grailsApplication.getClassForName(grailsApplication.config.apitoolkit.domain).findAll("from Hook where service='${service}/${state}'")
		def cache = apiCacheService.getApiCache(service)
		hooks.each { hook ->
			// get cache and check each users authority for hook
			List userRoles = []
			LinkedHashSet authorities = hook.user.getAuthorities()
			authorities.each{
				userRoles += it.authority
			}
			def roles= cache["${state}"]['roles']
			List temp = roles.intersect(userRoles)

			if(temp.size()>0){
				String format = hook.format.toLowerCase()
				if(hook.attempts>=grailsApplication.config.apitoolkit.attempts){
					data = 	[message:'Number of attempts exceeded. Please reset hook via web interface']
				}
				String hookData
	
				try{
					URL url = new URL(hook.callback)
					URLConnection conn = url.openConnection()
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
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	private String processJson(LinkedHashMap returns){
		def json = [:]
		returns.each{ p ->
			if(springSecurityService.principal.authorities*.authority.any { p.key }){
				p.value.each{ it ->
					ParamsDescriptor paramDesc = it
				
					def j = [:]
					if(paramDesc?.values){
						j["${paramDesc.name}"]=[]
					}else{
						String dataName=(['PKEY','FKEY','INDEX'].contains(paramDesc.paramType.toString()))?'ID':paramDesc.paramType
						j = (paramDesc?.mockData?.trim())?["${paramDesc.name}":"${paramDesc.mockData}"]:["${paramDesc.name}":"${dataName}"]
					}
					j.each(){ key,val ->
						if(val instanceof List){
							def child = [:]
							val.each(){ it2 ->
								it2.each(){ key2,val2 ->
									child["${key2}"] ="${val2}"
								}
							}
							json["${key}"] = child
						}else{
							json["${key}"]=val
						}
					}
				}
			}
		}

		if(json){
			json = json as JSON
		}
		return json
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	LinkedHashMap getApiDoc(GrailsParameterMap params){
		LinkedHashMap newDoc = [:]
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
		if(controller){
			def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
			if(cache){
				if(cache["${params.action}"]){

					def doc = cache["${params.action}"].doc
					def path = doc?.path
					def method = doc?.method
					def description = doc?.description

					def authority = springSecurityService.principal.authorities*.authority[0]
					newDoc["${params.action}"] = ["path":"${path}","method":method,"description":"${description}"]
					if(doc.receives){
						newDoc["${params.action}"].receives = [:]
						doc.receives.each{ it ->
							if(authority==it.key || it.key=='permitAll'){
								newDoc["${params.action}"].receives["${it.key}"] = it.value
							}
						}
					}

					if(doc.returns){
						newDoc["${params.action}"].returns = [:]
						doc.returns.each(){ v ->
							if(authority==v.key || v.key=='permitAll'){
								newDoc["${params.action}"].returns["${v.key}"] = v.value
							}
						}

						newDoc["${params.action}"].json = processJson(newDoc["${params.action}"].returns)
					}

					if(doc.errorcodes){
						doc.errorcodes.each{ it ->
							newDoc["${params.action}"].errorcodes.add(it)
						}
					}

					/*
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
					*/
					
					return newDoc
				}
			}
		}
		return [:]
	}
	
	Map generateApiDoc(String controllername, String actionname){
		Map doc = [:]
		def cont = apiCacheService.getApiCache(controllername)
		String apiPrefix = (grailsApplication.config.apitoolkit.apiName)?"${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String:"v${grailsApplication.metadata['app.version']}" as String
		
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
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	def Map generateDoc(String controllerName, String actionName){
		GrailsParameterMap params = getParams()
		
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
	
	List getPath(GrailsParameterMap params, String queryString){
		List path = []
		def newQuery = []
		if(params.containsKey("newPath")){
			if(params.newPath){
				path = params.newPath?.split('&')
			}else{
				path = (queryString)?queryString.split('&'):[]
			}
		}else{
			path = (queryString)?queryString.split('&'):[]
		}
		return path
	}
	
	/*
	 * Returns chainType
	 * 1 = prechain
	 * 2 = postchain
	 * 3 = illegal combination
	 */
	int checkChainedMethodPosition(SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params, List uri, Map path){
		boolean preMatch = false
		boolean postMatch = false
		boolean pathMatch = false

		List keys = path.keySet() as List
		Integer pathSize = keys.size()
		
		String controller = uri[0]
		String action = uri[1]
		Long id = uri[2]
		
		// prematch check
		String method = net.nosegrind.apitoolkit.Method["${request.method.toString()}"].toString()
		def cache = apiCacheService.getApiCache(controller)
		//def methods = cache["${uri[1]}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
		def methods = cache["${action}"]['method'].trim()

		if(method=='GET'){
			if(methods != method){
				preMatch = true
			}
		}else{
			if(methods == method){
				preMatch = true
			}
		}
		println("[pre] "+preMatch)
		
		// postmatch check
		if(pathSize>=1){
			def last=path[keys[pathSize-1]]
			if(last && last!='return'){
				List last2 = keys[pathSize-1].split('/')
				cache = apiCacheService.getApiCache(last2[0])
				methods = cache["${last2[1]}"]['method'].trim()
				if(method=='GET'){
					if(methods != method){
						postMatch = true
					}
				}else{
					if(methods == method){
						postMatch = true
					}
				}
			}else{
				postMatch = true
			}
		}
		println("[post] "+postMatch)
		
		// path check
		int start = 1
		int end = pathSize-2
		if(start<end){
			keys[0..(pathSize-1)].each{ val ->
				if(val){
					List temp2 = val.split('/')
					cache = apiCacheService.getApiCache(temp2[0])
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
		println("[path] "+pathMatch)
		
		if(pathMatch || (preMatch && postMatch)){
			return 3
		}else{
			if(preMatch){
				setParams(request,params)
				return 1
			}else if(postMatch){
				setParams(request,params)
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
	

	def setApiCache(String controllername,LinkedHashMap apidoc){
		apiCacheService.setApiCache(controllername,apidoc)
		//def cache = grailsCacheManager.getCache('ApiCache').get(controllername).get()

		apidoc.each{ k,v ->
			if(v){
				def doc = generateApiDoc(controllername, k)
				apiCacheService.setApiDocCache(controllername,k,doc)
			}
		}
	}
	
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	def apiRoles(List list) {
		if(springSecurityService.principal.authorities*.authority.any { list.contains(it) }){
			return true
		}
		return ['validation.customRuntimeMessage', 'ApiCommandObject does not validate. Check that your data validates or that requesting user has access to api method and all fields in api command object.']
	}

}
