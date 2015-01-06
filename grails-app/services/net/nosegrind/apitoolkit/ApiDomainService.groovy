/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit


import java.util.LinkedHashMap;

import org.codehaus.groovy.grails.commons.GrailsApplication;

import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService

import org.springframework.cache.Cache
import org.codehaus.groovy.grails.commons.*

import net.nosegrind.apitoolkit.*

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class ApiDomainService{

	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	ApiLayerService apiLayerService

	static transactional = false
	
	def showInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domainInstance
		try{
			domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}

		return domainInstance.get(params.id.toLong())
	}
	
	def createInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domain
		try{
			domain = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage'])
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}
		def request = apiLayerService.getRequest()
		def apiParams = apiLayerService.getApiObjectParams(request,cache[params.apiObject][params.action]['receives'])

		def domainInstance = domain.newInstance()
		def keys = apiParams.collect(){ it.key }
		apiParams.each{ k,v ->
			if(apiParams[k]=='FKEY'){
				println("FKEY : "+k+"/"+v)
				def index = k[0..-3]
				def type = domain.getPropertyByName(index).type
				
				domainInstance["${index}"] = type.get(params."${index}".toLong())
			}else{
				println("NOKEY : "+k+"/"+v)
				domainInstance["${k}"] = params."${k}"
			}
		}

		if(!domainInstance.save(flush:true)){
			domainInstance.errors.allErrors.each(){ println(it) }
		}else{
			return domainInstance
		}
		return null
	}
	
	def updateInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domainInstance
		try{
			domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}

		if(!domainInstance.save(flush:true)){
			domainInstance.errors.allErrors.each(){ println(it) }
		}else{
			return domainInstance
		}
		return null
	}
	
	Boolean deleteInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domainInstance
		try{
			domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}

		if(domainInstance.delete(params.id.toLong())){
			return true
		}else{
			return false
		}
		return false
	}
}
