import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")

includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")

USAGE = """
Usage: grails init-api-slave <user-domain-class-package> <user-class-name> <role-class-name>

Takes 3 arguments of the spring-security USER package, USER classname, ROLE classname 
and then it creates webhook domain, controller, views and supporting services

Example: grails init-api-slave com.yourapp User Role
"""

packageName = ''
userClassName = ''
roleClassName = ''
nosqlName = ''

templateDir = "$apiToolkitPluginDir/src/templates/apiobject"
appDir = "$basedir/grails-app"

target(initApiSlave: 'Creates artifacts for Api Slave Server') {
	if (!configure()) {
		return 1
	}

	copyControllersAndViews()

	printMessage """
	*************************************************************
	* SUCCESS! API Slave is now installed. Please see           *
	* documentation page on implementation details.             *
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

	templateAttributes = [packageName: packageName,userClassName: userClassName,roleClassName:roleClassName]

	true
}

target(copyControllersAndViews:"Create API Controllers and Views for apiobject") {
	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/ApiobjectController.groovy.template", "$appDir/controllers/${dir2}ApiobjectController.groovy"
	printMessage "Controller created..."
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

setDefaultTarget('initApiSlave')
