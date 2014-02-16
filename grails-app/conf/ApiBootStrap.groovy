import net.nosegrind.apitoolkit.ApiDescriptor
import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

import net.nosegrind.apitoolkit.Api
import net.nosegrind.apitoolkit.ApiDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ApiStatuses
import net.nosegrind.apitoolkit.ApiParams

import net.nosegrind.apitoolkit.ApiCacheService
import net.nosegrind.apitoolkit.ApiToolkitService

class ApiBootStrap {
	
	def springSecurityService
	def grailsApplication
	def apiCacheService
	def apiToolkitService
	
	def init = { servletContext ->
		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			Map methods = [:]
			controllerClass.getClazz().methods.each { Method method ->
				String actionname = method.getName()
				
				if(method.isAnnotationPresent(Api)) {
					def api = method.getAnnotation(Api)
					
					ApiStatuses error = new ApiStatuses()
					ApiParams param = new ApiParams()

					ApiDescriptor service = new ApiDescriptor(
						"method":"${api.method()}",
						"description":'',
						"receives":[],
						"doc":[:],
						"links":[]
					)
					service['apiRoles'] = api.apiRoles()
					if(api.hookRoles()){
						service['hookRoles'] = api.hookRoles()
					}
					
					methods["${actionname}"] = service
				}
			}

			if(methods){
				String controller = controllername.toString()
				apiCacheService.setApiCache(controller,methods)
			}
		}
	}

    def destroy = {}

}
