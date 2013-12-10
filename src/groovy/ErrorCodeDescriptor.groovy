package net.nosegrind.restrpc;

import grails.validation.Validateable

@grails.validation.Validateable
class ErrorCodeDescriptor {

	String code
	String description

	static constraints = { 
		code(nullable:false,inList: ["200", "304", "400","403","404","404","405","409","412","413","416","500","503"])
		description(nullable:false,maxSize:1000)
	} 
}