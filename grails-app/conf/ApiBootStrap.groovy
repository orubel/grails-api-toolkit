import net.nosegrind.apitoolkit.ApiDescriptor;
import net.nosegrind.apitoolkit.ErrorCodeDescriptor;
import net.nosegrind.apitoolkit.ParamsDescriptor;

class ApiBootStrap {
	
	def springSecurityService
	def grailsApplication
	def restRPCService
	def apiCacheService
	
	def init = { servletContext ->
		//restRPCService.flushAllApiCache()
	}

    def destroy = {}

}
