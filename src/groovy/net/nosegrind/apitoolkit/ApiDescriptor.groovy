package net.nosegrind.apitoolkit

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

import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import grails.validation.Validateable

// name is name of the object used

//@grails.validation.Validateable
@Validateable
class ApiDescriptor {

	String method
	List roles
	String name
    String description
	Map doc
    LinkedHashMap<String,ParamsDescriptor> receives
    LinkedHashMap<String,ParamsDescriptor> returns
    ErrorCodeDescriptor[] errorcodes

	static constraints = { 
		method(nullable:false,inList: ["GET","POST","PUT","DELETE"])
		roles(nullable:true)
		name(nullable:false,maxSize:200)
		description(nullable:true,maxSize:1000)
		doc(nullable:true)
		receives(nullable:true)
		returns(nullable:true)
		errorcodes(nullable:true)
	}

}
