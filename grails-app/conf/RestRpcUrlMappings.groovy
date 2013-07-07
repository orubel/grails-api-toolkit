class RestRpcUrlMappings {
	static mappings = {
		//"/api/$format/$id" (controller:"${controller}",action:"${action}", parseRequest: true)
		//"/api/$format" (controller:"${controller}",action:"${action}", parseRequest: true)
		
		String apiName = getGrailsApplication().config.apiName
		String apiVersion = getGrailsApplication().config.apiName

		"/$apiName/$apiVersion/$controller/$action" {
			controller = "${controllerClass.name}"
			action = action
			parseRequest = true
		}
		
		"/$apiName/$version/$controller/$action/$id" {
			controller = controller
			action = action
			id = id
			parseRequest = true
		}
		
		"/$apiName/$version/apidocs" {
			controller = 'apidocs'
			action = 'index'
			parseRequest = true
		}
		
		// OLD MAPPINGS KEPY FOR BACKWARDS COMPATIBILITY
		"/restrpc/$controller/$action/$format/$id" {
			controller = controller
			action = action
			parseRequest = true
		}
		
		"/restrpc/$controller/$action/$format" {
			controller = controller
			action = action
			parseRequest = true
		}
	}
}
