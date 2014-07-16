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
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService
import grails.spring.BeanBuilder
import grails.util.Holders as HOLDER

import java.util.ArrayList
import java.util.HashSet
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

import java.lang.reflect.Method
import javax.servlet.forward.*
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse
import com.linkedin.grails.profiler.ProfilerFilterResponse

import org.codehaus.groovy.grails.web.util.WebUtils
import org.codehaus.groovy.grails.validation.routines.UrlValidator

import org.springframework.cache.Cache
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.ui.ModelMap

import org.springframework.ui.ModelMap

import net.nosegrind.apitoolkit.*


class ApiResponseService extends ApiLayerService{

	static transactional = false
	
	//def grailsApplication
	//SpringSecurityService springSecurityService
	
	//ApiCacheService apiCacheService
	//ApiStatuses errors = new ApiStatuses()
	//GrailsCacheManager grailsCacheManager
	
	boolean handleApiChain(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsContentBufferingResponse response, Map model, GrailsParameterMap params){
		List keys = params?.apiChain?.order.keySet() as List
		List uri = [params.controller,params.action,params.id]
		def newModel = (model)?convertModel(model):model
		ApiStatuses errors = new ApiStatuses()
		
		List uri2 = keys[0].split('/')
		String controller = uri2[0]
		String action = uri2[1]
		Long id = newModel.id
		
		if(keys[0] && (params?.apiChain?.order["${keys[0]}"]='null' && params?.apiChain?.order["${keys[0]}"]!='return')){
			int pos = checkChainedMethodPosition(request,params,uri,params?.apiChain?.order as Map)
			if(pos==3){
				String msg = "[ERROR] Bad combination of unsafe METHODS in api chain."
				errors._403_FORBIDDEN(msg).send()
				return false
			}else{
				if(!uri2){
					String msg = "Path was unable to be parsed. Check your path variables and try again."
					//redirect(uri: "/")
					errors._404_NOT_FOUND(msg).send()
					return false
				}

				def currentPath = "${controller}/${action}"
				def methods = cache["${action}"]["${params.apiObject}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
				def method = (methods.contains(request.method))?request.method:null

				boolean hasAuth = false
				List roles = cache["${action}"]["${params.apiObject}"]['roles'].toArray() as List
				roles.each{
					if(request.isUserInRole(it)){
						hasAuth = true
					}
				}
				if(hasAuth){
					if(params?.apiChain.combine=='true'){
						params.apiCombine["${params.controller}/${params.action}"] = parseURIDefinitions(newModel,cache["${params.action}"]["${params.apiObject}"]['returns'])
					}
					params.controller = controller
					params.action = action
					params.id = newModel.id
					
					params.apiChain.order.remove("${keys[0]}")
					
					if(params?.apiChain.combine=='true'){
						params.apiCombine[currentPath] = parseURIDefinitions(newModel,cache["${params.action}"]["${params.apiObject}"]['returns'])
					}
				}else{
					String msg = "User does not have access."
					errors._403_FORBIDDEN(msg).send()
					return false
				}
			}
		}
		return true
	}
	
	def handleApiResponse(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, ProfilerFilterResponse response, ModelMap model, GrailsParameterMap params){
		try{
			String type = ''
			if(cache){
				if(cache["${params.action}"]["${params.apiObject}"]){
					// make 'application/json' default
					def formats = ['text/html','text/json','application/json','text/xml','application/xml']
					type = (params.contentType)?formats.findAll{ type.startsWith(it) }[0].toString():params.contentType
					if(type){
							def newModel = convertModel(model)
							response.setHeader('Authorization', cache["${params.action}"]["${params.apiObject}"]['roles'].join(', '))
							LinkedHashMap result = parseURIDefinitions(newModel,cache["${params.action}"]["${params.apiObject}"]['returns'])
							if(params?.apiChain?.combine=='true'){
								if(!params.apiCombine){ params.apiCombine = [:] }
								String currentPath = "${params.controller}/${params.action}"
								params.apiCombine[currentPath] = result
							}
							return result
					}else{
						//return true
						//render(view:params.action,model:model)
					}
				}else{
					//return true
					//render(view:params.action,model:model)
				}
			}
		}catch(Exception e){
			log.error("[ApiToolkitService :: handleApiResponse] : Exception - full stack trace follows:", e);
		}
	}
	
	GrailsParameterMap getParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		List formats = ['text/html','application/json','text/xml','application/xml']
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
		GrailsParameterMap params = RCH.currentRequestAttributes().params
		String uri = request.forwardURI.split('/')[1]
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String api
		if(params.apiObject){
			api = (apiName)?"${apiName}_v${apiVersion}-${params.apiObject}" as String:"v${apiVersion}-${params.apiObject}" as String
		}else{
			api = (apiName)?"${apiName}_v${apiVersion}" as String:"v${apiVersion}" as String
		}
		return uri==api
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	LinkedHashMap parseURIDefinitions(LinkedHashMap model,LinkedHashMap responseDefinitions){
		ApiStatuses errors = new ApiStatuses()
		String msg = "Error. Invalid variables being returned. Please see your administrator"
		List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
		List responseList = getApiParams(responseDefinitions)
		
		HashMap params = getMethodParams()
		//GrailsParameterMap params = RCH.currentRequestAttributes().params
		List paramsList = model.keySet() as List
		paramsList.removeAll(optionalParams)
		if(!responseList.containsAll(paramsList)){
			paramsList.removeAll(responseList)
			paramsList.each(){ it ->
				model.remove("${it}".toString())
			}
			if(!paramsList){
				errors._400_BAD_REQUEST(msg).send()
				return [:]
			}else{
				return model
			}
		}else{
			return model
		}
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
	
	void callHook(String service, String state, Map data, String apiversion) {
		send(data, state, apiversion, service)
	}
	
	void callHook(String service,String state, Object data, String apiversion) {
		data = formatDomainObject(data)
		send(data, state, apiversion, service)
	}
	
	private boolean send(Map data, String state, String apiversion, String service) {
		List hooks = grailsApplication.getClassForName(grailsApplication.config.apitoolkit.domain).findAll("from Hook where service='${service}/${state}'")
		def cache = apiCacheService.getApiCache(service)
		hooks.each { hook ->
			// get cache and check each users authority for hook
			List userRoles = []
			LinkedHashSet authorities = hook.user.getAuthorities()
			authorities.each{
				userRoles += it.authority
			}
			def roles= cache["${state}"]["${apiversion}"]['roles']
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
		def uri = HOLDER.getServletContext().getControllerActionUri(request)
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
				if(cache["${params.action}"]["${params.apiObject}"]){

					def doc = cache["${params.action}"]["${params.apiObject}"].doc
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

					
					return newDoc
				}
			}
		}
		return [:]
	}
	
	Map convertModel(Map map){
		Map newMap = [:]
		String k = map?.entrySet()?.toList()?.first()?.key
		
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
