import net.nosegrind.apitoolkit.ApiDescriptor
import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import java.lang.reflect.Method

import net.nosegrind.apitoolkit.Api
import net.nosegrind.apitoolkit.ApiDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ApiErrors
import net.nosegrind.apitoolkit.ApiParams

import net.nosegrind.apitoolkit.ApiCacheService
import net.nosegrind.apitoolkit.ApiToolkitService

class ApiBootStrap {
	
	def springSecurityService
	def grailsApplication
	def apiCacheService
	def apiToolkitService
	
	def init = { servletContext ->
		grailsApplication.controllerClasses.each { controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				//def cont = apiCacheService.getApiCache(controllername)
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
				for (Method method : controller.getClazz().getMethods()){
					def actionname = method.getName()
					if(method.isAnnotationPresent(Api)) {
						def api = method.getAnnotation(Api)
						
						ApiErrors error = new ApiErrors()
						ApiParams param = new ApiParams()

						def service = new ApiDescriptor(
							"method":"${api.method()}",
							"description":'',
							"receives":[],
							"doc": apiToolkitService.generateApiDoc(controllername,actionname)
						)
						apiCacheService.setApiCache("${controllername}","${actionname}",service)
					}
				}
			}
		}
	}

    def destroy = {}

}
