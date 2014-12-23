/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityService
//import grails.spring.BeanBuilder
//import grails.util.Holders as HOLDER


import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

//import java.lang.reflect.Method

import javax.servlet.forward.*

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse
import org.codehaus.groovy.grails.web.util.WebUtils
//import org.codehaus.groovy.grails.validation.routines.UrlValidator

import org.springframework.cache.Cache
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.context.request.RequestContextHolder as RCH
//import org.springframework.ui.ModelMap


import net.nosegrind.apitoolkit.*


class ApiLayerService{

	static transactional = false
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	ApiCacheService apiCacheService

	boolean chain = true
	boolean batch = true
	boolean localauth = true
	
	ApiStatuses errors = new ApiStatuses()
	
	void setEnv(){
		this.batch = grailsApplication.config.apitoolkit.batching.enabled
		this.chain = grailsApplication.config.apitoolkit.chaining.enabled
		this.localauth = grailsApplication.config.apitoolkit.localauth.enabled
	}
	
	private SecurityContextHolderAwareRequestWrapper getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}
	
	private GrailsContentBufferingResponse getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}
	
	boolean checkAuth(SecurityContextHolderAwareRequestWrapper request, List roles){
		try{
			boolean hasAuth = false
			roles.each{
				if(request.isUserInRole(it)){
					hasAuth = true
				}
			}
			return hasAuth
		}catch(Exception e) {
			throw new Exception("[ApiLayerService :: checkAuth] : Exception - full stack trace follows:"+e)
		}
	}
	
	List getContentType(String contentType){
		try{
			List tempType = contentType?.split(';')
			if(tempType){
				return tempType
			}else{
				return ['application/json']
			}
		}catch(Exception e) {
			throw new Exception("[ApiLayerService :: getContentType] : Exception - full stack trace follows:"+e)
		}
	}
	
	// set version,controller,action / controller,action
	List parseUri(String uri, String entrypoint){
		if(uri[0]=='/'){ uri=uri[1..-1] }
		List uriVars = uri.split('/')
		if(uriVars.size()==3){
			List temp2 = entrypoint.split('-')
			if(temp2.size()>1){
				// version
				uriVars[0] = temp2[1]
				return uriVars
			}else{
				uriVars.drop(1)
				return uriVars
			}
		}else{
			return uriVars
		}
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	boolean checkURIDefinitions(SecurityContextHolderAwareRequestWrapper request, LinkedHashMap requestDefinitions){
		try{
			List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
			List requestList = getApiParams(request, requestDefinitions)
			HashMap params = getMethodParams()
			
			//GrailsParameterMap params = RCH.currentRequestAttributes().params
			List paramsList = (request.method=='GET')?params.get.keySet() as List:params.post.keySet() as List
			paramsList.removeAll(optionalParams)
			if(paramsList.containsAll(requestList)){
				paramsList.removeAll(requestList)
				if(!paramsList){
					return true
				}
			}
			return false
		}catch(Exception e) {
			throw new Exception("[ApiLayerService :: checkURIDefinitions] : Exception - full stack trace follows:"+e)
		}
	}
	
	List getApiParams(SecurityContextHolderAwareRequestWrapper request, LinkedHashMap definitions){
		try{
			List apiList = []
			definitions.each{ key,val ->
				if(request.isUserInRole(key) || key=='permitAll'){
					val.each{ it ->
						if(it){
							apiList.add(it.name)
						}
					}
				}
			}
			return apiList
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: getApiParams] : Exception - full stack trace follows:"+e)
		}
	}
	
	HashMap getMethodParams(){
		try{
			boolean isChain = false
			List optionalParams = ['action','controller','apiName_v','contentType', 'encoding','apiChain', 'apiBatch', 'apiCombine', 'apiObject','apiObjectVersion', 'chain']
			SecurityContextHolderAwareRequestWrapper request = getRequest()
			GrailsParameterMap params = RCH.currentRequestAttributes().params
			Map paramsRequest = params.findAll {
				if(it.key=='apiChain'){ isChain=true }
				return !optionalParams.contains(it.key)
			}
			
			Map paramsGet = [:]
			Map paramsPost = [:]
			if(isChain){
				paramsPost = paramsRequest
			}else{
				paramsGet = WebUtils.fromQueryString(request.getQueryString() ?: "")
				paramsPost = paramsRequest.minus(paramsGet)
				if(paramsPost['id']){
					paramsGet['id'] = paramsPost['id']
					paramsPost.remove('id')
				}
			}
			return ['get':paramsGet,'post':paramsPost]
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: getMethodParams] : Exception - full stack trace follows:"+e)
		}
	}
	
	void setApiCache(String controllername,LinkedHashMap apidoc){
		apiCacheService.setApiCache(controllername,apidoc)
		apidoc.each(){ k1,v1 ->
			if(k1!='currentStable'){
				v1.each() { k2,v2 ->
					if(!['deprecated','defaultAction','domainPackage'].contains(k2)){
						def doc = generateApiDoc(controllername, k2, k1)
						apiCacheService.setApiDocCache(controllername,k2,k1,doc)
					}
				}
			}
		}
		def cache = apiCacheService.getApiCache(controllername)
	}
	
	Map generateApiDoc(String controllername, String actionname, String apiversion){
		try{
			Map doc = [:]
			def cache = apiCacheService.getApiCache(controllername)
			String apiPrefix = (grailsApplication.config.apitoolkit.apiName)?"${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}" as String:"v${grailsApplication.metadata['app.version']}" as String
			
			if(cache){
				String path = "/${apiPrefix}-${apiversion}/${controllername}/${actionname}"
				doc = ['path':"$path",'method':cache[apiversion][actionname]['method'],'description':cache[apiversion][actionname]['description']]
				if(cache[apiversion][actionname]['receives']){
	
					doc['receives'] = [:]
					for(receiveVal in cache[apiversion][actionname]['receives']){
						doc['receives']["$receiveVal.key"] = receiveVal.value
					}
				}
				
				if(cache[apiversion][actionname]['returns']){
					doc['returns'] = [:]
					for(returnVal in cache[apiversion][actionname]['returns']){
						doc['returns']["$returnVal.key"] = returnVal.value
					}
					doc['json'] = [:]
					doc['json'] = processJson(doc["returns"])
				}
				
				//if(cont["${actionname}"]["${apiversion}"]["errorcodes"]){
				//	doc["errorcodes"] = processDocErrorCodes(cont[("${actionname}".toString())][("${apiversion}".toString())]["errorcodes"] as HashSet)
				//}
	
			}
	
			return doc
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: generateApiDoc] : Exception - full stack trace follows:"+e)
		}
	}

	boolean checkDeprecationDate(String deprecationDate){
		try{
			def ddate = new SimpleDateFormat("MM/dd/yyyy").parse(deprecationDate)
			def deprecated = new Date(ddate.time)
			def today = new Date()
			if(deprecated < today ) {
				return true
			}
			return false
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: checkDeprecationDate] : Exception - full stack trace follows:"+e)
		}
	}
	
	/*
	 * TODO: Need to compare multiple authorities
	 */
	private String processJson(LinkedHashMap returns){
		try{
			def json = [:]
			returns.each{ p ->
					p.value.each{ it ->
						if(it){
							ParamsDescriptor paramDesc = it
						
							def j = [:]
							if(paramDesc?.values){
								j["$paramDesc.name"]=[]
							}else{
								String dataName=(['PKEY','FKEY','INDEX'].contains(paramDesc?.paramType?.toString()))?'ID':paramDesc.paramType
								j = (paramDesc?.mockData?.trim())?["$paramDesc.name":"$paramDesc.mockData"]:["$paramDesc.name":"$dataName"]
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
			}
	
			if(json){
				json = json as JSON
			}
			return json
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: processJson] : Exception - full stack trace follows:"+e)
		}
	}
	
	/*
	private ArrayList processDocErrorCodes(HashSet error){
		try{
			def errors = error as List
			def err = []
			errors.each{ v ->
				def code = ['code':v.code,'description':"${v.description}"]
				err.add(code)
			}
			return err
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: processDocErrorCodes] : Exception - full stack trace follows:"+e)
		}
	}
	*/
	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		try{
			SecurityContextHolderAwareRequestWrapper request = getRequest()
			GrailsParameterMap params = RCH.currentRequestAttributes().params
			String uri = request.forwardURI.split('/')[1]
			String api
			if(params.apiObject){
				api = (apiName)?"${params.apiName}_v${params.apiVersion}-${params.apiObject}" as String:"v${params.apiVersion}-${params.apiObject}" as String
			}else{
				api = (apiName)?"${params.apiName}_v${params.apiVersion}" as String:"v${params.apiVersion}" as String
			}
			return uri==api
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: isApiCall] : Exception - full stack trace follows:"+e)
		}
	}
	
	protected void setParams(SecurityContextHolderAwareRequestWrapper request,GrailsParameterMap params){
		try{
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
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: setParams] : Exception - full stack trace follows:"+e)
		}
	}
	
	/*
	 * Returns chainType
	 * 0 = blankchain
	 * 1 = prechain
	 * 2 = postchain
	 * 3 = illegal combination
	 */
	protected int checkChainedMethodPosition(LinkedHashMap cache,SecurityContextHolderAwareRequestWrapper request, GrailsParameterMap params, List uri, Map path){
		try{
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
			String methods = cache[params.apiObject][action]['method'].trim()

			if(methods=='GET'){
				if(methods != method && params?.apiChain?.type=='prechain'){
					preMatch = true
				}
			}else{
				if(methods == method){
					preMatch = true
				}
			}
	
			// postmatch check
			if(pathSize>=1){
				String last=path[keys[pathSize-1]]
				if(last && (last!='return' || last!='null')){
					List last2 = keys[pathSize-1].split('/')
					cache = apiCacheService.getApiCache(last2[0])
					methods = cache[params.apiObject][last2[1]]['method'].trim()

					if(methods=='GET'){
						if(methods != method && params?.apiChain?.type=='postchain'){
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
			

			// path check
			int start = 1
			int end = pathSize-2
			if(start<end){
				keys[0..(pathSize-1)].each{ val ->
					if(val){
						List temp2 = val.split('/')
						cache = apiCacheService.getApiCache(temp2[0])
						methods = cache[params.apiObject][temp2[1]]['method'].trim() as List
						if(methods=='GET'){
							if(methods != method && params?.apiChain?.type=='blankchain'){
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
				if(params?.apiChain?.type=='blankchain'){
					return 0
				}else{
					return 3
				}
			}else{
				if(preMatch){
					setParams(request,params)
					return 1
				}else if(postMatch){
					setParams(request,params)
					return 2
				}
			}

			if(params?.apiChain?.type=='blankchain'){
				return 0
			}else{
				return 3
			}
		}catch(Exception e){
			throw new Exception("[ApiLayerService :: checkChainedMethodPosition] : Exception - full stack trace follows:"+e)
		}
	}
	
}
