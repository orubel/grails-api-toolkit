class RestRpcUrlMappings {

	static grailsApplication

	static mappings = {
		String apiName = getGrailsApplication().config.restrpc.apiName
		String apiVersion = getGrailsApplication().config.restrpc.apiVersion
		
		"/$apiName/$apiVersion/$controller/$action/$format/" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/$apiName/$apiVersion/$controller/$action/$format/$id" {
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
