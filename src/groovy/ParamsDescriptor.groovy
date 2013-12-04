package net.nosegrind.restrpc;

@grails.validation.Validateable
class ParamsDescriptor {

	String paramType
	String name
	String domain =  "";
	String description
	String[] roles = [];
	boolean required = true
	String mockData = ""
	ParamsDescriptor[] values = []

	static constraints = { 
		paramType(nullable:false,maxSize:100)
		name(nullable:false,maxSize:100)
		domain(nullable:false,maxSize:100)
		description(nullable:false,maxSize:1000)
		roles(nullable:true)
		mockData(nullable:true)
		values(nullable:true)
	} 
}