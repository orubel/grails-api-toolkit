/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

import org.springframework.web.context.request.RequestContextHolder as RCH
 
class ApiToolkitUrlMappings {

	static mappings = {
		String apiName = grails.util.Holders.getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = grails.util.Holders.getGrailsApplication().metadata['app.version']
		"/apidoc/show" (controller:'apidoc',action:'show', parseRequest: true)
		"/hook/$action" {
			controller = 'hook'
			action = action
			parseRequest= true
		}
		"/login/$action" {
			controller = 'login'
			action = action
			parseRequest= true
		}
		"/logout?/$action" {
			controller = 'logout'
			action = action
			parseRequest= true
		}
		if(apiName){
			"/${apiName}_v${apiVersion}-$apiObjectVersion/$controller/$action/$id**" {
				controller = controller
				action = action
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/${apiName}_v${apiVersion}/$controller/$action/$id**" {
				controller = controller
				action = action
				parseRequest = true
			}
			
			"/${apiName}_v${apiVersion}-$apiObjectVersion/$controller/$action" {
				controller = controller
				if(action?.toInteger()==action && action!=null){
					id=action
					action = null
				}else{
					action=action
				}
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/${apiName}_v${apiVersion}/$controller/$action" {
				controller = controller
				if(action?.toInteger()==action && action!=null){
					id=action
					action = null
				}else{
					action=action
				}
				parseRequest = true
			}
			
		}else{
			"/v$apiVersion-$apiObjectVersion/$controller/$action/$id**" {
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

			"/v$apiVersion/$controller?/$id" {
				controller = controller
				parseRequest = true
			}
			
			"/v$apiVersion-$apiObjectVersion/$controller?/$id" {
				controller = controller
				parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
		}
		
		"403" {
			controller = "errors"
			parseRequest = true
		}
		"404" {
			controller = "errors"
			parseRequest = true
		}
		"405" {
			controller = "errors"
			parseRequest = true
		}
		"500" {
			controller = "errors"
			parseRequest = true
		}
	}
}