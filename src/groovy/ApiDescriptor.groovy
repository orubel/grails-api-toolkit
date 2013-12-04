package net.nosegrind.restrpc;

import net.nosegrind.restrpc.ErrorCode;
import net.nosegrind.restrpc.Params;

@grails.validation.Validateable
class ApiDescriptor {

	String method
    String description
    ParamsDescriptor[] receives
    ParamsDescriptor[] returns
    ErrorCode[] errors

	static constraints = { 
		method(nullable:false,inList: ["GET", "POST", "PUT","DELETE"])
		description(nullable:false,maxSize:1000)
		receives(nullable:true)
		returns(nullable:true)
		errors(nullable:true)
	} 
}