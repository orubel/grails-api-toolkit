import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")



USAGE = """
Usage: grails scaffold-api-objects

Creates ApiDocController and view for your environment

Example: grails scaffold-api-objects
"""

packageName = ''
userClassName = ''
templateDir = "$apiToolkitPluginDir/src/templates"
appDir = "$basedir/grails-app"

target(scaffoldApiObjects: 'Creates artifacts for the Api Objects') {
	if (!configure()) {
		return 1
	}

	copyControllersAndViews()
	updateConfig()

	printMessage """
	*************************************************************
	* API Objects successfully Scaffolded.                      *
	*************************************************************
	"""
}

private boolean configure() {
	def argValues = parseArgs()
	if (!argValues) {
		return false
	}

	if (argValues.size() == 3) {
		(packageName, userClassName,roleClassName) = argValues
	}else {
		return false
	}

	templateAttributes = [packageName: packageName,userClassName: userClassName,roleClassName:roleClassName,apiProperties:'',apiConstraints:'']

	true
}


private void copyControllersAndViews() {
	ant.mkdir dir: "$appDir/views/hook"
	// add default views for hooks administration
	copyFile "$templateDir/hook/create.gsp.template", "$appDir/views/hook/create.gsp"
	copyFile "$templateDir/hook/edit.gsp.template", "$appDir/views/hook/edit.gsp"
	copyFile "$templateDir/hook/list.gsp.template", "$appDir/views/hook/list.gsp"
	copyFile "$templateDir/hook/show.gsp.template", "$appDir/views/hook/show.gsp"

	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/hook/HookController.groovy.template", "$appDir/controllers/${dir2}HookController.groovy"
	printMessage "Controller / Views created..."
}

private void updateConfig() {
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Api Toolkit plugin:'
			it.writeLine "apitoolkit.domain = '${packageName}.Hook'"
			it.writeLine "apitoolkit.controller = '${packageName}.HookController'"
			it.writeLine "apitoolkit.batch.limit = '10'"
		}
	}
}

private parseArgs() {
	def args = argsMap.params

	if (3 == args.size()) {
		printMessage "Creating classes in package ${args[0]}..."
		return args
	}

	errorMessage USAGE
	null
}

setDefaultTarget('scaffoldApiObjects')