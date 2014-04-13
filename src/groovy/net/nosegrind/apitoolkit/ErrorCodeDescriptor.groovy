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
package net.nosegrind.apitoolkit;

import grails.validation.Validateable

//@grails.validation.Validateable
@Validateable
class ErrorCodeDescriptor {

	String code
	String description

	static constraints = { 
		code(nullable:false,inList: ["200", "304", "400","403","404","404","405","409","412","413","416","500","503"])
		description(nullable:false,maxSize:1000)
	} 
}