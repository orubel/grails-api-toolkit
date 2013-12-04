

class ApiBootStrap {
	
	def springSecurityService
	def grailsApplication
	def restRPCService
	
	def init = { servletContext ->
		restRPCService.flushAllApiCache()
	}

    def destroy = {}

}
