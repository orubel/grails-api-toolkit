class RestRpcUrlMappings {
	static mappings = {
		//"/api/$format/$id" (controller:"${controller}",action:"${action}", parseRequest: true)
		//"/api/$format" (controller:"${controller}",action:"${action}", parseRequest: true)
		
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
