
class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
		"/login/full"(controller:'login',action:'full', parseRequest: true)
		"/$apiName_$apiVersion/$controller/$action?/$id" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/hook" (controller:'hook',action:'list', parseRequest: true)

	}
}