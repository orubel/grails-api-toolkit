import java.util.ArrayList;
import java.util.List;
import java.util.Map

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import net.nosegrind.apitoolkit.Method;
import net.nosegrind.apitoolkit.ApiStatuses;
import org.springframework.web.context.request.RequestContextHolder as RCH

import org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingInfo
import org.codehaus.groovy.grails.web.mapping.UrlMappingData
import org.codehaus.groovy.grails.web.mapping.UrlMappingInfo

import org.codehaus.groovy.grails.commons.ControllerArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.web.mapping.UrlMappingInfo;
import org.codehaus.groovy.grails.web.mapping.UrlMappingsHolder;
import org.codehaus.groovy.grails.web.mapping.exceptions.UrlMappingException;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.web.context.request.RequestContextHolder

import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse

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
				if(!model){
					return false
				}
				def newModel = apiToolkitService.convertModel(model)
				ApiStatuses error = new ApiStatuses()
				params.action = (params.action)?params.action:'index'
				def uri = [params.controller,params.action,params.id]
				def queryString = request.'javax.servlet.forward.query_string'
				List oldPath = (queryString)?queryString.split('&'):[]
				
				List path = []

				if(params.newPath){
					path = params.newPath.split('&')
				}else{
					path = (queryString)?queryString.split('&'):[]
				}
				
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null

				
				def tempType = request.getHeader('Content-Type')?.split(';')
				def type = (tempType)?tempType[0]:request.getHeader('Content-Type')

				// api chaining
				if(path){
					int pos = apiToolkitService.checkChainedMethodPosition(uri,oldPath as List)
					if(pos==3){
						log.info("[ERROR] Bad combination of unsafe METHODS in api chain.")
						return false
					}else{
						def uri2 = [:]
						def inc = 0

						while(uri2['controller']!=path.last().split('=')[0].split('/')[0]){
							uri2 = apiToolkitService.isChainedApi(newModel,path as List)
							if(uri2){
								Map query = [:]
								inc.each{ i ->
									path.remove(i)
								}
								
								for(int b = inc+1;b<path.size();b++){
									def temp = path[b].split('=')
									query[temp[0]] = temp[1]
								}

								def newQuery = []
								query.each{ k,v ->
									newQuery.add("${k}=${v}")
								}
								
								def methods = cache["${uri2['action']}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
								def method = (methods.contains(request.method))?request.method:null
								
								if(apiToolkitService.checkAuthority(cache["${params.action}"]['apiRoles'])){
									switch(type){
										case 'application/xml':
											//request.getRequestDispatcher("/${apiName}_${apiVersion}/${uri2['controller']}/${uri2['action']}/${uri2['id']}?${newQuery.join('&')}").forward(request, response);
											forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
											break
										case 'application/json':
										default:
											//request.getRequestDispatcher("/${apiName}_${apiVersion}/${uri2['controller']}/${uri2['action']}/${uri2['id']}?${newQuery.join('&')}").forward(request, response)
											forward(controller:"${uri2['controller']}",action:"${uri2['action']}",id:"${uri2['id']}",params:[newPath:newQuery.join('&')])
											break
									}
								}else{
									String msg = "User does not have access."
									error._403_FORBIDDEN()(msg).send()
									return false
								}
								break
							}else{
								String msg = "Path was unable to be parsed. Check your path variables and try again."
								//redirect(uri: "/")
								error._404_NOT_FOUND(msg).send()
								return false
							}
							inc++
						}
						

					}
					
					switch(type){
						case 'application/xml':
							render(text:newModel as XML, contentType: "${type}")
							break
						case 'application/json':
						default:
							render(text:newModel as JSON, contentType: "${type}")
							break
					}
					return false
				}else{
					if(cache){
						if(cache["${params.action}"]){
							def formats = ['text/html','application/json','application/xml']
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

}