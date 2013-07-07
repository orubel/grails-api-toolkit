import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")

includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")

packageName = 'net.nosegrind.restrpc'
templateDir = "$restrpcPluginDir/src/templates"
appDir = "$basedir/grails-app"

copyControllersAndViews()
updateConfig()

printMessage """
*************************************************************
* SUCCESS! Created controllers, and GSPs.                   *
* RestRPC Plugin is now installed. Please see documentation *
* page on implementation details.                           *
*************************************************************
"""

private void copyControllersAndViews() {
	ant.mkdir dir: "$appDir/views/apidoc"
	// add default views for webhooks administration
	copyFile "$templateDir/show.gsp.template", "$appDir/views/apidoc/show.gsp"

	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/ApidocController.groovy.template", "$appDir/controllers/${dir2}ApidocController.groovy"
	printMessage "Controller / Views created..."
}

private void updateConfig() {
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Webhook plugin:'
			it.writeLine "restrpc.apiName = 'api'"
			it.writeLine "restrpc.apiVersion = '1.0'"
		}
	}
}
