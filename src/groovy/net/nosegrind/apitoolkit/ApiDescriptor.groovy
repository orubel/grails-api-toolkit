package net.nosegrind.apitoolkit;

import net.nosegrind.apitoolkit.ErrorCodeDescriptor;
import net.nosegrind.apitoolkit.ParamsDescriptor;
import grails.validation.Validateable

//@grails.validation.Validateable
@Validateable
class ApiDescriptor {

	String method
    String description
    ParamsDescriptor[] receives
    ParamsDescriptor[] returns
    ErrorCodeDescriptor[] errorcodes

	static constraints = { 
		method(nullable:false,inList: ["GET", "POST", "PUT","DELETE"])
		description(nullable:false,maxSize:1000)
		receives(nullable:true)
		returns(nullable:true)
		errorcodes(nullable:true)
	} 
}