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
Usage: grails api-handlers

Example: grails api-handlers
"""

newPackageName = 'net.nosegrind.apitoolkit'
domainPackageName = ''
domainPath = ''
apiClassName = ''
templateDir = "$apiToolkitPluginDir/src/templates"
appDir = "$basedir/grails-app"

target(apiHandlers: 'Creates api handlers from pre-existing domains') {
	depends(configureProxy, packageApp, classpath, loadApp, configureApp, compile)
	//depends(checkVersion, configureProxy, packageApp,runApp)
	createObjects()

	printMessage """
	*************************************************************
	* SUCCESS! Created api handlers from domains classes.        *
	*************************************************************
	"""
}

//target(createObjects: 'Create api object classes from pre-existing GORM domains'){
def createObjects(){
	//String dir = packageToDir('net.nosegrind.apitoolkit')
	ant.mkdir dir: "$appDir/apiHandlers/"
	
	// if it begins java.util or java.lang, then use
	grailsApp.domainClasses.each { DefaultGrailsDomainClass domainClass ->
		def apiProperties = ''
		println("#### ${domainClass}")
		List methods = []
		def properties = [:]
		domainClass.getProperties().each { property ->
			//println(property)
			if(property.type.name!='java.lang.Object'){
				if(grailsApp.isDomainClass(property.type)){
					apiProperties += "Long ${property.name}_id\n"
					//properties["${property.name}_id"] = 'java.lang.Long'
				}else{
					def type = (property.type.name.toString()?.split('\\.'))?(property.type.name.toString().split('\\.')).last():property.type.name
					apiProperties += "${type} ${property.name}\n"
					//properties["${property.name}"] = property.type.name
				}
			}
		}
		println(apiProperties)
	}
	//templateAttributes = [apiClassName: apiClassName,apiProperties:apiProperties]
	//generateFile "$templateDir/hook/Hook.groovy.template", "$appDir/domain/${dir}Hook.groovy"

}


setDefaultTarget('apiHandlers')
