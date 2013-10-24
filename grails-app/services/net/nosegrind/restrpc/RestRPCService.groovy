package net.nosegrind.restrpc

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH

//org.springframework.web.context.request.RequestContextHolder.getRequestAttributes().getResponse()

class RestRPCService{

	def grailsApplication

	static transactional = false

	def getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}

	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	def getParams(){
		def params = RCH.currentRequestAttributes().params
		def request = getRequest()
		def json = request.JSON
		json.each() { key,value ->
			params[key] = value
		}
		return params
	}

	boolean isApiCall(){
		def request = getRequest()
		def params = getParams()
		def queryString = request.'javax.servlet.forward.query_string'
		
		def uri
		if(request.isRedirected()){
			if(params.action=='index'){
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI+'/'+params.action
			}else{
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
			}
		}else{
			uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
		}
		def api
		if(grailsApplication.config.grails.app.context=='/'){
			if(params?.format){
				api = "/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}/${params.format}"
			}else{
				api = "/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}"
			}
			if(queryString){
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}?${queryString}"
					}else{
						api += "/${params.id}?${queryString}"
					}
				}else{
					api += "?${queryString}"
				}
				
			}else{
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}"
					}else{
						api += "/${params.id}"
					}
				}
			}
		}else if(grailsApplication.config?.grails?.app?.context){
			if(params?.format){
				api = "${grailsApplication.config.grails.app.context}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}"
			}else{
				api = "${grailsApplication.config.grails.app.context}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}/${params.format}"
			}
			if(queryString){
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}?${queryString}"
					}else{
						api += "/${params.id}?${queryString}"
					}
				}else{
					api += "?${queryString}"
				}
			}else{
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}"
					}else{
						api += "/${params.id}"
					}
				}
			}
		}else if(!grailsApplication.config?.grails?.app?.context){
			if(params?.format){
				api = "/${grailsApplication.metadata['app.name']}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}"
			}else{
				api = "/${grailsApplication.metadata['app.name']}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.config.restrpc.apiVersion}/${params.controller}/${params.action}/${params.format}"
			}
			if(queryString){
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}?${queryString}"
					}else{
						api += "/${params.id}?${queryString}"
					}
				}else{
					api += "?${queryString}"
				}
			}else{
				if(params?.id){
					if(params?.path){
						api += "/${params.id}/${params?.path}"
					}else{
						api += "/${params.id}"
					}
				}
			}
		}

		//println("${uri}==${api}")
		return uri==api
	}

	boolean isRequestMatch(String protocol){
		def request = getRequest()
		protocol = protocol.toUpperCase()
		return request.method==protocol
	}
	
	Map formatModel(Object data){
		def newMap = [:]
		if(data && (!data?.response && !data?.metaClass && !data?.params)){
			data.each{ key, value ->
				if(value){
					if(grailsApplication.isDomainClass(value.getClass())){
						newMap[key]=value
					}else{
						if(value in java.util.Collection){
							if(val?.size()>0){
								if(grailsApplication.isDomainClass(val[0].getClass())){
									val.each{ k,v ->
										newMap[key][v.id]= v
									}
								}else{
									value = formatModel(value)
									newMap[key]= value
								}
							}
						}else{
							newMap[key]=value.toString()
						}
					}
				}
			}
		}
		return newMap
	}

	boolean validateUrl(String url){
		String[] schemes = ["http","https"]
		UrlValidator urlValidator = new UrlValidator(schemes)
		return urlValidator.isValid(url)
	}
	
	def _200_SUCCESS(String msg){
		def response = getResponse()
		response.setStatus(200,"[Success] : ${msg}")
		return
	}

	def _200_SUCCESS(){
		def response = getResponse()
		response.setStatus(200,"[Success]")
		return
	}

	// 304 not modified
	def _304_NOTMODIFIED(String msg){
		def response = getResponse()
		response.setStatus(304,"[Not Modified] : ${msg}")
		return
	}

	def _304_NOTMODIFIED(){
		def response = getResponse()
		response.setStatus(304,"[Not Modified]")
		return
	}

	// 404 not found
	def _404_NOTFOUND(String msg){
		def response = getResponse()
		response.setStatus(404,"[Not Found] : ${msg}")
		return
	}

	def _404_NOTFOUND(){
		def response = getResponse()
		response.setStatus(404,"[Not Found]")
		return
	}

	// 400 bad request
	def _404_BADREQUEST(String msg){
		def response = getResponse()
		response.setStatus(404,"[Bad Request] : ${msg}")
		return
	}

	def _404_BADREQUEST(){
		def response = getResponse()
		response.setStatus(404,"[Bad Request]")
		return
	}

	// 403 forbidden
	def _403_FORBIDDEN(String msg){
		def response = getResponse()
		response.setStatus(403,"[Forbidden] : ${msg}")
		return
	}

	def _403_FORBIDDEN(){
		def response = getResponse()
		response.setStatus(403,"[Forbidden]")
		return
	}
}
