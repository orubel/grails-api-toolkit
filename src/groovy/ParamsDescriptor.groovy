package net.nosegrind.restrpc;

@grails.validation.Validateable
class ParamsDescriptor {

	String paramType
	String name
	String idReferences
	String description
	String[] roles = [];
	boolean required = true
	String mockData = ""
	ParamsDescriptor[] values = []

	static constraints = { 
		paramType(nullable:false,maxSize:100,inList: ["PKEY", "FKEY", "INDEX","STRING","LONG","BOOLEAN","FLOAT","BIGDECIMAL","EMAIL","URL"])
		name(nullable:false,maxSize:100)
		idReferences(nullable:true,maxSize:100)
		description(nullable:false,maxSize:1000)
		roles(nullable:true)
		mockData(nullable:true)
		values(nullable:true)
	} 
}