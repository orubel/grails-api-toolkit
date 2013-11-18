
class RestRpcUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.restrpc.apiName
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
		
		"/$apiName/$apiVersion/apidoc" {
			controller = 'apidoc'
			parseRequest = true
		}
		"/$apiName/$apiVersion/apidoc/show" {
			controller = 'apidoc'
			action = 'show'
			parseRequest = true
		}
	}
}
