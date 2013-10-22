
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
				def newModel = restRPCService.formatModel(model)

				
				switch(anno.method()) {
					case RestMethod.GET:
						if(restRPCService.isRequestMatch('GET')){
							if(!newModel.isEmpty()){
								switch(params.format){
									case 'JSON':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
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
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
												}
											}
										}
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
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
												}
											}
										}
										render(text:map as JSON, contentType: "application/json")
										break
									case 'XML':
										def map = newModel
										if(params?.path){
											params.path.each{
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
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
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
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
												if(map?."${it}"){
													map = (map?."${it}" in java.util.Collection)?map."${it}":[map."${it}"]
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
