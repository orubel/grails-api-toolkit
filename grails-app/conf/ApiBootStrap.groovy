import net.nosegrind.apitoolkit.ApiObjectService

class ApiBootStrap {
	
	def grailsApplication
	def apiObjectService
	
	def init = { servletContext ->
		apiObjectService.initApiCache()
	}

    def destroy = {}

}
