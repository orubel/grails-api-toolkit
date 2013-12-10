import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for restrpc apidocs

ant.mkdir(dir: "${basedir}/grails-app/views/apidoc")
ant.copy(file:"${pluginBasedir}/src/templates/show.gsp.template",tofile:"${basedir}/grails-app/views/apidoc/show.gsp")

ant.mkdir(dir: "${basedir}/grails-app/controllers/net/nosegrind/restrpc")
ant.copy(file:"${pluginBasedir}/src/templates/ApidocController.groovy.template",tofile:"${basedir}/grails-app/controllers/net/nosegrind/restrpc/ApidocController.groovy")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine '\n// Added by the Restrpc plugin:'
		it.writeLine "restrpc.apiName = 'api'"
		it.writeLine "\n"
		it.writeLine "restrpc.defaultData.PKEY = '26'"
		it.writeLine "restrpc.defaultData.FKEY = '32'"
		it.writeLine "restrpc.defaultData.INDEX = '32'
		it.writeLine "restrpc.defaultData.STRING = 'Hello World'"
		it.writeLine "restrpc.defaultData.BOOLEAN = 'true'"
		it.writeLine "restrpc.defaultData.FLOAT = '1.00'"
		it.writeLine "restrpc.defaultData.BIGDECIMAL = '123567828794.87'"
		it.writeLine "restrpc.defaultData.LONG = '18926'"
		it.writeLine "restrpc.defaultData.EMAIL = 'example@yoursite.com'"
		it.writeLine "restrpc.defaultData.URL = 'http://www.yoursite.com'"
	}
}