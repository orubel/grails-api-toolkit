
import net.nosegrind.restrpc.RestRPC
import net.nosegrind.restrpc.RpcMethod

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.commons.GrailsControllerClass

class RestRPCFilters {
	
	def restRPCService
	
	def filters = {
		restrpc(controller:'*', action:'*'){
			after = { model ->

				//if(restRPCService.isApiCall()){
					def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllerName)
					def action = controller?.getClazz()?.getDeclaredMethod(actionName)

					if(action){
						if(action.isAnnotationPresent(RestRPC)){
							def anno = action.getAnnotation(RestRPC)
							switch(anno.request()) {
								case RpcMethod.GET:
									println("###### GET METHOD #######")
									if(restRPCService.isRequestMatch('GET')){
										switch(params.format){
											case 'JSON':
												render model as JSON
												break
											case 'XML':
												render model as XML
												break
										}
									}
									break
							}
						} else {
							println(action)
							println "ANNOTATION IS NOT PRESENT FOR ACTION $action.name"
						}
					}else{
						println(action)
						println "ACTION IS NOT PRESENT FOR ACTION $action.name"
					}

				//}
			}
		}
	}
}