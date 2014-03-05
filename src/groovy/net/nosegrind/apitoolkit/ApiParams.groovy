/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/
package net.nosegrind.apitoolkit

import net.nosegrind.apitoolkit.ParamsDescriptor
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import grails.util.Holders
import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiParams{

	def applicationContext
	def grailsApplication
	def apiDocService
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
	
	boolean validate(){
		println(this.param)
		/*
		switch(this.paramType.toLowerCase()){
			case 'string':
			case '_string':
				break;
			case 'float':
			case '_float':
				break
			case 'double':
				break
			case 'bigdecimal':
			case '_bigdecimal':
				break
			case 'long':
			case '_long':
				break;
			case 'integer':
				break
			case 'boolean':
			case '_boolean':
				break
			case 'email':
			case '_email':
				break
			case 'url':
			case '_url':
				break
		}
		*/
	}
	
	def hasParams(ParamsDescriptor[] values){
		this.param.values = values
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

	
	/*
	private def testKey(){
		def tmp = this.name.toLowerCase()
		def tmp2 = tmp.split("_")
		if(tmp2.count()>1){
			if(tmp2.last()=='id'){
				this.isKey = 'FKEY'
				return this
			}
		}else{
			if(tmp=='id'){
				this.isKey = 'PKEY'
				return this
			}
		}
	}
	*/
	
	def _DOMAIN(GrailsClass domain){
		ParamsDescriptor[] prms
		//GrailsClass clazz =  grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, domainName)
		//apiDocService.getDomain("${domainName}")
		
		//this.param = new ParamsDescriptor(paramType:"STRING",name:"${name}",description:"${description}").validate(this.paramType)
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
		testKey()
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
