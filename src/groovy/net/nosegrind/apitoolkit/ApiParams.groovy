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
	
	def isRequired(){
		this.param.required = true
		return this
	}
	
	def isNotRequired(){
		this.param.required = false
		return this
	}
	
	def hasParams(ParamsDescriptor[] values){
		this.param.values = values
		return this
	}
	
	def exposeToService(boolean data){
		this.param.expose = data
		return this
	}
	
	def _PKEY(String name,String description,String idReference){
		this.param = new ParamsDescriptor(paramType:"PKEY",name:"${name}",description:"${description}",idReferences:"${idReference}")
		return this
	}
	
	def _FKEY(String name,String description,String idReference){
		this.param = new ParamsDescriptor(paramType:"FKEY",name:"${name}",description:"${description}",idReferences:"${idReference}")
		return this
	}

	def _INDEX(String name,String description,String idReference){
		this.param = new ParamsDescriptor(paramType:"INDEX",name:"${name}",description:"${description}",idReferences:"${idReference}")
		return this
	}
	
	def _STRING(String name,String description){
		this.param = new ParamsDescriptor(paramType:"STRING",name:"${name}",description:"${description}")
		return this
	}

	def _BOOLEAN(String name,String description){
		this.param = new ParamsDescriptor(paramType:"BOOLEAN",name:"${name}",description:"${description}")
		return this
	}
	
	def _FLOAT(String name,String description){
		this.param = new ParamsDescriptor(paramType:"FLOAT",name:"${name}",description:"${description}")
		return this
	}
	
	def _BIGDECIMAL(String name,String description){
		this.param = new ParamsDescriptor(paramType:"BIGDECIMAL",name:"${name}",description:"${description}")
		return this
	}
	
	def _LONG(String name,String description){
		this.param = new ParamsDescriptor(paramType:"LONG",name:"${name}",description:"${description}")
		return this
	}
	
	def _EMAIL(String name,String description){
		this.param = new ParamsDescriptor(paramType:"EMAIL",name:"${name}",description:"${description}")
		return this
	}
	
	def _URL(String name,String description){
		this.param = new ParamsDescriptor(paramType:"URL",name:"${name}",description:"${description}")
		return this
	}
}
