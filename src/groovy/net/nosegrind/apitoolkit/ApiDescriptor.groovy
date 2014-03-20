package net.nosegrind.apitoolkit

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