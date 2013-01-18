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
	
	def methods = ['GET','POST','PUT','DELETE']
	
	def getParams(){
		def params = RCH.currentRequestAttributes().params
		def request = getRequest()
		def json = request.JSON
		json.each() { key,value ->
			params[key] = value
		}
		return params
	}
	
	// ERROR CODES
	// 200 = success
	// 304 not modified
	// 404 = not found
	// 400 bad request
	// 403 forbidden
	
	public isApiCall(){
		def request = getRequest()
		def params = getParams()
		def api = "/"+grailsApplication.metadata['app.name']+"/restrpc/"+params.controller+"/"+params.action+"/"+params.format?.toLowerCase()
		return(request.forwardURI?.toLowerCase()==api || request.forwardURI?.toLowerCase()=="${api}/${params.id}")?true:false
	}
	
	public isRequestMatch(String protocol){
		def request = getRequest()
		protocol = protocol.toUpperCase()
		return (request.method==protocol)?true:false
	}
	
    public boolean protocolMatch(String protocol){
		def params = RCH.requestAttributes.params
		def request = getRequest()
		
		protocol = protocol.toUpperCase()
		
		// test for API redirect- if API redirect is attempted... continue
		def api = "/"+grailsApplication.metadata['app.name']+"/restrpc/"+params.controller+"/"+params.action+"/"+params.format?.toLowerCase()
		if(request.forwardURI?.toLowerCase()==api || request.forwardURI?.toLowerCase()=="${api}/${params.id}"){
			if(methods.contains(protocol)){
					if(request.method==protocol){
						getParams()
						return true
					}else{
						sendData([errorCode:'400',errorMessage:"ERROR: REQUESTED METHOD DOES NOT MATCH SERVICE PROTOCOL."],'JSON')
						return false
					}
			}else{
				sendData([errorCode:'400',errorMessage:"ERROR: UNSUPPORTED REQUEST METHOD SENT"],'JSON')
				return false
			}
		}else{
			// NO API REDIRECT
			return false
		}
	}

	def sendData(Map data,String format){
		def request = getRequest()
		def response = getResponse()
		def json = request.JSON
		switch(request.method){
			case "${request.method}":
				if(data.errorCode && data.errorCode!=200){
					response.status = data.errorCode.toInteger()
					return data.errorMessage
				}else{
					switch(format){
						case 'xml':
							response.status = 200
							return data as XML
							break
						case 'json':
						default:
							response.status = 200
							return data as JSON
							break
					}
				}
				break
			default:
				response.status = 400
				return 'Bad Request'
				break
		}
	}

	Map formatModel(Map data){
		def newMap = [:]
		data.each{key, value ->
			if(grailsApplication.domainClasses*.clazz.contains(org.hibernate.Hibernate.getClass(value))){
				newMap[key]=formatDomainObject(value)
			}else{
				newMap[key]=value
			}
		}
		return newMap
	}
	
	Map formatDomainObject(Object data){
	    def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass","maps"]
	    def newMap = [:]
	    data.getProperties().each { key, val ->
	        if (!nonPersistent.contains(key)) {
				if(grailsApplication.isDomainClass(val.getClass())){
					newMap.put key, val.id
				}else{
					newMap.put key, val
				}
	        }
	    }
		return newMap
	}
	
	Map processMap(Map data,Map processor){
		processor.each() { key, val ->
			if(!val?.trim()){
				data.remove(key)
			}else{
				def matcher = "${data[key]}" =~ "${data[key]}"
				data[key] = matcher.replaceAll(val)
			}
		}
		return data
	}
	
	boolean validateUrl(String url){
		String[] schemes = ["http","https"]
		UrlValidator urlValidator = new UrlValidator(schemes)
		if (urlValidator.isValid(url)) {
			return true
		}else{
			return false
		}
	}
}
