import static java.util.Calendar.*

class ApiTagLib {

	static namespace = 'api'
	
	def grailsApplication
	def apitoolkitService
	
	// <api:checkRole role="['ROLE_ADMIN','ROLE_USER']"/>
	def checkRole = { attrs,body ->
		if(attrs.roles){
			if(apitoolkitService.checkHookAuthority(attrs.roles)){
				out << body()
				//return true
			}else{
				//return false
			}
		}
		//return false
	}
}

