package net.nosegrind.apitoolkit

import net.nosegrind.apitoolkit.ErrorCodeDescriptor;

import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiStatuses{

	def grailsApplication
	def springSecurityService

	static transactional = false

	ErrorCodeDescriptor status
	
	private static final INSTANCE = new ApiStatuses()
	
	static getInstance(){ return INSTANCE }
	
	private ApiStatuses() {}
	
	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	/*
	 * Error messages
	 * For complete list of messages, see http://msdn.microsoft.com/en-us/library/windowsazure/dd179357.aspx
	 */
	def send(){
		def response = getResponse()
		response.setStatus(this.status.code.toInteger(),this.status.description)
		return
	}
	
	def toObject(){
		return this.status
	}
	

	def _200_SUCCESS(String msg){
		this.status = new ErrorCodeDescriptor(code:200,description:"[Success] : ${msg}")
		return this
	}

	def _200_SUCCESS(){
		this.status = new ErrorCodeDescriptor(code:200,description:"[Success]")
		return this
	}

	// 302 found
	def _304_FOUND(String msg){
		this.status = new ErrorCodeDescriptor(code:302,description:"[Found] : ${msg}")
		return this
	}
	
	def _304_FOUND(){
		this.status = new ErrorCodeDescriptor(code:302,description:"[Found]")
		return this
	}
	
	// 304 not modified
	def _304_NOT_MODIFIED(String msg){
		this.status = new ErrorCodeDescriptor(code:304,description:"[Not Modified] : ${msg}")
		return this
	}
	
	def _304_NOT_MODIFIED(){
		this.status = new ErrorCodeDescriptor(code:304,description:"[Not Modified]")
		return this
	}

	// 400 bad request
	def _400_BAD_REQUEST(String msg){
		status = new ErrorCodeDescriptor(code:400,description:"[Bad Request] : ${msg}")
		return this
	}
	
	def _400_BAD_REQUEST(){
		status = new ErrorCodeDescriptor(code:400,description:"[Bad Request]")
		return this
	}
	
	// 401 unauthorized
	def _401_UNAUTHORIZED(String msg){
		status = new ErrorCodeDescriptor(code:400,description:"[Bad Request] : ${msg}")
		return this
	}
	
	def _401_UNAUTHORIZED(){
		status = new ErrorCodeDescriptor(code:400,description:"[Bad Request]")
		return this
	}
	
	// 403 forbidden
	def _403_FORBIDDEN(String msg){
		status = new ErrorCodeDescriptor(code:403,description:"[Forbidden] : ${msg}")
		return this
	}
	def _403_FORBIDDEN(){
		status = new ErrorCodeDescriptor(code:403,description:"[Forbidden]")
		return this
	}
	
	// 404 not found
	def _404_NOT_FOUND(String msg){
		status = new ErrorCodeDescriptor(code:404,description:"[Not Found] : ${msg}")
		return this
	}
	def _404_NOT_FOUND(){
		status = new ErrorCodeDescriptor(code:404,description:"[Not Found]")
		return
	}

	// UNSUPPORTED METHOD
	def _405_UNSUPPORTED_METHOD(String msg){
		status = new ErrorCodeDescriptor(code:405,description:"[Unsupported Method] : ${msg}")
		return this
	}
	def _405_UNSUPPORTED_METHOD(){
		status = new ErrorCodeDescriptor(code:405,description:"[Unsupported Method]")
		return this
	}
	
	// ACCOUNT CONFLICT
	def _409_ACCOUNT_CONFLICT(String msg){
		status = new ErrorCodeDescriptor(code:409,description:"[Account Conflict] : ${msg}")
		return this
	}
	def _409_ACCOUNT_CONFLICT(){
		status = new ErrorCodeDescriptor(code:409,description:"[Account Conflict]")
		return
	}
	
	// ConditionNotMet
	def _412_CONDITION_NOT_MET(String msg){
		status = new ErrorCodeDescriptor(code:412,description:"[Condition Not Met] : ${msg}")
		return this
	}
	def _412_CONDITION_NOT_MET(){
		status = new ErrorCodeDescriptor(code:412,description:"[Condition Not Met] ")
		return this
	}
	
	// RequestBodyTooLarge
	def _413_REQUEST_BODY_TOO_LARGE(String msg){
		status = new ErrorCodeDescriptor(code:413,description:"[Request Body Too Large] : ${msg}")
		return this
	}
	def _413_REQUEST_BODY_TOO_LARGE(){
		status = new ErrorCodeDescriptor(code:413,description:"[Request Body Too Large]")
		return this
	}
	
	// InvalidRange
	def _416_INVALID_RANGE(String msg){
		status = new ErrorCodeDescriptor(code:416,description:"[Invalid Range] : ${msg}")
		return
	}
	def _416_INVALID_RANGE(){
		status = new ErrorCodeDescriptor(code:416,description:"[Invalid Range]")
		return this
	}
	
	// SERVER ERROR
	def _500_SERVER_ERROR(String msg){
		status = new ErrorCodeDescriptor(code:500,description:"[Server Error] : ${msg}")
		return this
	}
	def _500_SERVER_ERROR(){
		status = new ErrorCodeDescriptor(code:500,description:"[Server Error]")
		return this
	}
	
	// SERVICE UNAVAILABLE
	def _503_UNAVAILABLE(String msg){
		status = new ErrorCodeDescriptor(code:503,description:"[Service Unavailable] : ${msg}")
		return this
	}
	def _503_UNAVAILABLE(){
		status = new ErrorCodeDescriptor(code:503,description:"[Service Unavailable]")
		return this
	}

}
