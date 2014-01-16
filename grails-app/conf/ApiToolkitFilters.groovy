import java.util.ArrayList;
import java.util.List;
import java.util.Map

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import net.nosegrind.apitoolkit.Method;
import net.nosegrind.apitoolkit.ApiStatuses;

class ApiToolkitFilters {
	
	def apiToolkitService
	def grailsApplication
	def apiCacheService

	def filters = {
		
		String apiName = grailsApplication.config.apitoolkit.apiName
		String apiVersion = grailsApplication.metadata['app.version']
		
		apitoolkit(uri:"/${apiName}_${apiVersion}/**"){
			before = { Map model ->
				params.action = (params.action)?params.action:'index'
				
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
				
				if(cache){
					if(cache["${params.action}"]){
						if (apiToolkitService.isApiCall()) {
							// USER HAS ACCESS?
							if(!apiToolkitService.checkAuthority(cache["${params.action}"]['apiRoles'])){
								return false
							}
							// DOES METHOD MATCH?
							def method = cache["${params.action}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
							def uri = [params.controller,params.action,params.id]
							// DOES api.methods.contains(request.method)
							if(!apiToolkitService.isRequestMatch(method)){
								// check for apichain
								def queryString = request.'javax.servlet.forward.query_string'
								List path = (queryString)?queryString.split('&'):[]
								if(path){
									int pos = apiToolkitService.checkChainedMethodPosition(uri,path as List)
									if(pos==3){
										return false
									}
								}else{
									return false
								}
							}
						}else{
							return false
						}
					}
				}
			}
			
			after = { Map model ->
				println(model)
				def newModel = apiToolkitService.convertModel(model)
				println(newModel)
				ApiStatuses error = new ApiStatuses()
				params.action = (params.action)?params.action:'index'
				def uri = [params.controller,params.action,params.id]
				def queryString = request.'javax.servlet.forward.query_string'
				List path = (queryString)?queryString.split('&'):[]

				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null

				if(path){
					println(path)
					int pos = apiToolkitService.checkChainedMethodPosition(uri,path as List)
					if(pos==3){
						println("bad position")
						return false
					}else{
						String uri2 = apiToolkitService.isChainedApi(newModel,path as List)
println("uri2 = ${uri2}")
						if(uri2){
							if(uri2!='null'){
								if(uri2 =~ "&"){
									redirect(url: "${uri2}",absolute:true)
									return false
								}else{
									redirect(uri: "${uri2}",absolute:true)
									return false
								}
								return false
							}
						}else{
							String msg = "Path was unable to be parsed. Check your path variables and try again."
							redirect(uri: "/")
							error._404_NOT_FOUND(msg).send()
							return
						}
					}
					return false
				}
				
				if(cache){
					if(cache["${params.action}"]){
						def formats = ['text/html','application/json','application/xml']
						def tempType = request.getHeader('Content-Type')?.split(';')
						def type = (tempType)?tempType[0]:request.getHeader('Content-Type')
						def encoding = null
						if(tempType){
							encoding = (tempType.size()>1)?tempType[1]:null
						}
						
						// make 'application/json' default
						type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null

						if(type){
							if (apiToolkitService.isApiCall()) {
								def methods = cache["${params.action}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
								def method = (methods.contains(request.method))?request.method:null
								
								response.setHeader('Allow', methods.join(', '))
								response.setHeader('Authorization', cache["${params.action}"]['apiRoles'].join(', '))
								
								if(method){
									switch(request.method) {
										case 'HEAD':
											break;
										case 'OPTIONS':
											switch(type){
												case 'application/xml':
													render(text:cache["${params.action}"]['doc'] as XML, contentType: "${type}")
													break
												case 'application/json':
												default:
													render(text:cache["${params.action}"]['doc'] as JSON, contentType: "${type}")
													break
											}
											break;
										case 'GET':
											def map = newModel
											if(!newModel.isEmpty()){
											switch(type){
												case 'application/xml':
													if(encoding){
														render(text:map as XML, contentType: "${type}",encoding:"${encoding}")
													}else{
														render(text:map as XML, contentType: "${type}")
													}
													break
												case 'text/html':
													break
												case 'application/json':
												default:
													if(encoding){
														render(text:map as JSON, contentType: "${type}",encoding:"${encoding}")
													}else{
														render(text:map as JSON, contentType: "${type}")
													}
													break
												}
											}
											break
										case 'POST':
											switch(type){
												case 'application/xml':
													//return response.status
													break
												case 'application/json':
												default:
													//return response.status
													break
											}
											break
										case 'PUT':
											switch(type){
												case 'application/xml':
													//return response.status
													break
												case 'application/json':
												default:
													//return response.status
													break
											}
											break
										case 'DELETE':
											switch(type){
												case 'application/xml':
													//return response.status
													break
												case 'application/json':
												default:
													//return response.status
													break;
											}
											break
									}
								}
								return false
							}	
						}else{
							//render(view:params.action,model:model)
						}
					}else{
						//render(view:params.action,model:model)
					}
				}
			}
		}

	}
}