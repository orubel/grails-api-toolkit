
class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
		"/login/full"(controller:'login',action:'full', parseRequest: true)
		"/$apiName/$apiVersion/$format/$controller/$action" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/$apiName/$apiVersion/$format/$controller/$action/$id" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/$apiName/$apiVersion/apidoc"(controller:'apidoc',action:'show',parseRequest:true)
		"/$apiName/$apiVersion/apidoc/show"(controller:'apidoc',action:'show',parseRequest:true)
		
		"/$apiName/$apiVersion/$format/hook/api/$id" (controller:'hook',action:'api', parseRequest: true)
		"/$apiName/$apiVersion/$format/hook/api" (controller:'hook',action:'api', parseRequest: true)
		"/$apiName/$apiVersion/$format/hook" (controller:'hook',action:'list', parseRequest: true)
	}
}