
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
				
				switch(anno.method()) {
					case RestMethod.GET:
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
					case RestMethod.PUT:
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
					case RestMethod.POST:
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
