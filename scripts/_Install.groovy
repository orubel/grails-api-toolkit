import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for apitoolkit apidocs

ant.mkdir(dir: "${basedir}/grails-app/views/apidoc")
ant.copy(file:"${pluginBasedir}/src/templates/show.gsp.template",tofile:"${basedir}/grails-app/views/apidoc/show.gsp")

ant.mkdir(dir: "${basedir}/grails-app/controllers/net/nosegrind/apitoolkit")
ant.copy(file:"${pluginBasedir}/src/templates/ApidocController.groovy.template",tofile:"${basedir}/grails-app/controllers/net/nosegrind/apitoolkit/ApidocController.groovy")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine '\n// Added by the Api Toolkit plugin:'
		it.writeLine "apitoolkit.apiName = 'api'"
		it.writeLine "\n"
		it.writeLine "apitoolkit.defaultData.PKEY = '26'"
		it.writeLine "apitoolkit.defaultData.FKEY = '32'"
		it.writeLine "apitoolkit.defaultData.INDEX = '32'
		it.writeLine "apitoolkit.defaultData.STRING = 'Hello World'"
		it.writeLine "apitoolkit.defaultData.BOOLEAN = 'true'"
		it.writeLine "apitoolkit.defaultData.FLOAT = '1.00'"
		it.writeLine "apitoolkit.defaultData.BIGDECIMAL = '123567828794.87'"
		it.writeLine "apitoolkit.defaultData.LONG = '18926'"
		it.writeLine "apitoolkit.defaultData.EMAIL = 'example@yoursite.com'"
		it.writeLine "apitoolkit.defaultData.URL = 'http://www.yoursite.com'"
	}
}