
import grails.converters.JSON
import grails.converters.XML
import net.nosegrind.restrpc.RestRPC
import net.nosegrind.restrpc.RpcMethod

class RestRPCFilters {
	
	def restRPCService
	
	def filters = {
		restrpc(controller:'*', action:'*'){
			after = { Map model ->
				// IF THIS IS AN API REQUEST, WE PROCESS ELSE WE IGNORE
				if (!restRPCService.isApiCall()) {
					return
				}

				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllerName)
				def action = controller?.getClazz()?.getDeclaredMethod(actionName)
				// IF THERE IS AN ACTION, WE PROCESS ELSE WE IGNORE
				if (!action) {
					return
				}

				// IF THERE IS AN ANNOTATION ON SAID ACTION WE CONTINUE TO PROCESS
				if (!action.isAnnotationPresent(RestRPC)) {
					return
				}

				def anno = action.getAnnotation(RestRPC)
				
				switch(anno.request()) {
					case RpcMethod.GET:
						def newModel = restRPCService.formatModel(model)
						if(restRPCService.isRequestMatch('GET')){
							if(!newModel.isEmpty()){
								switch(params.format){
									case 'JSON':
										render(text:newModel as JSON, contentType: "application/json")
										return false
										break
									case 'XML':
										render(text:newModel as XML, contentType: "application/xml")
										return false
										break
								}
							}
						}
						break
					case RpcMethod.PUT:
						def newModel = restRPCService.formatModel(model)
						if(restRPCService.isRequestMatch('PUT')){
								switch(params.format){
									case 'JSON':
										render(text:newModel as JSON, contentType: "application/json")
										break
									case 'XML':
										render(text:newModel as XML, contentType: "application/xml")
										return false
										break
								}
						}
						break
					case RpcMethod.POST:
						println("POST METHOD")
						def newModel = restRPCService.formatModel(model)
						if(restRPCService.isRequestMatch('POST')){
								switch(params.format){
									case 'JSON':
										render(text:newModel as JSON, contentType: "application/json")
										return false
										break
									case 'XML':
										render(text:newModel as XML, contentType: "application/xml")
										return false
										break
								}
						}
						break
					case RpcMethod.DELETE:
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
