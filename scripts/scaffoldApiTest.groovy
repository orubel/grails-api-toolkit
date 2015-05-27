import grails.util.GrailsNameUtils
import grails.util.Metadata
import grails.util.Holders as HOLDER

/*
 * Get apicache names and create scaffolded tests for controllers
 * based on cache names and I/O data
 */
//includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsBootstrap")
//includeTargets << new File("scripts/_S2Common.groovy")
includeTargets << new File(apiToolkitPluginDir, 'scripts/_S2Common.groovy')

USAGE = """
Usage: grails scaffold-api-test

Scaffolds API Tests based on I/O State

Example: grails scaffold-api-test
"""

// scaffolded variables
packageName = 'net.nosegrind.apitoolkit'
testName = ''
methods = ['GET':'Get','POST':'Post','PUT':'Put','DELETE':'Delete']
templateDir = "$apiToolkitPluginDir/src/templates/tests"
appDir = "$basedir/grails-app/test/functional"
//ant.mkdir(dir: "${System.properties.'user.home'}/.apitoolkit")

target('scaffoldApiTest': 'Scaffolds API Objects based on Controllers') {
	depends(checkVersion, configureProxy, packageApp, parseArguments)
	if (argsMap.https) {
		runAppHttps()
	}
	else {
		runApp()
		
		def grailsApplication = HOLDER.getGrailsApplication()
		def appCtx = HOLDER.applicationContext
		def cacheNames = appCtx.getBean('apiCacheService').getCacheNames()
		def adminRoles = grailsApplication.config.apitoolkit.admin.roles
		
		for(controller in grailsApplication.controllerClasses) {
			String templateMethods = ""
			String cName = controller.logicalPropertyName
			String cacheName = cName.replaceAll('Controller','').toLowerCase()
			
			println("cacheName : "+cacheName)
			if(cacheNames.contains(cacheName)){
				
				def cache = appCtx.getBean('apiCacheService').getApiCache(cacheName)
				def version = cache.currentStable.value
				def methods = cache[version]
				methods.remove('deprecated')
				methods.remove('defaultAction')
				methods.remove('domainPackage')
				
				// sort methods back into proper order to walk through methods, keys, values
				def methodGrps = sortMethods(methods)
				methodGrps['POST'].each{
					templateMethods['POST'].add(generatePostMethod(key,cacheName))
				}
				methodGrps['GET'].each{
					templateMethods['GET'].add(generateGetMethod(key,cacheName))
				}
				methodGrps['PUT'].each{
					templateMethods['PUT'].add(generatePutMethod(key,cacheName))
				}
				methodGrps['DELETE'].each{
					templateMethods['POST'].delete(generateDeleteMethod(key,cacheName))
				}

				//println("templateMethods : "+templateMethods)
				//templateAttributes = [className: cName,templateMethods: templateMethods]
				println "*** ... Functional test generated for '"+cacheName+"'"
			}



			
		}
		//startPluginScanner()
		//watchContext()
	}

	println """
	*************************************************************
	* API Tests successfully scaffolded.                        *
	*************************************************************
	"""
}

LinkedHashMap sortMethods(LinkedHashMap methods){
	LinkedHashMap methodGrps = ['GET':[],'POST':[],'PUT':[],'DELETE':[]]
	methods.each(){ key,val ->
		String method = val.method.toUpperCase()
		methodGrps[method].add(key)
	}
	return methodGrps
}

target('scaffoldIoState':'Scaffolds Basic REST Test Templates based on Available IO States'){
	println("### scaffoldIoState")
	//loadApp()
	//configureApp()
	
	def grailsApplication = HOLDER.getGrailsApplication()
	for(controller in grailsApp.controllerClasses) {
		println("controller:"+controller)
		def cName = controller.logicalPropertyName
		def cacheName = cName.replaceAll('Controller','').toLowerCase()
		
		//def serviceClass = grailsApp.getClassForName('net.nosegrind.apitoolkit.ApiCacheService')
		//def serviceClassMethod = serviceClass.metaClass.getMetaMethod('getCacheNames')

		//def apiCacheService = appCtx.getBean('apiCacheService')
		//def cacheNames = serviceClassMethod.invoke(apiCacheService,[] as Object[])
		def appCtx = ctx = HOLDER.applicationContext
		def cacheNames = appCtx.getBean('apiCacheService').getCacheNames()

		//def cache = serviceClassMethod.invoke(apiCacheService, [cacheName] as Object)
		
		//println(cache)
		// needed to determine i/o values and methods for template tests
		def adminRoles = grailsApp.config.apitoolkit.admin.roles
		def input = [:]
		def output = [:]
		

		
		//templateAttributes = [className: cName]
	}
	/*
	ant.mkdir dir: "$appDir/views/hook"
	// add default views for hooks administration
	copyFile "$templateDir/hook/create.gsp.template", "$appDir/views/hook/create.gsp"
	copyFile "$templateDir/hook/edit.gsp.template", "$appDir/views/hook/edit.gsp"
	copyFile "$templateDir/hook/list.gsp.template", "$appDir/views/hook/list.gsp"
	copyFile "$templateDir/hook/show.gsp.template", "$appDir/views/hook/show.gsp"

	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/hook/HookController.groovy.template", "$appDir/controllers/${dir2}HookController.groovy"
	printMessage "Controller / Views created..."
	*/
}

String generatePostMethod(String methodName, String className){
/*
templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Post.groovy.template"
File templateFile = new File(templatePath)
if (!templateFile.exists()) {
	println("\nERROR: $templatePath doesn't exist")
	return null
}else{
	String output
	output << templateEngine.createTemplate(templateFile.text).make(templateAttributes)
	println("generateTemplate:"+output)
	return output
}
*/
}

def String generateGetMethod(String methodName, String className){
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Get.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output = ""
		templateAttributes = [className: className,methodName:methodName]
		output = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

String generatePutMethod(String methodName, String className){
	/*
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Put.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output
		output << templateEngine.createTemplate(templateFile.text).make(templateAttributes)
		println("generateTemplate:"+output)
		return output
	}
	*/
}

String generateDeleteMethod(String methodName, String className){
	/*
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Delete.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output
		output << templateEngine.createTemplate(templateFile.text).make(templateAttributes)
		println("generateTemplate:"+output)
		return output
	}
	*/
}

setDefaultTarget('scaffoldApiTest')