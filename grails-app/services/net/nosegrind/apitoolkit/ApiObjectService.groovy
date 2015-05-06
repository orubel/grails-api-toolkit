/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import org.codehaus.groovy.grails.web.json.JSONObject

import java.lang.reflect.Method

import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import org.codehaus.groovy.grails.commons.GrailsApplication;

import grails.util.Holders
import grails.converters.JSON
import grails.converters.XML
import groovy.util.ConfigObject
import grails.util.Environment

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService

import org.springframework.cache.Cache
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH

import net.nosegrind.apitoolkit.*


class ApiObjectService{

	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	ApiLayerService apiLayerService
	//ApiToolkitService apiToolkitService
	GrailsCacheManager grailsCacheManager
	ApiCacheService apiCacheService
	
	static transactional = false
	
	public initialize(){
		try {
			// if serverType = master, parse iostate, hook
			
			if(grailsApplication.config.apitoolkit.serverType=='master'){
				String ioPath
				if(grailsApplication.isWarDeployed()){
					ioPath = Holders.servletContext.getRealPath('/')
					if(Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST){
						ioPath += 'WEB-INF/classes/iostate'
					}else{
						// test in Environment.PRODUCTION
						ioPath += 'WEB-INF/classes/iostate'
					}
				}else{
					ioPath = grails.util.BuildSettingsHolder.settings?.resourcesDir?.path
					if(Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST){
						ioPath += '/iostate'
					}else{
						// test in Environment.PRODUCTION
						ioPath += '/iostate'
					}
					
				}
				parseFiles(ioPath)
			}
			
			String apiObjectSrc = grailsApplication.config.apitoolkit.iostate.preloadDir.toString()
			parseFiles(apiObjectSrc)
		} catch (Exception e) {
			throw new Exception("[ApiObjectService :: initialize] : Exception - full stack trace follows:",e)
		}
	}
	
	public updateIoState(String file){

	}
	
	private Map parseFile(JSONObject json){
		String apiObjectName = json.NAME.toString()
		parseJson(json.NAME.toString(),json)
	}
	
	private parseFiles(String path){
		println("### Importing iostate : "+path)
		new File(path).eachFile() { file ->
			def tmp = file.name.toString().split('\\.')
			if(tmp[1]=='json'){
				println("... parsing iostate for file : "+file.name)
				try{
					JSONObject json = JSON.parse(file.text)
					parseJson(json.NAME.toString(),json)
					//def cache = apiCacheService.getApiCache(apiName)
				}catch(Exception e){
					throw new Exception("[ApiObjectService :: initialize] : Unacceptable file '${file.name}' - full stack trace follows:",e)
				}
			}
		}
	}
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	private LinkedHashMap getIOSet(JSONObject io,LinkedHashMap apiObject){
		LinkedHashMap<String,ParamsDescriptor> ioSet = [:]

		io.each{ k, v ->
			// init
			if(!ioSet[k]){ ioSet[k] = [] }
			
			def roleVars=v.toList()
			roleVars.each{ val ->
				if(v.contains(val)){
					if(!ioSet[k].contains(apiObject[val])){
						ioSet[k].add(apiObject[val])
					}
				}
			}
		}
		
		// add permitAll vars to other roles after processing
		ioSet.each(){ key, val ->
			if(key!='permitAll'){
				ioSet['permitAll'].each(){ it ->
						ioSet[key].add(it)
				}
			}
		}
		
		return ioSet
	}
	
	private ApiDescriptor createApiDescriptor(String apiname,String apiMethod, String apiDescription, List apiRoles, List batchRoles, String uri, JSONObject values, JSONObject json){
		LinkedHashMap<String,ParamsDescriptor> apiObject = [:]
		ApiParams param = new ApiParams()
		
		values.each{ k,v ->
			String references = ""
			String hasDescription = ""
			String hasMockData = ""
			
			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			param.setParam(v.type,k)
			
			def configType = grailsApplication.config.apitoolkit.apiobject.type."${v.type}"
			
			hasDescription = (configType?.description)?configType.description:hasDescription
			hasDescription = (v?.description)?v.description:hasDescription
			if(hasDescription){ param.hasDescription(hasDescription) }
			
			references = (configType?.references)?configType.references:""
			references = (v?.references)?v.references:references
			if(references){ param.referencedBy(references) }
			
			hasMockData = (v?.mockData)?v.mockData:hasMockData
			if(hasMockData){ param.hasMockData(hasMockData) }

			// collect api vars into list to use in apiDescriptor
			apiObject[param.param.name] = param.toObject()
		}
		
		LinkedHashMap receives = getIOSet(json.URI[uri]?.REQUEST,apiObject)
		LinkedHashMap returns = getIOSet(json.URI[uri]?.RESPONSE,apiObject)
		
		ApiDescriptor service = new ApiDescriptor(
			'method':"$apiMethod",
			'description':"$apiDescription",
			'roles':[],
			'batchRoles':[],
			'doc':[:],
			'receives':receives,
			'returns':returns
		)
		
		service['roles'] = apiRoles
		service['batchRoles'] = batchRoles

		return service
	}
	
	Boolean parseJson(String apiName,JSONObject json){
		Map methods = [:]
		json.VERSION.each() { vers ->
			String defaultAction = (vers.value.DEFAULTACTION)?vers.value.DEFAULTACTION:'index'
			List deprecated = (vers.value.DEPRECATED)?vers.value.DEPRECATED:[]
			String domainPackage = (vers.value.DOMAINPACKAGE!=null || vers.value.DOMAINPACKAGE?.size()>0)?vers.value.DOMAINPACKAGE:null
			vers.value.URI.each() { it ->
				def cache = apiCacheService.getApiCache(apiName)
				methods['cacheversion'] = (!cache?.cacheversion)? 1 : cache['cacheversion']+1
				
				JSONObject apiVersion = json.VERSION[vers.key]
				
				//List temp = it.key.split('/')
				//String actionname = temp[1]
				String actionname = it.key
				
				ApiStatuses error = new ApiStatuses()
				
				ApiDescriptor apiDescriptor
				Map apiParams
				
				String apiMethod = it.value.METHOD
				String apiDescription = it.value.DESCRIPTION
				List apiRoles = it.value.ROLES
				List batchRoles = it.value.BATCH
				
				String uri = it.key
				apiDescriptor = createApiDescriptor(apiName, apiMethod, apiDescription, apiRoles, batchRoles, uri, json.get('VALUES'), apiVersion)
				if(!methods[vers.key]){
					methods[vers.key] = [:]
				}
				
				if(!methods['currentStable']){
					methods['currentStable'] = [:]
					methods['currentStable']['value'] = json.CURRENTSTABLE
				}
				if(!methods[vers.key]['deprecated']){
					methods[vers.key]['deprecated'] = []
					methods[vers.key]['deprecated'] = deprecated
				}
				
				if(!methods[vers.key]['defaultAction']){
					methods[vers.key]['defaultAction'] = defaultAction
				}

				if(!methods[vers.key]['domainPackage']){
					methods[vers.key]['domainPackage'] = domainPackage
				}

				methods[vers.key][actionname] = apiDescriptor

			}
			if(methods){
				apiCacheService.setApiCache(apiName,methods)
				//apiLayerService.setApiCache(apiName,methods)
			}
		}
	}

}
