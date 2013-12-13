package net.nosegrind.restrpc

import net.nosegrind.restrpc.ParamsDescriptor

import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiParams{

	def grailsApplication
	def springSecurityService

	ParamsDescriptor param
	
	private static final INSTANCE = new ApiParams()
	
	static getInstance(){ return INSTANCE }
	
	private ApiParams() {}

	def toObject(){
		return this
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

	def _PKEY(String name,String description,String idReference){
		this.param.paramType = 'PKEY'
		this.param.name = name
		this.param.description = description
		this.param.idReferences = idReference
		return this
	}
	
	def _FKEY(String name,String description,String idReference){
		this.param.paramType = 'FKEY'
		this.param.name = name
		this.param.description = description
		this.param.idReferences = idReference
		return this
	}

	def _INDEX(String name,String description,String idReference){
		this.param.paramType = 'INDEX'
		this.param.name = name
		this.param.description = description
		this.param.idReferences = idReference
		return this
	}
	
	def _STRING(String name,String description){
		this.param.paramType = 'STRING'
		this.param.name = name
		this.param.description = description
		return this
	}

	def _BOOLEAN(String name,String description){
		this.param.paramType = 'BOOLEAN'
		this.param.name = name
		this.param.description = description
		return this
	}
	
	def _FLOAT(String name,String description){
		this.param.paramType = 'FLOAT'
		this.param.name = name
		this.param.description = description
		return this
	}
	
	def _BIGDECIMAL(String name,String description){
		this.param.paramType = 'BIGDECIMAL'
		this.param.name = name
		this.param.description = description
		return this
	}
	
	def _LONG(String name,String description){
		this.param.paramType = 'LONG'
		this.param.name = name
		this.param.description = description
		return this
	}
	
	def _EMAIL(String name,String description){
		this.param.paramType = 'EMAIL'
		this.param.name = name
		this.param.description = description
		return this
	}
	
	def _URL(String name,String description){
		this.param.paramType = 'URL'
		this.param.name = name
		this.param.description = description
		return this
	}
}
