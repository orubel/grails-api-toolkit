import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for apitoolkit apidocs

ant.mkdir(dir: "${basedir}/grails-app/views/apidoc")
ant.copy(file:"${pluginBasedir}/src/templates/apidoc/show.gsp.template",tofile:"${basedir}/grails-app/views/apidoc/show.gsp")

ant.mkdir(dir: "${basedir}/grails-app/controllers/net/nosegrind/apitoolkit")
ant.copy(file:"${pluginBasedir}/src/templates/apidoc/ApidocController.groovy.template",tofile:"${basedir}/grails-app/controllers/net/nosegrind/apitoolkit/ApidocController.groovy")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine '\n// Added by the Api Toolkit plugin:'
		it.writeLine "apitoolkit.apiName = 'api'"
		it.writeLine "apitoolkit.apichain.limit=3"
		it.writeLine "apitoolkit.attempts = 5"
		it.writeLine "apitoolkit.user.roles = ['ROLE_USER']"
		it.writeLine "apitoolkit.admin.roles = ['ROLE_ROOT','ROLE_ADMIN']"
	}
	
	println """
	*************************************************************
	* SUCCESS! You have successfully installed the API Toolkit..*
	* Please run 'grails apitoolkit-init' to finish the install *
	* process...                                                *
	*************************************************************
"""
}

