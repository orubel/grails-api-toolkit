
class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
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

		"/hook/" (controller:'hook',action:'list', parseRequest: true)
	}
}