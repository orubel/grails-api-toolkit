import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for apitoolkit apidocs

ant.mkdir(dir: "${basedir}/src/apiObject")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine '\n// Added by the Api Toolkit plugin:'
		it.writeLine "apitoolkit.apiName = 'api'"
		it.writeLine "apitoolkit.apichain.limit=3"
		it.writeLine "apitoolkit.attempts = 5"
		it.writeLine "apitoolkit.chaining.enabled=true"
		it.writeLine "apitoolkit.batching.enabled=true"
		it.writeLine "apitoolkit.localAuth.enabled=false"
		it.writeLine "apitoolkit.user.roles = ['ROLE_USER']"
		it.writeLine "apitoolkit.admin.roles = ['ROLE_ROOT','ROLE_ADMIN']"
	}
	
	println """
	************************************************************
	* SUCCESS! You have successfully installed the API Toolkit *
	************************************************************
"""
}

