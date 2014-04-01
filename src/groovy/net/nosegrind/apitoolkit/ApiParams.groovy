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
	
	def _PKEY(String name){
		this.param = new ParamsDescriptor(paramType:"PKEY",name:"${name}")
		return this
	}
	
	def _FKEY(String name){
		this.param = new ParamsDescriptor(paramType:"FKEY",name:"${name}")
		return this
	}

	def _INDEX(String name){
		this.param = new ParamsDescriptor(paramType:"INDEX",name:"${name}")
		return this
	}
	
	def _STRING(String name){
		this.param = new ParamsDescriptor(paramType:"STRING",name:"${name}")
		return this
	}

	def _BOOLEAN(String name){
		this.param = new ParamsDescriptor(paramType:"BOOLEAN",name:"${name}")
		return this
	}
	
	def _FLOAT(String name){
		this.param = new ParamsDescriptor(paramType:"FLOAT",name:"${name}")
		return this
	}
	
	def _BIGDECIMAL(String name){
		this.param = new ParamsDescriptor(paramType:"BIGDECIMAL",name:"${name}")
		return this
	}
	
	def _LONG(String name){
		this.param = new ParamsDescriptor(paramType:"LONG",name:"${name}")
		return this
	}
	
	def _EMAIL(String name){
		this.param = new ParamsDescriptor(paramType:"EMAIL",name:"${name}")
		return this
	}
	
	def _URL(String name){
		this.param = new ParamsDescriptor(paramType:"URL",name:"${name}")
		return this
	}
}
