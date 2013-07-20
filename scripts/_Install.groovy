import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for restrpc apidocs
//ant.mkdir(dir:"mkdir")
//ant.javac(srcdir:"src/java", destdir:"output")

ant.mkdir(dir: "${basedir}/grails-app/views/apidoc")
ant.copy(file:"${pluginBasedir}/src/templates/show.gsp.template",tofile:"${basedir}/grails-app/views/apidoc/show.gsp")

//String dir2 = packageToDir(packageName)
ant.mkdir(dir: "${basedir}/grails-app/controllers/net/nosegrind/restrpc")
ant.copy(file:"${pluginBasedir}/src/templates/ApidocController.groovy.template",tofile:"${basedir}/grails-app/controllers/net/nosegrind/restrpc/ApidocController.groovy")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine '\n// Added by the Restrpc plugin:'
		it.writeLine "restrpc.apiName = 'api'"
		it.writeLine "restrpc.apiVersion = '1.0'"
	}
}