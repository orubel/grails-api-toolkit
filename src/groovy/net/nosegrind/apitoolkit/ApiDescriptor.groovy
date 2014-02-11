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

import net.nosegrind.apitoolkit.ErrorCodeDescriptor;
import net.nosegrind.apitoolkit.ParamsDescriptor;
import grails.validation.Validateable

//@grails.validation.Validateable
@Validateable
class ApiDescriptor {

	String method
	List apiRoles
	List hookRoles
	String name
    String description
	Map doc
    ParamsDescriptor[] receives
    ParamsDescriptor[] returns
    ErrorCodeDescriptor[] errorcodes

	static constraints = { 
		method(nullable:true,inList: ["GET","POST","PUT","DELETE"])
		apiRoles(nullable:true)
		name(nullable:true,maxSize:500)
		description(nullable:true,maxSize:1000)
		doc(nullable:true)
		receives(nullable:true)
		returns(nullable:true)
		errorcodes(nullable:true)
	} 
}