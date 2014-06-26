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
import grails.util.Holders

class ApiToolkitUrlMappings {

	static mappings = {
		String apiName = grails.util.Holders.getGrailsApplication().config.apitoolkit.apiName
		String apiVersion = grails.util.Holders.getGrailsApplication().metadata['app.version']
		
		"/apidoc/show" (controller:'apidoc',action:'show', parseRequest: true)
		"/hook" (controller:'hook',action:'list', parseRequest: true)
		
		if(apiName){
			"/$apiName_v$apiVersion-$apiObjectVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				//parseRequest = true
				constraints {
					apiObjectVersion(matches:/^[0-9]?[0-9]?(\\.[0-9][0-9]?)?/)
				}
			}
			
			"/$apiName_v$apiVersion/$controller/$action?/$id**" {
				controller = controller
				action = action
				//parseRequest = true
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
	}
}