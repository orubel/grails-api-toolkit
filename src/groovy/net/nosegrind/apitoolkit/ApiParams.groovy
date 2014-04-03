package net.nosegrind.apitoolkit

import net.nosegrind.apitoolkit.ParamsDescriptor;

import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiParams{

	def grailsApplication
	def springSecurityService

	ParamsDescriptor param
	
	private static final INSTANCE = new ApiParams()
	
	static getInstance(){ return INSTANCE }
	
	private ApiParams() {}

	def toObject(){
		return this.param
	}

	def hasRoles(List roles){
		this.param.roles = roles
		return this
	}
	
	def hasMockData(String data){
		this.param.mockData = data
		return this
	}
	
	def hasDescription(String data){
		this.param.description = data
		return this
	}
	
	def isRequired(boolean data){
		this.param.required = data
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
	
	def isVisible(boolean data){
		this.param.visible = data
		return this
	}
	
	def setParam(String type,String name){
		this.param = new ParamsDescriptor(paramType:"${type}",name:"${name}")
		return this
	}
}
