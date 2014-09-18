package net.nosegrind.apitoolkit

/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/

import net.nosegrind.apitoolkit.ErrorCodeDescriptor
import net.nosegrind.apitoolkit.ParamsDescriptor
import grails.validation.Validateable

// name is name of the object used

//@grails.validation.Validateable
@Validateable
class ApiDescriptor {
	String defaultAction
	List deprecated
	String method
	List roles
	List batchRoles
	String name
    String description
	Map doc
    LinkedHashMap<String,ParamsDescriptor> receives
    LinkedHashMap<String,ParamsDescriptor> returns
    ErrorCodeDescriptor[] errorcodes

	static constraints = { 
		defaultAction(nullable:false)
		deprecated(nullable:true)
		method(nullable:false,inList: ["GET","POST","PUT","DELETE"])
		roles(nullable:true)
		batchRoles(nullable:true, validator: { val, obj ->
			if (batchRoles){
				if(obj.roles.containsAll(batchRoles)) {
				  return true
				}else {
				  return false
				}
			}
		})
		name(nullable:false,maxSize:200)
		description(nullable:true,maxSize:1000)
		doc(nullable:true)
		receives(nullable:true)
		returns(nullable:true)
		errorcodes(nullable:true)
	}

}
