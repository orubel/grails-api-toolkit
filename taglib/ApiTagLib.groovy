import static java.util.Calendar.*

class ApiTagLib {

	static namespace = 'api'
	
	def grailsApplication
	def apitoolkitService
	
	// <api:checkHookRole controller="${controllerName}" method="${action}"/>
	def checkHookRole = { attrs,body ->
		if(attrs.roles){
			println("has roles")
			if(apitoolkitService.checkHookAuthority(attrs.roles)){
				println("has authority")
				out << body()
				//return true
			}else{
				//return false
			}
		}
		//return false
	}
}

