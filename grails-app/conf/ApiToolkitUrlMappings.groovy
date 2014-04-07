class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
		"/$apiName_v$apiVersion/$controller/$action?/$id**" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/v$apiVersion/$controller/$action?/$id**" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/hook" (controller:'hook',action:'list', parseRequest: true)

	}
}