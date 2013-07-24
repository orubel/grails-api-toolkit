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
		it.writeLine "restrpc.apiVersion = '1.0'"
		it.writeLine "\n"
		it.writeLine "restrpc.defaultData.ID = '26'"
		it.writeLine "restrpc.defaultData.String = 'Hello World'"
		it.writeLine "restrpc.defaultData.Boolean = 'true'"
		it.writeLine "restrpc.defaultData.Float = '1.00'"
		it.writeLine "restrpc.defaultData.BigDecimal = '123567828794.87'"
		it.writeLine "restrpc.defaultData.Integer = '18'"
		it.writeLine "restrpc.defaultData.Long = '18926'"
		it.writeLine "restrpc.defaultData.Email = 'example@yoursite.com'"
		it.writeLine "restrpc.defaultData.Url = 'http://www.yoursite.com'"
	}
}