import grails.util.Holders

class ApiToolkitUrlMappings {

	static mappings = {
		String apiName = grails.util.Holders.getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = grails.util.Holders.getGrailsApplication().metadata['app.version']

		"/$apiName_v$apiVersion/$controller/$action?/$id**" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/$apiName_v$apiVersion/$controller/$action" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/v$apiVersion/$controller/$action?/$id**" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/v$apiVersion/$controller/$action" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/hook" (controller:'hook',action:'list', parseRequest: true)

	}
}