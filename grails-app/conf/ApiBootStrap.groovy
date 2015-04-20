import net.nosegrind.apitoolkit.ApiCacheService;
import net.nosegrind.apitoolkit.ApiObjectService

import org.codehaus.groovy.grails.plugins.GrailsPluginManager

class ApiBootStrap {
	
	def grailsApplication
	def apiObjectService
	def apiCacheService
	def mongoCacheService
	
	def init = { servletContext ->
		if(grailsApplication.config.apitoolkit.sharedCache.type && grailsApplication.config.apitoolkit.master==true){
			switch(grailsApplication.config.apitoolkit.sharedCache.type){
				case 'MongoDB':
				case 'mongodb':
				case 'mongoDB':
					mongoCacheService.initialize()
					break;
			}
		}
		if(grailsApplication.config.apitoolkit.slave==true || grailsApplication.config.apitoolkit.master==false){
			apiObjectService.initApiCache()
		}

	}

    def destroy = {}

}
