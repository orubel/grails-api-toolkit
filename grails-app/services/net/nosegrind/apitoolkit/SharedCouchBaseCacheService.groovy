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

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;

import org.springframework.cache.Cache;

import grails.plugin.cache.GrailsValueWrapper
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.springsecurity.SpringSecurityService


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import net.nosegrind.apitoolkit.*


class SharedCouchBaseCacheService{

	static transactional = false
	
	GrailsApplication grailsApplication
	//SpringSecurityService springSecurityService
	CouchbaseClient handler
	
	public initialize(){
		try {
			List<URI> baseURIs = new ArrayList<URI>()
			URI base = new URI("http://${grailsApplication.config.apitoolkit.sharedCache.url}:${grailsApplication.config.apitoolkit.sharedCache.port}/pools", uri)
			baseURIs.add(base)
			
			CouchbaseConnectionFactory cf = new CouchbaseConnectionFactory(baseURIs, grailsApplication.config.apitoolkit.sharedCache.bucket, grailsApplication.config.apitoolkit.sharedCache.user, grailsApplication.config.apitoolkit.sharedCache.password);
			this.handler = new CouchbaseClient((CouchbaseConnectionFactory)cf);
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
