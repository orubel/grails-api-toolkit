import java.util.ArrayList;
import java.util.List;
import java.util.Map

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import net.nosegrind.apitoolkit.Method;
import net.nosegrind.apitoolkit.ApiStatuses;
import org.springframework.web.context.request.RequestContextHolder as RCH

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
							// CHECK METHOD FOR API CHAINING. DOES METHOD MATCH?
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
										return true
									}
								}else{
									return false
								}
							}
						}else{
							return true
						}
					}
				}
				return true
			}
			
			after = { Map model ->
				if(!model){
					return true
				}
				def newModel = apiToolkitService.convertModel(model)
				ApiStatuses error = new ApiStatuses()
				params.action = (params.action)?params.action:'index'
				def uri = [params.controller,params.action,params.id]
				def queryString = request.'javax.servlet.forward.query_string'
				List oldPath = (queryString)?queryString.split('&'):[]
				
				// create response data
				List path = []
				def newQuery = []
				if(params.containsKey("newPath")){
					path = params.newPath?.split('&')
				}else{
					path = (queryString)?queryString.split('&'):[]
				}
				if(path){
					path.remove(0)
					Map query = [:]
					for(int b = 1;b<path.size();b++){
						def temp = path[b].split('=')
						query[temp[0]] = temp[1]
					}
					query.each{ k,v ->
						newQuery.add("${k}=${v}")
					}
				}

				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
				
				def tempType = request.getHeader('Content-Type')?.split(';')
				def type = (tempType)?tempType[0]:request.getHeader('Content-Type')

				// api chaining
				if(path){
					def uri2 = apiToolkitService.isChainedApi(newModel,path as List)
					int pos = apiToolkitService.checkChainedMethodPosition(uri,oldPath as List)
					if(pos==3){
						log.info("[ERROR] Bad combination of unsafe METHODS in api chain.")
						return false
					}else{
						def currentPath = "${uri2['controller']}/${uri2['action']}"
						def inc = 0
						
						if(currentPath!=path.last().split('=')[0]){
							if(uri2){
								
								def methods = cache["${uri2['action']}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
								def method = (methods.contains(request.method))?request.method:null
								if(apiToolkitService.checkAuthority(cache["${uri2['action']}"]['apiRoles'])){
									switch(type){
										case 'application/xml':
											forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
											break
										case 'application/json':
										default:
											forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
											break
									}
								}else{
									String msg = "User does not have access."
									error._403_FORBIDDEN()(msg).send()
									return false
								}
								

							}else{
								String msg = "Path was unable to be parsed. Check your path variables and try again."
								//redirect(uri: "/")
								error._404_NOT_FOUND(msg).send()
								return false
							}
							inc++
						}else{
							switch(type){
								case 'application/xml':
									forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
									return false
									break
								case 'application/json':
								default:
									forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
									return false
									break
							}
						}

					}
					return
				}else{
					if(cache){
						if(cache["${params.action}"]){
							
							def encoding = null
							if(tempType){
								encoding = (tempType.size()>1)?tempType[1]:null
							}
							
							// make 'application/json' default
							def formats = ['text/html','application/json','application/xml']
							type = (request.getHeader('Content-Type'))?formats.findAll{ type.startsWith(it) }[0].toString():null
	
							if(type){
								if (apiToolkitService.isApiCall()) {
									List methods = []
									if(queryString){
										def uri2 = apiToolkitService.isChainedApi(newModel,queryString.split('&') as List)
										methods = cache["${uri2['action']}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
									}else{
										methods = cache["${params.action}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
									}
									
									def method = (methods.contains(request.method))?request.method:null
									
									response.setHeader('Allow', methods.join(', '))
									response.setHeader('Authorization', cache["${params.action}"]['apiRoles'].join(', '))
									
									if(method){
										switch(request.method) {
											case 'PURGE':
												// cleans cache
												break;
											case 'TRACE':
												break;
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
												return false
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
													return false
												}
												break
											case 'POST':
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
														case 'application/json':
														default:
															if(encoding){
																render(text:map as JSON, contentType: "${type}",encoding:"${encoding}")
															}else{
																render(text:map as JSON, contentType: "${type}")
															}
															break
													}
													return false
												}
												break
											case 'PUT':
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
														case 'application/json':
														default:
															if(encoding){
																render(text:map as JSON, contentType: "${type}",encoding:"${encoding}")
															}else{
																render(text:map as JSON, contentType: "${type}")
															}
															break
													}
													return false
												}
												break
											case 'DELETE':
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
														case 'application/json':
														default:
															if(encoding){
																render(text:map as JSON, contentType: "${type}",encoding:"${encoding}")
															}else{
																render(text:map as JSON, contentType: "${type}")
															}
															break
													}
													return false
												}
												break
										}
									}
									return true
								}	
							}else{
								return true
								//render(view:params.action,model:model)
							}
						}else{
							return true
							//render(view:params.action,model:model)
						}
					}
				}
			}
		}

	}

}