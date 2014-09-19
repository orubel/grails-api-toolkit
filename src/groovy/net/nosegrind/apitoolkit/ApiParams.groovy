/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import net.nosegrind.apitoolkit.ParamsDescriptor
import grails.compiler.GrailsCompileStatic

//@GrailsCompileStatic
class ApiParams{

	//def applicationContext
	//def grailsApplication
	//def apiDocService
	//def springSecurityService

	ParamsDescriptor param
	
	private static final INSTANCE = new ApiParams()
	
	static getInstance(){ return INSTANCE }
	
	private ApiParams() {}

	def toObject(){
		return this.param
	}
	
	def hasMockData(String data){
		this.param.mockData = data
		return this
	}
	
	def hasDescription(String data){
		this.param.description = data
		return this
	}
	
	def hasParams(ParamsDescriptor[] values){
		this.param.values = values
		return this
	}

	def referencedBy(String data){
		this.param.idReferences = data
		return this
	}
	
	def setParam(String type,String name){
		this.param = new ParamsDescriptor(paramType:"${type}",name:"${name}")
		return this
	}
}
