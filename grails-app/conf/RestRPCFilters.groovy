
import java.util.Map;

import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.restrpc.Api
import net.nosegrind.restrpc.RestMethod

class RestRPCFilters {
	
	def restRPCService
	
	def filters = {
		restrpc(controller:'*', action:'*'){
			after = { Map model ->
				// IF THIS IS AN API REQUEST, WE PROCESS ELSE WE IGNORE
				if (!restRPCService.isApiCall()) {
					return
				}
				
				if(params?.path?.trim()){
					params.path = params.path.split("/")
					// object.(params.path.join('.'))
				}
				
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllerName)
				def action = controller?.getClazz()?.getDeclaredMethod(actionName)
				// IF THERE IS AN ACTION, WE PROCESS ELSE WE IGNORE
				if (!action) {
					return
				}

				// IF THERE IS AN ANNOTATION ON SAID ACTION WE CONTINUE TO PROCESS
				if (!action.isAnnotationPresent(Api)) {
					return
				}
				
				def anno = action.getAnnotation(Api)

				def newModel
				if(grailsApplication.isDomainClass(model.getClass())){
					newModel = model
				}else{
					newModel = restRPCService.formatModel(model)
				}

				def lastKey
				switch(anno.method()) {
					case RestMethod.GET:
						if(restRPCService.isRequestMatch('GET')){
							if(!newModel.isEmpty()){
								switch(params.format){
									case 'JSON':
										def map = newModel
										def key
										if(params?.path){
											if(grailsApplication.isDomainClass(map.getClass())){
												map = map.(params.path.join('.'))
											}
										}else{
											if(map in Set){
												Map newMap = [1 : map.toArray()]
												map = [1 : map.toArray()]
											}
										}
											/*
											params.path.each{
												if(grailsApplication.isDomainClass(it.getClass())){
													if(map.containsKey(it)){
														map = (map.containsKey(it))?map."${it}":[map."${it}"]
													}else{
														if(it.getClass()==String){
															if(it.isNumber()){
																if(map."${lastKey}".id == it.toLong()){
																	key=it
																	map = map."${lastKey}".find { id.value == it.toLong()}
																	//key=it
																	//map = map."${lastKey}"
																}
															}else{
																if(map."${it}"){
																	key=it
																	map = map."${it}"
																}
															}
														}
													}
												}else{
													if(it.getClass()==String){
														if(map."${it}"){
															key=it
															map = map."${it}"
														}
													}
												}
												if(map in Set){
													Map newMap = [1 : map.toArray()]
													map = [1 : map.toArray()]
												}
												lastKey=it
											}
											
										}
								*/
										render(text:map as JSON, contentType: "application/json")
										return false
										break
									case 'XML':
										def map = newModel
										def key
										if(params?.path){
											if(grailsApplication.isDomainClass(map.getClass())){
												map = map.(params.path.join('.'))
											}
										}else{
											if(map in Set){
												Map newMap = [1 : map.toArray()]
												map = [1 : map.toArray()]
											}
										}
										/*
										if(params?.path){
											params.path.each{
												if(grailsApplication.isDomainClass(it.getClass())){
													if(map.containsKey(it)){
														map = (map.containsKey(it))?map."${it}":[map."${it}"]
													}else{
														if(it.getClass()==String){
															if(it.isNumber()){
																if(map.getProperties("${lastKey}").getProperties('id') == it.toLong()){
																	key=it
																	map = map."${lastKey}".getProperties('id').get(it.toLong())
																}
															}else{
																if(map."${it}"){
																	key=it
																	map = map."${it}"
																}
															}
														}
													}
												}else{
													if(it.getClass()==String){
														if(map."${it}"){
															key=it
															map = map."${it}"
														}
													}
												}
												if(map in Set){
													Map newMap = [1 : map.toArray()]
													map = [1 : map.toArray()]
												}
												lastKey=it
											}
										}
										*/
										render(text:map as XML, contentType: "application/xml")
										return false
										break
								}
							}
						}
						break
					case RestMethod.PUT:
						if(restRPCService.isRequestMatch('PUT')){
								switch(params.format){
									case 'JSON':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map.containsKey("${it}")){
													map = (map.containsKey("${it}"))?map."${it}":[map."${it}"]
												}
											}
										}
										render(text:map as JSON, contentType: "application/json")
										break
									case 'XML':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map.containsKey("${it}")){
													map = (map.containsKey("${it}"))?map."${it}":[map."${it}"]
												}
											}
										}
										render(text:map as XML, contentType: "application/xml")
										return false
										break
								}
						}
						break
					case RestMethod.POST:
						if(restRPCService.isRequestMatch('POST')){
								switch(params.format){
									case 'JSON':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map.containsKey("${it}")){
													map = (map.containsKey("${it}"))?map."${it}":[map."${it}"]
												}
											}
										}
										render(text:map as JSON, contentType: "application/json")
										return false
										break
									case 'XML':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map.containsKey("${it}")){
													map = (map.containsKey("${it}"))?map."${it}":[map."${it}"]
												}
											}
										}
										render(text:map as XML, contentType: "application/xml")
										return false
										break
								}
						}
						break
					case RestMethod.DELETE:
						if(restRPCService.isRequestMatch('DELETE')){
								switch(params.format){
									case 'JSON':
									case 'XML':
										return response.status
										break
								}
						}
						break
				}
				return false
			}
		}
	}
}
