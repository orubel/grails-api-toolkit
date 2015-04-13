import net.nosegrind.apitoolkit.ApiCacheService;
import net.nosegrind.apitoolkit.ApiObjectService


class ApiBootStrap {
	
	def grailsApplication
	def apiObjectService
	def apiCacheService
	def mongoCacheService
	
	def init = { servletContext ->
		if(grailsApplication.config.apitoolkit.sharedCache.type){
			switch(grailsApplication.config.apitoolkit.sharedCache.type){
				case 'MongoDB':
				case 'mongodb':
				case 'mongoDB':
					mongoCacheService.initialize()
					break;
			}
		}

		apiObjectService.initApiCache()
	}

    def destroy = {}

}
