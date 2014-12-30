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
	
	Long createInstance(LinkedHashMap cache, GrailsParameterMap params){
		Long id = params.id.toLong()
		def domainInstance
		try{
			domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}

		if(domainInstance.save(flush:true)){
			return id
		}else{
			return null
		}
		return null
	}
	
	Boolean updateInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domainInstance
		try{
			domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		}catch(Exception e){
			log.error("[ApiDomainService :: showInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}

		if(domainInstance.save(flush:true)){
			return true
		}else{
			return false
		}
		return false
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
