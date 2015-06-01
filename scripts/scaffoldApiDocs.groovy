import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript('_GrailsBootstrap')


USAGE = """
Usage: grails scaffold-api-docs

Scaffolds API Objects based on Controllers

Example: grails scaffold-api-docs
"""

packageName = ''
userClassName = ''
templateDir = "$apiToolkitPluginDir/src/templates"
appDir = "$basedir/grails-app"

target(scaffoldApiDocs: 'Scaffolds API Objects based on Controllers') {
	if (!configure()) {
		return 1
	}



	printMessage """
	*************************************************************
	* API Docs successfully scaffolded.                         *
	*************************************************************
	"""
}

private boolean configure() {
	def argValues = parseArgs()
	if (!argValues) {
		return false
	}
	
	createApiMethods()


	if (argValues.size() == 3) {
		(packageName, userClassName,roleClassName) = argValues
	}else {
		return false
	}

	templateAttributes = [packageName: packageName,userClassName: userClassName,roleClassName:roleClassName,apiProperties:'',apiConstraints:'']

	true
}


private void createApiMethods() {
	def controllerArtefact = grailsApp?.getArtefactByLogicalPropertyName("Controller", "book")
	controllerArtefact.clazz.methods.each { method ->
		
	}

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

private parseArgs() {
	def args = argsMap.params

	if (args.size()==1 && args.size()<=3) {
		printMessage "Scaffolding API Objects..."
		return args
	}

	errorMessage USAGE
	null
}

setDefaultTarget('scaffoldApiDocs')