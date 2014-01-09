import java.util.ArrayList;
import java.util.List;
import java.util.Map

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.apitoolkit.Api;
import net.nosegrind.apitoolkit.Method;

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
							
							if(!apiToolkitService.isRequestMatch(method)){
								return false
							}
						}else{
							return false
						}
					}
				}
			}
			
			after = { Map model ->
				println("after filter...")
				/*
				 if(request.isRedirected()){
					 def uri = grailsAttributes.getControllerActionUri(request)
					 println(uri)
				 }
				 */

				params.action = (params.action)?params.action:'index'
				
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
				println(params.controller)
				
				def newModel

				if(cache){
					println("has cache...")
					if(cache["${params.action}"]){
						def formats = ['text/html','application/json','application/xml']
						def type = (request.getHeader('Content-Type'))?formats.findAll{ request.getHeader('Content-Type')?.startsWith(it) }[0].toString():null
						if(type){
						
							if (apiToolkitService.isApiCall()) {
								def methods = cache["${params.action}"]['method'].replace('[','').replace(']','').split(',')*.trim() as List
								def method = (methods.contains(request.method))?request.method:null
								def queryString = request.'javax.servlet.forward.query_string'
								def path = (queryString)?queryString.split('&'):[]
								
								response.setHeader('Allow', methods.join(', '))
								response.setHeader('Content-Type', "${type};charset=UTF-8")
								response.setHeader('Authorization', cache["${params.action}"]['apiRoles'].join(', '))
								
								newModel = (grailsApplication.isDomainClass(model.getClass()))?model:apiToolkitService.formatModel(model)

								def lastKey
								if(method){
									switch(method) {
										case 'HEAD':
											break;
										case 'OPTIONS':
											println(type)
											switch(type){
												case 'application/xml':
													render(text:cache["${params.action}"]['doc'] as XML, contentType: "application/xml")
													break
												case 'application/json':
												default:
													render(text:cache["${params.action}"]['doc'] as JSON, contentType: "application/json")
													break
											}
											break;
										case 'GET':
											if(!newModel.isEmpty()){
												println("model not empty")
												println(type)
												switch(type){
													case 'application/json':
														def map = newModel
														def key
														if(path){
															def pathSize = path.size()
															path.eachWithIndex(){ val,i ->
																if(val){
																	def temp = val.split('=')
																	String pathKey = temp[0]
																	String pathVal = (temp.size()>1)?temp[1]:null
					
																	if(pathKey=='null'){
																		pathVal = pathVal.split('/').join('.')
																		if(map."${pathVal}"){
																			if(map."${pathVal}" in java.util.Collection){
																				map = map."${pathVal}"
																			}else{
																				if(map."${pathVal}".toString().isInteger()){
																					if(i==(pathSize-1)){
																						def newMap = ["${pathVal}":map."${pathVal}"]
																						map = newMap
																					}else{
																						params.id = map."${pathVal}"
																					}
																				}else{
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}
																			}
																		}else{
																			String msg = "Path '${pathKey}' was unable to be parsed"
																			return apiToolkitService._404_NOTFOUND(msg)
																		}
																	}else{
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																		uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																		redirect(uri: "${uri}")
																	}
																}
															}
														}
														
														render(text:map as JSON, contentType: "application/json")
														//return false
														break
													case 'application/xml':
														def map = newModel
														def key
					
														if(path){
															def pathSize = path.size()
															path.eachWithIndex(){ val,i ->
																if(val){
																	def temp = val.split('=')
																	String pathKey = temp[0]
																	String pathVal = (temp.size()>1)?temp[1]:null
					
																	if(pathKey=='null'){
																		pathVal = pathVal.split('/').join('.')
																		if(map."${pathVal}"){
																			if(map."${pathVal}" in java.util.Collection){
																				map = map."${pathVal}"
																			}else{
																				if(map."${pathVal}".toString().isInteger()){
																					if(i==(pathSize-1)){
																						def newMap = ["${pathVal}":map."${pathVal}"]
																						map = newMap
																					}else{
																						params.id = map."${pathVal}"
																					}
																				}else{
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}
																			}
																		}else{
																			String msg = "Path '${pathKey}' was unable to be parsed"
																			return apiToolkitService._404_NOTFOUND(msg)
																		}
																	}else{
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																		uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																		redirect(uri: "${uri}")
																	}
																}
															}
														}
					
														render(text:map as XML, contentType: "application/xml")
														//return false
														break
													case 'text/html':
														def map = newModel
														def key
					
														if(path){
															def pathSize = path.size()
															path.eachWithIndex(){ val,i ->
																if(val){
																	def temp = val.split('=')
																	String pathKey = temp[0]
																	String pathVal = (temp.size()>1)?temp[1]:null
					
																	if(pathKey=='null'){
																		pathVal = pathVal.split('/').join('.')
																		if(map."${pathVal}"){
																			if(map."${pathVal}" in java.util.Collection){
																				map = map."${pathVal}"
																			}else{
																				if(map."${pathVal}".toString().isInteger()){
																					if(i==(pathSize-1)){
																						def newMap = ["${pathVal}":map."${pathVal}"]
																						map = newMap
																					}else{
																						params.id = map."${pathVal}"
																					}
																				}else{
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}
																			}
																		}else{
																			String msg = "Path '${pathKey}' was unable to be parsed"
																			return apiToolkitService._404_NOTFOUND(msg)
																		}
																	}else{
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																		uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																		redirect(uri: "${uri}")
																	}
																}
															}
														}
														/*
														def linkRels = []
														map.each(){ k,v ->
															def api = action.getAnnotation(Api)
															def returns = api.returns()
															returns.each{ p ->
																String paramType = p.paramType().toString()
																String name = p.name().toString()
																String belongsTo = p.belongsTo().toString()
																Integer paramKey = apiToolkitService.getKey(paramType)
																if(paramKey>0 && name==k){
																	def temp = []
																	if(paramKey==1){
																		temp = apiToolkitService.createLinkRelationships(paramType,name,params.controller)
																	}else{
																		temp = apiToolkitService.createLinkRelationships(paramType,name,belongsTo)
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/${format}/${belongsTo[0].toLowerCase()+belongsTo.substring(1)}/show/${v}"
																		map[k] = "<a href=${uri}>${v}<a>"
																	}
																	linkRels.add(temp)
																}
															}
														}
														map['linkRelationships']=linkRels
														
														def json = map as JSON
														json = json.toString().replaceAll("\\{\n","\\{<br><div style='padding-left:2em;'>")
														json = json.toString().replaceAll("}"," </div>}<br>")
														json = json.toString().replaceAll(",",",<br>")
														
														render(text:json)
														//return false
														break
														*/
												}
											}
											break
										case 'POST':
											switch(type){
												case 'application/json':
													def map = newModel
													def key
					
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as JSON, contentType: "application/json")
													break
												case 'application/xml':
													def map = newModel
													def key
					
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as XML, contentType: "application/xml")
													return false
													break
											}
											break
										case 'PUT':
											switch(type){
												case 'application/json':
													def map = newModel
													def key
					
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as JSON, contentType: "application/json")
													break
												case 'application/json':
													def map = newModel
													def key
					
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as XML, contentType: "application/xml")
													return false
													break
											}
											break
										case 'DELETE':
											switch(type){
												case 'application/json':
													def key
													
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													return response.status
													break;
												case 'application/xml':
													def key
													
													if(path){
														def pathSize = path.size()
														path.eachWithIndex(){ val,i ->
															if(val){
																def temp = val.split('=')
																String pathKey = temp[0]
																String pathVal = (temp.size()>1)?temp[1]:null
					
																if(pathKey=='null'){
																	pathVal = pathVal.split('/').join('.')
																	if(map."${pathVal}"){
																		if(map."${pathVal}" in java.util.Collection){
																			map = map."${pathVal}"
																		}else{
																			if(map."${pathVal}".toString().isInteger()){
																				if(i==(pathSize-1)){
																					def newMap = ["${pathVal}":map."${pathVal}"]
																					map = newMap
																				}else{
																					params.id = map."${pathVal}"
																				}
																			}else{
																				def newMap = ["${pathVal}":map."${pathVal}"]
																				map = newMap
																			}
																		}
																	}else{
																		String msg = "Path '${pathKey}' was unable to be parsed"
																		return apiToolkitService._404_NOTFOUND(msg)
																	}
																}else{
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
												
													return response.status
													break
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
		
