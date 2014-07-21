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
	
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	private LinkedHashMap getIOSet(JSONObject io,LinkedHashMap apiObject){
		LinkedHashMap<String,ParamsDescriptor> ioSet = [:]

		io.each{ k, v ->
			// init
			if(!ioSet["${k}"]){
				ioSet["${k}"] = []
			}
			

			def roleVars=v.toList()
			roleVars.each{ val ->
				if(v.contains(val)){
					if(!ioSet["${k}"].contains(apiObject["${val}"])){
						ioSet["${k}"].add(apiObject["${val}"])
					}
				}
			}

		}
		
		// add permitAll vars to other roles after processing
		ioSet.each(){ key, val ->
			if(key!='permitAll'){
				ioSet["permitAll"].each(){ it ->
						ioSet["${key}"].add(it)
				}
			}
		}
		
		return ioSet
	}
	
	private ApiDescriptor createApiDescriptor(String apiname,String apiMethod, String apiDescription, List apiRoles, String uri, JSONObject values, JSONObject json, List deprecated){
		LinkedHashMap<String,ParamsDescriptor> apiObject = [:]
		ApiParams param = new ApiParams()
		
		values.each{ k,v ->
			String references = ""
			String hasDescription = ""
			String hasMockData = ""
			
			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			param.setParam(v.type,"${k}")
			
			def configType = grailsApplication.config.apitoolkit.apiobject.type."${v.type}"
			
			hasDescription = (configType?.description)?configType.description:hasDescription
			hasDescription = (v?.description)?v.description:hasDescription
			if(hasDescription){ param.hasDescription("${hasDescription}") }
			
			references = (configType?.references)?configType.references:""
			references = (v?.references)?v.references:references
			if(references){ param.referencedBy(references) }
			
			hasMockData = (v?.mockData)?v.mockData:hasMockData
			if(hasMockData){ param.hasMockData("${hasMockData}") }

			// collect api vars into list to use in apiDescriptor
			apiObject["${param.param.name}"] = param.toObject()
		}
		
		LinkedHashMap receives = getIOSet(json.URI["${uri}"]?.REQUEST,apiObject)
		LinkedHashMap returns = getIOSet(json.URI["${uri}"]?.RESPONSE,apiObject)
		
		ApiDescriptor service = new ApiDescriptor(
			"deprecated":[],
			"method":"${apiMethod}",
			"description":"${apiDescription}",
			"roles":[],
			"doc":[:],
			"receives":receives,
			"returns":returns
		)
		if(deprecated){
			service['deprecated'] = deprecated
		}
		service['roles'] = apiRoles

		return service
	}
	
	void initApiCache(){
		apiCacheService.flushAllApiCache()
		String apiObjectSrc = grailsApplication.config.apitoolkit.apiobjectSrc
		new File("${apiObjectSrc}").eachFile() { file ->
			String apiName = file.getName().split('\\.')[0].toLowerCase()
			JSONObject json = JSON.parse(file.text)
			parseJson(apiName,json)
			def cache2 = apiCacheService.getApiCache(apiName)
		}
	}
	

	Boolean parseJson(String apiName,JSONObject json){
		Map methods = [:]
		json.VERSION.each() { vers ->
			
			List deprecated = (vers.value.DEPRECATED)?vers.value.DEPRECATED:[]
			vers.value.URI.each() { it ->

				JSONObject apiVersion = json.VERSION["${vers.key}"]
				
				List temp = it.key.split('/')
				String actionname = temp[1]
				
				ApiStatuses error = new ApiStatuses()
				
				ApiDescriptor apiDescriptor
				Map apiParams
				
				String apiMethod = it.value.METHOD
				String apiDescription = it.value.DESCRIPTION
				List apiRoles = it.value.ROLES
				
				String uri = it.key
				apiDescriptor = createApiDescriptor(apiName, apiMethod, apiDescription, apiRoles, uri, json.get('VALUES'), apiVersion, deprecated)

				if(!methods["currentStable"]){
					methods["currentStable"] = [:]
					methods["currentStable"]['value'] = json.CURRENTSTABLE
				}
				if(!methods["${actionname}"]){
					methods["${actionname}"] = [:]
				}
				methods["${actionname}"]["${vers.key}"] = apiDescriptor
				
				if(methods){
					apiLayerService.setApiCache(apiName,methods)
				}
			}

		}
	}

}
