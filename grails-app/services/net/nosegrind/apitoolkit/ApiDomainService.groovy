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
	
	void getInstance(String domainPackage){
		try{
			this.clazz = grailsApplication.getDomainClass(domainPackage).newInstance() 
		}catch(Exception e){
			log.error("[ApiDomainService :: getInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}
	}
	
	def showInstance(LinkedHashMap cache, GrailsParameterMap params){
		def domainInstance = grailsApplication.getDomainClass(cache[params.apiObject]['domainPackage']).newInstance()
		return domainInstance.get(params.id.toLong())
	}
	
	Long createInstance(LinkedHashMap cache, GrailsParameterMap params){
	
	}
	
	Boolean updateInstance(LinkedHashMap cache, GrailsParameterMap params){
		
	}
	
	Boolean deleteInstance(LinkedHashMap cache, GrailsParameterMap params){
		
	}
}
