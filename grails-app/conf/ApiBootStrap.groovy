import net.nosegrind.restrpc.ApiDescriptor;
import net.nosegrind.restrpc.ParamsDescriptor;
import net.nosegrind.restrpc.ErrorCodeDescriptor;

class ApiBootStrap {
	
	def springSecurityService
	def grailsApplication
	def restRPCService
	def apiCacheService
	
	def init = { servletContext ->
		evaluate(new File("../src/groovy/net/nosegrind/restrpc/ApiDocs.groovy"))
		//restRPCService.flushAllApiCache()
	}

    def destroy = {}

}
