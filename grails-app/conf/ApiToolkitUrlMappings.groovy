
class ApiToolkitUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = getGrailsApplication().metadata['app.version']
		
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
		
		"/$apiName/$apiVersion/$format/webhook/api/${id}" (controller:'webhook',action:'api', parseRequest: true)
		"/$apiName/$apiVersion/$format/webhook/api" (controller:'webhook',action:'api', parseRequest: true)
		"/$apiName/$apiVersion/$format/webhook/${id}" (controller:'webhook',action:'api', parseRequest: true)
		"/$apiName/$apiVersion/$format/webhook" (controller:'webhook',action:'api', parseRequest: true)
	}
}