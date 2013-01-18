
import net.nosegrind.restrpc.RestRPC
import net.nosegrind.restrpc.RpcMethod

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.commons.GrailsControllerClass

class RestRPCFilters {
	
	def restRPCService
	
	def filters = {
		restrpc(controller:'*', action:'*'){
			after = { Map model ->

				if(restRPCService.isApiCall()){
					def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllerName)
					def action = controller?.getClazz()?.getDeclaredMethod(actionName)

					if(action){
						if(action.isAnnotationPresent(RestRPC)){
							def anno = action.getAnnotation(RestRPC)
							
							def newModel = restRPCService.formatModel(model)

							switch(anno.request()) {
								case RpcMethod.GET:
									println("###### GET METHOD #######")
									if(restRPCService.isRequestMatch('GET')){
										switch(params.format){
											case 'JSON':
												render text:newModel as JSON, contentType: "application/json"
												break
											case 'XML':
												render text:newModel as XML, contentType: "application/xml"
												break
										}
									}
									break
							}
							return false
						} else {
							// ANNOTATION IS NOT PRESENT FOR ACTION $action.name
						}
					}else{
						// ACTION IS NOT PRESENT
					}
					
				}
			}
		}
	}
}