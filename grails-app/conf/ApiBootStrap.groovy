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
	
	def grailsApplication
	def apiObjectService
	
	def init = { servletContext ->
		apiObjectService.initApiCache()
	}


    def destroy = {}

}
