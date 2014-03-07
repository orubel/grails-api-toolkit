import grails.util.Metadata

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import grails.util.Holders

grailsHome = Ant.project.properties."environment.GRAILS_HOME"

includeTargets << grailsScript("_GrailsInit")
includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")
includeTargets << grailsScript('_GrailsProxy')
includeTargets << grailsScript('_GrailsPackage')
includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript('_GrailsRun')

USAGE = """
Usage: grails api-objects your.package.com DomainClassName

Takes two arguments of package name and optional domain class name
and creates api classes based on domain classes.

If no domain class name given, will read all files in package directory,
and convert them to validateable api classes.

Example: grails api-objects
"""

newPackageName = 'net.nosegrind.apitoolkit'
domainPackageName = ''
domainPath = ''
apiClassName = ''
templateDir = "$apiToolkitPluginDir/src/templates"
appDir = "$basedir/grails-app"

target(apiObjects: 'Creates api command objects from pre-existing domains') {
	depends(configureProxy, packageApp, classpath, loadApp, configureApp, compile)
	//depends(checkVersion, configureProxy, packageApp,runApp)
	createObjects()


	


	printMessage """
	*************************************************************
	* SUCCESS! Created api classes from domains classes.        *
	*************************************************************
	"""
}

//target(createObjects: 'Create api object classes from pre-existing GORM domains'){
def createObjects(){
	if (!configureScript()) {
		exit(1)
	}
	String dir = packageToDir('net.nosegrind.apitoolkit')
	ant.mkdir dir: "$appDir/api/"
	
	//def grailsApplication = Holders.getGrailsApplication()

	if(domainPackageName){

		String domainPath = domainPackageName.replace(".","/")
		println(domainPath)

		if(apiClassName){
			grailsApp.domainClasses.each { DefaultGrailsDomainClass domainClass ->
				println(domainClass)
				domainClass.getProperties().each { property ->
					if(property.type.name!='java.lang.Object'){
						if(grailsApp.isDomainClass(property.type)){
							println("java.lang.Long = ${property.name}_id")
						}else{
							println("${property.type.name} = ${property.name}")
						}
					}
				}
			}
		}else{
			// read all files in directory
		}

	}else{
	
	}
	//generateFile "$templateDir/hook/Hook.groovy.template", "$appDir/domain/${dir}Hook.groovy"

}

private boolean configureScript() {
	def argValues = parseArgs()
	if (!argValues) {
		return false
	}

	if (argValues.size() == 2) {
		(domainPackageName, apiClassName) = argValues
	}else {
		return false
	}

	templateAttributes = [packageName: domainPackageName,apiClassName: apiClassName]

	true
}

private parseArgs() {
	def args = argsMap.params

	if ([1,2].contains(args.size())) {
		printMessage "Creating classes for package ${args[0]}..."
		return args
	}

	errorMessage USAGE
	null
}

setDefaultTarget('apiObjects')
