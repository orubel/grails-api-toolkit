import java.util.ArrayList;
import java.util.List;
import java.util.Map

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import org.codehaus.groovy.grails.web.json.JSONObject

import net.nosegrind.apitoolkit.Method;
import net.nosegrind.apitoolkit.ApiStatuses;
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.codehaus.groovy.grails.web.sitemesh.GrailsContentBufferingResponse

class ApiToolkitFilters {
	
	def apiToolkitService
	def grailsApplication
	def apiCacheService
	
	def filters = {
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		String apiDir = (apiName)?"${apiName}_v${apiVersion}":"v${apiVersion}"
		
		apitoolkit(uri:"/${apiDir}/**"){
			before = { Map model ->
				//println("##### FILTER (BEFORE)")
				params.action = (params.action)?params.action:'index'
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]

				if(cache){
					boolean result = apiToolkitService.handleApiRequest(cache,request,params)
					return result
				}else{
					return false
				}
			}
			
			after = { Map model ->
				 //println("##### FILTER (AFTER)")
				 def cache = (params.controller)?apiCacheService.getApiCache(params.controller):[:]
				 if(params?.apiChain?.order){
					 // return map of variable and POP first variable off chain 'order'
					 boolean result = apiToolkitService.handleApiChain(cache, request,response,model,params)
					 forward(controller:"${params.controller}",action:"${params.action}",id:"${model.id}")
					 return false
				 }else if(params?.apiBatch){
						 forward(controller:"${params.controller}",action:"${params.action}",params:params)
						 return false
				 }else{
				 	LinkedHashMap map = apiToolkitService.handleApiResponse(cache, request,response,model,params)
					 if(!model){
						 response.flushBuffer()
						 return false
					 }
					 
					 switch(request.method) {
						 case 'PURGE':
							 // cleans cache
							 break;
						 case 'TRACE':
							 break;
						 case 'HEAD':
							 break;
						 case 'OPTIONS':
	
							 LinkedHashMap doc = apiToolkitService.getApiDoc(params)
							 
							 switch(params.contentType){
								 case 'application/xml':
									 render(text:doc as XML, contentType: "${params.contentType}")
									 break
								 case 'application/json':
								 default:
									 render(text:doc as JSON, contentType: "${params.contentType}")
									 break
							 }
							 return false
							 break;
						 case 'GET':
							 if(!map.isEmpty()){
								 switch(params.contentType){
									 case 'application/xml':
										 if(params.encoding){
											 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as XML, contentType: "${params.contentType}")
										 }
										 break
									 case 'text/html':
										 break
									 case 'application/json':
									 default:
										 if(params.encoding){
											 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as JSON, contentType: "${params.contentType}")
										 }
										 break
								 }
								 return false
							 }
							 break
						 case 'POST':
							 if(!map.isEmpty()){
								 switch(params.contentType){
									 case 'application/xml':
										 if(params.encoding){
											 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as XML, contentType: "${params.contentType}")
										 }
										 break
									 case 'application/json':
									 default:
										 if(params.encoding){
											 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as JSON, contentType: "${params.contentType}")
										 }
										 break
								 }
								 return false
							 }
							 break
						 case 'PUT':
							 if(!map.isEmpty()){
								 switch(params.contentType){
									 case 'application/xml':
										 if(params.encoding){
											 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as XML, contentType: "${params.contentType}")
										 }
										 break
									 case 'application/json':
									 default:
										 if(params.encoding){
											 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as JSON, contentType: "${params.contentType}")
										 }
										 break
								 }
								 return false
							 }
							 break
						 case 'DELETE':
							 if(!map.isEmpty()){
								 switch(params.contentType){
									 case 'application/xml':
										 if(params.encoding){
											 render(text:map as XML, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as XML, contentType: "${params.contentType}")
										 }
										 break
									 case 'application/json':
									 default:
										 if(params.encoding){
											 render(text:map as JSON, contentType: "${params.contentType}",encoding:"${params.encoding}")
										 }else{
											 render(text:map as JSON, contentType: "${params.contentType}")
										 }
										 break
								 }
								 return false
							 }
							 break
					 }
				 }
			 }

		}

	}

}