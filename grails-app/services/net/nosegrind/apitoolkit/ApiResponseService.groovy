/* ****************************************************************************
 * Copyright 2014 Owen Rubel
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
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.web.util.WebUtils
import org.codehaus.groovy.grails.validation.routines.UrlValidator

import org.springframework.cache.Cache
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.ui.ModelMap

import org.springframework.ui.ModelMap

import net.nosegrind.apitoolkit.*


class ApiResponseService extends ApiLayerService{

	static transactional = false
	
	boolean handleApiChain(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, GrailsContentBufferingResponse response, Map model, GrailsParameterMap params){
		try{
			List uri = [params.controller,params.action,params.id]
			ApiStatuses errors = new ApiStatuses()
			List keys = []
			String controller
			String action
			List uri2 = []
			
			if(params?.apiChain?.order!='null'){
				keys = params?.apiChain?.order.keySet() as List
				uri2 = keys.last().split('/')
				controller = uri2[0]
				action = uri2[1]
			}
			
			Long id = model.id

			
			//if(keys.last() && (params?.apiChain?.order["${keys.last()}"]!='null' && params?.apiChain?.order["${keys.last()}"]!='return')){
			if(keys.last()){
				int pos = checkChainedMethodPosition(cache,request,params,uri,params?.apiChain?.order as Map)
				if(pos==3){
					String msg = '[ERROR] Bad combination of unsafe METHODS in api chain.'
					errors._403_FORBIDDEN(msg).send()
					return false
				}else{
					if(!uri2){
						String msg = 'Path was unable to be parsed. Check your path variables and try again.'
						errors._404_NOT_FOUND(msg).send()
						return false
					}
					
					def currentPath = "${controller}/${action}"
					List roles = cache[params.apiObject][params.action]['roles'].toArray() as List
					if(checkAuth(request,roles)){
						/*
						if(params?.apiChain.combine=='true'){
							params.apiCombine["${params.controller}/${params.action}"] = parseURIDefinitions(model,cache[params.action][params.apiObject]['returns'])
						}
						*/
						params.controller = controller
						params.action = action

						if(params.apiChain.key){
							params.id = model[params.apiChain.key]
							params.apiChain.remove('key')
						}else{
							params.id = model.id
						}

						if(params?.apiChain.combine=='true'){
							params.apiCombine[currentPath] = parseURIDefinitions(request,model,cache[params.apiObject][params.action]['returns'])
						}
						
						if(keys.last() && (params?.apiChain?.order["${keys.last()}"]=='null' && params?.apiChain?.order["${keys.last()}"]=='return')){
							params.remove('apiChain')
						}
						params?.apiChain?.order.remove("$currentPath")
						return true
					}else{
						String msg = "User does not have access."
						errors._403_FORBIDDEN(msg).send()
						return false
					}
				}
			}
			//}else{
			//	params.remove("apiChain")
			//}

			return false
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: handleApiChain] : Exception - full stack trace follows:"+e)
		}
	}
	
	def handleApiResponse(LinkedHashMap cache, SecurityContextHolderAwareRequestWrapper request, HttpServletResponse response, LinkedHashMap model, GrailsParameterMap params){
		try{
			String type = ''
			if(cache){
				if(cache[params.apiObject][params.action]){
					// make 'application/json' default
					//def formats = ['text/html','text/json','application/json','text/xml','application/xml']
					//type = (params.contentType)?formats.findAll{ type.startsWith(it) }[0].toString():params.contentType
					//if(type){
							response.setHeader('Authorization', cache[params.apiObject][params.action]['roles'].join(', '))
							LinkedHashMap result = parseURIDefinitions(request,model,cache[params.apiObject][params.action]['returns'])
							if(params?.apiChain?.combine=='true'){
								if(!params.apiCombine){ params.apiCombine = [:] }
								String currentPath = "${params.controller}/${params.action}"
								params.apiCombine[currentPath] = result
							}
							return result
					//}else{
						//return true
						//render(view:params.action,model:model)
					//}
				}else{
					//return true
					//render(view:params.action,model:model)
				}
			}
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: handleApiResponse] : Exception - full stack trace follows:"+e)
		}
	}
	
	GrailsParameterMap getParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		try{
			List formats = ['text/json','application/json','text/xml','application/xml']
			List tempType = getContentType(request.getHeader('Content-Type'))
			String type = (tempType)?tempType[0]:request.getHeader('Content-Type')
			type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
			switch(type){
				case 'text/json':
				case 'application/json':
					request.JSON?.each() { key,value ->
						params.put(key,value)
					}
					break
				case 'text/xml':
				case 'application/xml':
					request.XML?.each() { key,value ->
						params.put(key,value)
					}
					break
			}
			return params
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: getParams] : Exception - full stack trace follows:"+e)
		}
	}
	
	boolean isChain(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		try{
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
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: isChain] : Exception - full stack trace follows:"+e)
		}
	}
	

	LinkedHashMap parseURIDefinitions(SecurityContextHolderAwareRequestWrapper request, LinkedHashMap model,LinkedHashMap responseDefinitions){
		try{
			ApiStatuses errors = new ApiStatuses()
			String msg = 'Error. Invalid variables being returned. Please see your administrator'
			List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
			List responseList = getApiParams(request,responseDefinitions)

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
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: parseURIDefinitions] : Exception - full stack trace follows:"+e)
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
	
	boolean isRequestRedirected(){
		if(request.getAttribute(GrailsApplicationAttributes.REDIRECT_ISSUED) != null){
			return true
		}else{
			return false
		}
	}
	
	List getRedirectParams(){
		def uri = HOLDER.getServletContext().getControllerActionUri(request)
		return uri[1..(uri.size()-1)].split('/')
	}
	
	private ArrayList processDocValues(List<ParamsDescriptor> value){
		List val2 = []
		value.each{ v ->
			Map val = [:]
			val = [
				'paramType':v.paramType,
				'name':v.name,
				'description':v.description
			]
			
			if(v.paramType=='PKEY' || v.paramType=='FKEY'){
				val["idReferences"] = v.idReferences
			}
	
			if(v.required==false){
				val['required'] = false
			}
			if(v.mockData){
				val['mockData'] = value.mockData
			}
			if(v.values){
				val['values'] = processDocValues(v.values)
			}
			if(v.roles){
				val['roles'] = v.roles
			}
			val2.add(val)
		}
		return val2
	}
	
	private ArrayList processDocErrorCodes(HashSet error){
		List errors = error as List
		ArrayList err = []
		errors.each{ v ->
			def code = ['code':v.code,'description':v.description]
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
						j[paramDesc.name]=[]
					}else{
						String dataName=(['PKEY','FKEY','INDEX'].contains(paramDesc.paramType.toString()))?'ID':paramDesc.paramType
						j = (paramDesc?.mockData?.trim())?["${paramDesc.name}":paramDesc.mockData]:["${paramDesc.name}":dataName]
					}
					j.each(){ key,val ->
						if(val instanceof List){
							def child = [:]
							val.each(){ it2 ->
								it2.each(){ key2,val2 ->
									child[key2] = val2
								}
							}
							json[key] = child
						}else{
							json[key]=val
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
				if(cache[params.apiObject][params.action]){

					def doc = cache[params.apiObject][params.action].doc
					def path = doc?.path
					def method = doc?.method
					def description = doc?.description

					def authority = springSecurityService.principal.authorities*.authority[0]
					newDoc[params.action] = ['path':path,'method':method,'description':description]
					if(doc.receives){
						newDoc[params.action].receives = [:]
						doc.receives.each{ it ->
							if(authority==it.key || it.key=='permitAll'){
								newDoc[params.action].receives[it.key] = it.value
							}
						}
					}

					if(doc.returns){
						newDoc[params.action].returns = [:]
						doc.returns.each(){ v ->
							if(authority==v.key || v.key=='permitAll'){
								newDoc[params.action].returns[v.key] = v.value
							}
						}

						newDoc[params.action].json = processJson(newDoc[params.action].returns)
					}

					if(doc.errorcodes){
						doc.errorcodes.each{ it ->
							newDoc[params.action].errorcodes.add(it)
						}
					}
					
					return newDoc
				}
			}
		}
		return [:]
	}
	
	Map convertModel(Map map){
		try{
			Map newMap = [:]
			String k = map?.entrySet()?.toList()?.first()?.key
			if(map && (!map?.response && !map?.metaClass && !map?.params)){
				if(grailsApplication.isDomainClass(map[k].getClass())){
					newMap = formatDomainObject(map[k])
					return newMap
				}else{
					switch(map[k].getClass()){
						case 'class java.util.LinkedList':
						case 'class java.util.ArrayList':
							map[k].eachWithIndex(){ val, key ->
								if(val){
									if(grailsApplication.isDomainClass(val.getClass())){
										newMap[key]=formatDomainObject(val)
									}else{
										newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || val in java.util.Map)?val:val.toString()
									}
								}
							}
							return newMap
							break
						case 'class java.util.Map':
						case 'class java.util.LinkedHashMap':
						default:
							map[k].each(){ key, val ->
								if(val){
									if(grailsApplication.isDomainClass(val.getClass())){
										newMap[key]=formatDomainObject(val)
									}else{
										newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || val in java.util.Map)?val:val.toString()
									}
								}
							}
							return newMap
							break
					}
				}
			}
			return newMap
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: convertModel] : Exception - full stack trace follows:"+e)
		}
	}

	Map formatDomainObject(Object data){
		try{
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
		}catch(Exception e){
			throw new Exception("[ApiResponseService :: formatDomainObject] : Exception - full stack trace follows:"+e)
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
