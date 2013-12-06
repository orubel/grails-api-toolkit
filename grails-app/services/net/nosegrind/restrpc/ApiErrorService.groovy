package net.nosegrind.restrpc

import net.nosegrind.restrpc.ErrorCodeDescriptor

import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiErrorService{

	def grailsApplication
	def springSecurityService

	static transactional = false

	ErrorCodeDescriptor error
	
	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	/*
	 * Error messages
	 * For complete list of messages, see http://msdn.microsoft.com/en-us/library/windowsazure/dd179357.aspx
	 */
	def send(){
		def response = getResponse()
		response.setStatus(error.code,error.description)
		return
	}
	
	def toObject(){
		return this.error
	}
	

	def _200_SUCCESS(String msg){
		error = new ErrorCodeDescriptor(code:200,description:"[Success] : ${msg}")
		return
	}

	def _200_SUCCESS(){
		error = new ErrorCodeDescriptor(code:200,description:"[Success]")
		return
	}

	// 304 not modified
	def _304_NOT_MODIFIED(String msg){
		error = new ErrorCodeDescriptor(code:304,description:"[Not Modified] : ${msg}")
		return
	}
	def _304_NOTMODIFIED(){
		error = new ErrorCodeDescriptor(code:304,description:"[Not Modified]")
		[toObject: { 
			println(error)
		 }]
		[send: {
			def response = getResponse()
			response.setStatus(304,"[Not Modified]")
		 }]
		return
	}

	// 400 bad request
	def _400_BAD_REQUEST(String msg){
		error = new ErrorCodeDescriptor(code:400,description:"[Bad Request] : ${msg}")
		return
	}
	def _400_BAD_REQUEST(){
		error = new ErrorCodeDescriptor(code:400,description:"[Bad Request]")
		return
	}
	
	// 403 forbidden
	def _403_FORBIDDEN(String msg){
		error = new ErrorCodeDescriptor(code:403,description:"[Forbidden] : ${msg}")
		return
	}
	def _403_FORBIDDEN(){
		error = new ErrorCodeDescriptor(code:403,description:"[Forbidden]")
		return
	}
	
	// 404 not found
	def _404_NOT_FOUND(String msg){
		error = new ErrorCodeDescriptor(code:404,description:"[Not Found] : ${msg}")
		return
	}
	def _404_NOT_FOUND(){
		error = new ErrorCodeDescriptor(code:404,description:"[Not Found]")
		return
	}

	// UNSUPPORTED METHOD
	def _405_UNSUPPORTED_METHOD(String msg){
		error = new ErrorCodeDescriptor(code:405,description:"[Unsupported Method] : ${msg}")
		return
	}
	def _405_UNSUPPORTED_METHOD(){
		error = new ErrorCodeDescriptor(code:405,description:"[Unsupported Method]")
		return
	}
	
	// ACCOUNT CONFLICT
	def _409_ACCOUNT_CONFLICT(String msg){
		error = new ErrorCodeDescriptor(code:409,description:"[Account Conflict] : ${msg}")
		return
	}
	def _409_ACCOUNT_CONFLICT(){
		error = new ErrorCodeDescriptor(code:409,description:"[Account Conflict]")
		return
	}
	
	// ConditionNotMet
	def _412_CONDITION_NOT_MET(String msg){
		error = new ErrorCodeDescriptor(code:412,description:"[Condition Not Met] : ${msg}")
		return
	}
	def _412_CONDITION_NOT_MET(){
		error = new ErrorCodeDescriptor(code:412,description:"[Condition Not Met] ")
		return
	}
	
	// RequestBodyTooLarge
	def _413_REQUEST_BODY_TOO_LARGE(String msg){
		error = new ErrorCodeDescriptor(code:413,description:"[Request Body Too Large] : ${msg}")
		return
	}
	def _413_REQUEST_BODY_TOO_LARGE(){
		error = new ErrorCodeDescriptor(code:413,description:"[Request Body Too Large]")
		return
	}
	
	// InvalidRange
	def _416_INVALID_RANGE(String msg){
		error = new ErrorCodeDescriptor(code:416,description:"[Invalid Range] : ${msg}")
		return
	}
	def _416_INVALID_RANGE(){
		error = new ErrorCodeDescriptor(code:416,description:"[Invalid Range]")
		return
	}
	
	// SERVER ERROR
	def _500_SERVER_ERROR(String msg){
		error = new ErrorCodeDescriptor(code:500,description:"[Server Error] : ${msg}")
		return
	}
	def _500_SERVER_ERROR(){
		error = new ErrorCodeDescriptor(code:500,description:"[Server Error]")
		return
	}
	
	// SERVICE UNAVAILABLE
	def _503_UNAVAILABLE(String msg){
		error = new ErrorCodeDescriptor(code:503,description:"[Service Unavailable] : ${msg}")
		return
	}
	def _503_UNAVAILABLE(){
		error = new ErrorCodeDescriptor(code:503,description:"[Service Unavailable]")
		return
	}

}
