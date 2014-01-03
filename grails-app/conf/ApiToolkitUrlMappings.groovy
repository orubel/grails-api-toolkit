
class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
		"/login/full"(controller:'login',action:'full', parseRequest: true)
		"/$apiName/$apiVersion/$format/$c/$a?/$id" {
			controller = c
			action = a
			parseRequest = true
		}
		/*
		"/$apiName/$apiVersion/apidoc?/show" {
			controller = 'apidoc'
			action = 'show'
			parseRequest = true
		}
		"/apidoc?/show"(uri: "/$apiName/$apiVersion/apidoc/show.dispatch")
		*/
		
		"/hook/" (controller:'hook',action:'list', parseRequest: true)

	}
}