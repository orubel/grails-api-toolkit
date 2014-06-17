import grails.util.Holders

class ApiToolkitUrlMappings {

	static mappings = {
		String apiName = grails.util.Holders.getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = grails.util.Holders.getGrailsApplication().metadata['app.version']
		
		if(apiName){
			"/$apiName_v$apiVersion-$apiObjectVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/$apiName_v$apiVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				parseRequest = true
			}
			
			"/$apiName_v$apiVersion-$apiObjectVersion/$controller/$action" {
				controller = controller
				action = action
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/$apiName_v$apiVersion/$controller/$action" {
				controller = controller
				action = action
				parseRequest = true
			}
		}else{
			"/v$apiVersion-$apiObjectVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/v$apiVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				parseRequest = true
			}
			
			"/v$apiVersion-$apiObjectVersion/$controller/$action" {
				controller = controller
				action = action
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/v$apiVersion/$controller/$action" {
				controller = controller
				action = action
				parseRequest = true
				
			}
		}

		"/apidoc/show" (controller:'apidoc',action:'show', parseRequest: true)
		"/hook" (controller:'hook',action:'list', parseRequest: true)

	}
}