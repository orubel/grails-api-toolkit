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
		apitoolkit(controller:'*', action:'*'){
			before = { Map model ->

				// used for testing
				//if(request.getAttribute(GrailsApplicationAttributes.REDIRECT_ISSUED) != null){
				/*
				if(request.isRedirected()){
					def uri = grailsAttributes.getControllerActionUri(request)
					println(uri)
				}
				*/

				
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
							def method = cache["${params.action}"]['method']
							if(!apiToolkitService.isRequestMatch(Method["${method}"])){
								return false
							}
						}else{
							return false
						}
					}
				}
			}
			
			after = { Map model ->

				params.action = (params.action)?params.action:'index'
				
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', params.controller)
				def cache = (params.controller)?apiCacheService.getApiCache(params.controller):null
				
				def newModel

				if(cache){
					if(cache["${params.action}"]){
							
							/*
							 * if api call, send api data
							 */
							if (apiToolkitService.isApiCall()) {
								response.setHeader('Allow', cache["${params.action}"]['apiRoles'])
								switch(params.format){
									case 'JSON':
										response.setHeader('Content-Type', 'application/json;charset=UTF-8')
										break;
									case 'XML':
										response.setHeader('Content-Type', 'application/xml;charset=UTF-8')
										break;
									case 'HTML':
										response.setHeader('Content-Type', 'application/html;charset=UTF-8')
										break;
								}
								
								newModel = (grailsApplication.isDomainClass(model.getClass()))?model:apiToolkitService.formatModel(model)
								String format = params.format
								def method = cache["${params.action}"]['method'].intersect(request.method)
								def queryString = request.'javax.servlet.forward.query_string'
								def path = (queryString)?queryString.split('&'):[]
								
								def lastKey
								if(method){
									switch(method) {
										case 'HEAD':
											apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											break;
										case 'OPTIONS':
											//apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											switch(params.format){

												case 'XML':
													render(text:cache["${params.action}"]['doc'] as XML, contentType: "application/json")
													break
												case 'JSON':
												default:
													render(text:cache["${params.action}"]['doc'] as JSON, contentType: "application/json")
													break
											}
											break;
										case 'GET':
											apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											if(!newModel.isEmpty()){
												switch(params.format){
													case 'JSON':
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
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
																		uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																		redirect(uri: "${uri}")
																	}
																}
															}
														}
														
														render(text:map as JSON, contentType: "application/json")
														//return false
														break
													case 'XML':
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
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
																		uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																		redirect(uri: "${uri}")
																	}
																}
															}
														}
					
														render(text:map as XML, contentType: "application/xml")
														//return false
														break
													case 'HTML':
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
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
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
																		def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}/${belongsTo[0].toLowerCase()+belongsTo.substring(1)}/show/${v}"
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
											apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											switch(params.format){
												case 'JSON':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as JSON, contentType: "application/json")
													break
												case 'XML':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
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
											apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											switch(params.format){
												case 'JSON':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													
													render(text:map as JSON, contentType: "application/json")
													break
												case 'XML':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
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
											apiToolkitService.setApiHeaders(params.format,cache["${params.action}"]['method'])
											switch(params.format){
												case 'JSON':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
																	uri += (params.id)?"${pathKey}/${params.id}":"${pathKey}"
																	redirect(uri: "${uri}")
																}
															}
														}
													}
													return response.status
													break;
												case 'XML':
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
																	def uri = "/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/${format}"
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
		
