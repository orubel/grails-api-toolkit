import net.nosegrind.apitoolkit.ApiCacheService;
import net.nosegrind.apitoolkit.ApiObjectService


class ApiBootStrap {
	
	def grailsApplication
	def apiObjectService
	def apiCacheService
	def sharedCouchBaseService
	
	def init = { servletContext ->

		switch(grailsApplication.config.apitoolkit.sharedCache.type){
			case 'CouchBase':
			case 'couchbase':
			case 'couchBase':
				sharedCouchBaseService.initialize()
				break;
			case 'MongoDB':
			case 'mongodb':
			case 'mongoDB':
				break;
			
		}

		apiObjectService.initApiCache()
	}

    def destroy = {}

}
