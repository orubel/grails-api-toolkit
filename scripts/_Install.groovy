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
	}
}