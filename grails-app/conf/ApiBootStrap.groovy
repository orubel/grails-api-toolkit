import net.nosegrind.apitoolkit.ApiCacheService;
import net.nosegrind.apitoolkit.ApiObjectService


class ApiBootStrap {
	
	def grailsApplication
	def apiObjectService
	def apiCacheService

	
	def init = { servletContext ->
		apiObjectService.initialize()
		def test = apiCacheService.getCacheNames()
	}

    def destroy = {}

}
