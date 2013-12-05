package net.nosegrind.restrpc

import org.springframework.web.context.request.RequestContextHolder as RCH

class ApiErrorsService{

	def grailsApplication
	def springSecurityService

	static transactional = false

	Long responseCode
	String responseMessage

	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	/*
	 * Error messages
	 * For complete list of messages, see http://msdn.microsoft.com/en-us/library/windowsazure/dd179357.aspx
	 */
	def send(){
		def response = getResponse()
		response.setStatus(responseCode,responseMessage)
		return
	}
	
	def text(){
		return "${responseCode} ${responseMessage}"
	}
	
	def _200_SUCCESS(String msg){
		responseCode=200
		responseMessage="[Success] : ${msg}"
		return
	}

	def _200_SUCCESS(){
		responseCode=200
		responseMessage="[Success]"
		return
	}

	// 304 not modified
	def _304_NOT_MODIFIED(String msg){
		responseCode=304
		responseMessage="[Not Modified] : ${msg}"
		return
	}
	def _304_NOTMODIFIED(){
		responseCode=304
		responseMessage="[Not Modified]"
		return
	}

	// 400 bad request
	def _400_BAD_REQUEST(String msg){
		responseCode=400
		responseMessage="[Bad Request] : ${msg}"
		return
	}
	def _400_BAD_REQUEST(){
		responseCode=400
		responseMessage="[Bad Request]"
		return
	}
	
	// 403 forbidden
	def _403_FORBIDDEN(String msg){
		responseCode=403
		responseMessage="[Forbidden] : ${msg}"
		return
	}
	def _403_FORBIDDEN(){
		responseCode=403
		responseMessage="[Forbidden]"
		return
	}
	
	// 404 not found
	def _404_NOT_FOUND(String msg){
		responseCode=404
		responseMessage="[Not Found] : ${msg}"
		return
	}
	def _404_NOT_FOUND(){
		responseCode=404
		responseMessage="[Not Found]"
		return
	}

	// UNSUPPORTED METHOD
	def _405_UNSUPPORTED_METHOD(String msg){
		responseCode=405
		responseMessage="[Unsupported Method] : ${msg}"
		return
	}
	def _405_UNSUPPORTED_METHOD(){
		responseCode=405
		responseMessage="[Unsupported Method]"
		return
	}
	
	// ACCOUNT CONFLICT
	def _409_ACCOUNT_CONFLICT(String msg){
		responseCode=409
		responseMessage="[Account Conflict] : ${msg}"
		return
	}
	def _409_ACCOUNT_CONFLICT(){
		responseCode=409
		responseMessage="[Account Conflict]"
		return
	}
	
	// ConditionNotMet
	def _412_CONDITION_NOT_MET(String msg){
		responseCode=412
		responseMessage="[Condition Not Met] : ${msg}"
		return
	}
	def _412_CONDITION_NOT_MET(){
		responseCode=412
		responseMessage="[Condition Not Met]"
		return
	}
	
	// RequestBodyTooLarge
	def _413_REQUEST_BODY_TOO_LARGE(String msg){
		responseCode=413
		responseMessage="[Request Body Too Large] : ${msg}"
		return
	}
	def _413_REQUEST_BODY_TOO_LARGE(){
		responseCode=413
		responseMessage="[Request Body Too Large]"
		return
	}
	
	// InvalidRange
	def _416_INVALID_RANGE(String msg){
		responseCode=416
		responseMessage="[Invalid Range] : ${msg}"
		return
	}
	def _416_INVALID_RANGE(){
		responseCode=416
		responseMessage="[Invalid Range]"
		return
	}
	
	// SERVER ERROR
	def _500_SERVER_ERROR(String msg){
		responseCode=500
		responseMessage="[Server Error] : ${msg}"
		return
	}
	def _500_SERVER_ERROR(){
		responseCode=500
		responseMessage="[Server Error]"
		return
	}
	
	// SERVICE UNAVAILABLE
	def _503_UNAVAILABLE(String msg){
		responseCode=503
		responseMessage="[Service Unavailable] : ${msg}"
		return
	}
	def _503_UNAVAILABLE(){
		responseCode=503
		responseMessage="[Service Unavailable]"
		return
	}
}
