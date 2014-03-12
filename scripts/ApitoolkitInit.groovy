import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")

includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")

USAGE = """
Usage: grails apitoolkit-init <domain-class-package> <user-class-name> <role-class-name>

Takes three arguments of package name, user classname and authorities class name
and creates hook domain, controller and views.

Example: grails apitoolkit-init com.yourapp User Role
"""

packageName = ''
userClassName = ''
templateDir = "$apiToolkitPluginDir/src/templates"
appDir = "$basedir/grails-app"

target(apitoolkitInit: 'Creates artifacts for the Api Hooks') {
	if (!configure()) {
		return 1
	}

	createDomains()
	copyControllersAndViews()
	updateConfig()

	printMessage """
	*************************************************************
	* SUCCESS! Created domain classes, controllers, and GSPs.   *
	* Api Toolkit Plugin is now installed. Please see documentation *
	* page on implementation details.                           *
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

private void createDomains() {
	String dir = packageToDir(packageName)
	generateFile "$templateDir/hook/Hook.groovy.template", "$appDir/domain/${dir}Hook.groovy"
	generateFile "$templateDir/hook/HookRole.groovy.template", "$appDir/domain/${dir}HookRole.groovy"
	printMessage "Domains created..."
}

private void copyControllersAndViews() {
	ant.mkdir dir: "$appDir/views/hook"
	// add default views for hooks administration
	copyFile "$templateDir/hook/create.gsp.template", "$appDir/views/hook/create.gsp"
	copyFile "$templateDir/hook/edit.gsp.template", "$appDir/views/hook/edit.gsp"
	copyFile "$templateDir/hook/list.gsp.template", "$appDir/views/hook/list.gsp"
	copyFile "$templateDir/hook/show.gsp.template", "$appDir/views/hook/show.gsp"

	String dir2 = packageToDir(packageName)
	println("${templateDir}/hook/HookController.groovy.template")
	println("${appDir}/controllers/${dir2}HookController.groovy")
	//generateFile "$templateDir/hook/HookController.groovy.template", "$appDir/controllers/${dir2}HookController.groovy"
	printMessage "Controller / Views created..."
}

private void updateConfig() {
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Api Toolkit plugin:'
			it.writeLine "apitoolkit.domain = '${packageName}.Hook'"
			it.writeLine "apitoolkit.controller = '${packageName}.HookController'"
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

setDefaultTarget('apitoolkitInit')
