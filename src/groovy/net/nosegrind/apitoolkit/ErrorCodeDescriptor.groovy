/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit;

import grails.validation.Validateable
import grails.compiler.GrailsCompileStatic

@Validateable
//@GrailsCompileStatic
class ErrorCodeDescriptor {

	Integer code
	String description

	static constraints = { 
		code(nullable:false,inList: [200, 304, 400,403,404,405,409,412,413,416,500,503])
		description(nullable:false,maxSize:1000)
	} 
}