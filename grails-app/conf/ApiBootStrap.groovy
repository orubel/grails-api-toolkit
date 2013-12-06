import net.nosegrind.restrpc.ApiDescriptor;
import net.nosegrind.restrpc.ParamsDescriptor;
import net.nosegrind.restrpc.ErrorCodeDescriptor;

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
