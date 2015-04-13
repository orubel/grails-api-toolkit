/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.converters.JSON
import grails.converters.XML
import java.lang.reflect.Method
import java.util.HashSet
import java.net.URI
import java.util.ArrayList
import java.util.List

import org.springframework.data.authentication.UserCredentials
import com.mongodb.DB
import com.mongodb.Mongo
import com.mongodb.MongoClient
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import org.springframework.cache.Cache;

import grails.plugin.cache.GrailsValueWrapper
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import net.nosegrind.apitoolkit.*


class MongoCacheService{

	static transactional = false
	
	GrailsApplication grailsApplication
	//SpringSecurityService springSecurityService
	def mongoDbFactory
	DB db
	
	public initialize(){
		try {
			DB db = mongoDbFactory.getDb()
		} catch (IOException ex) {
			Logger.info(ex.getMessage());
		}
	}

	/*
	 * Placeholder Function for Proxy; not used in API Application
	 */
	public getIoStateNames(){
		
	}
	
	public getIoState(){
		
	}
	
	/*
	 * can only be called on initialize; requires restart as new functionality would be being added
	 */
	private createIoState(){
		
	}
	
	public updateIoState(){
	
	}
	
	public deleteIoState(){
	
	}
}
