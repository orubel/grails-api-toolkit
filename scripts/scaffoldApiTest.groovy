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
		String role = getLoginRole()
		
		for(controller in grailsApplication.controllerClasses) {
			String cacheName = controller.logicalPropertyName
			if(!['iostate','hook'].contains(cacheName)){
				LinkedHashMap templateMethods = [:]
				
				println("cacheName : "+cacheName)
				if(cacheNames.contains(cacheName)){
					
					def cache = appCtx.getBean('apiCacheService').getApiCache(cacheName)
					def version = cache.currentStable.value
	
					def methods = cache[version]
					methods.remove('deprecated')
					methods.remove('defaultAction')
					methods.remove('domainPackage')
					
					// sort methods back into proper order to walk through methods, keys, values
					templateMethods = createMethods(methods,cacheName,role)
					if(templateMethods){
						fkeys = methods.fkeys
						
						//println("templateMethods : "+templateMethods)
						templateAttributes = [className: cacheName.capitalize(),templateMethods: templateMethods,fkeys:fkeys]
						// generateFile "$templateDir/FunctionalSpec.groovy.template", "$appDir/${cacheName.capitalize()}FunctionalSpec.groovy"
						
						println "*** ... Functional test generated for '"+cacheName+"'"
						
					}
				}
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

List getLoginRole(){
	def personClass = HOLDER.getGrailsApplication().getDomainClass(HOLDER.config.grails.plugin.springsecurity.userLookup.userDomainClassName).clazz
	def principal = personClass.findByUsername(login)
	Long user = principal.id.toLong()
	
	String userClass = (HOLDER.config.grails.plugin.springsecurity.userLookup.userDomainClassName).split('.').last()
	grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'net.nosegrind.apitoolkit.PersonRole'
	def personRoleClass = HOLDER.getGrailsApplication().getDomainClass(HOLDER.config.grails.plugin.springsecurity.userLookup.authorityJoinClassName).clazz
	List role = personRoleClass.findAuthorityBy${userClass}(user)

	return role
}

LinkedHashMap createInput(Map receives,String role){
	LinkedHashMap input = [:]
	
	return input
}

List createOutput(Map returns,String Role){
	List output = []
	
	return output
}

LinkedHashMap createMethods(LinkedHashMap methods,String cacheName,String role){
	LinkedHashMap methodGrps = ['GET':[],'POST':[],'PUT':[],'DELETE':[]]
	List fkeys = []
	List tempKeys = []
	methods.each(){ key,val ->
		
		def input = createInput(val.receives,role)
		def output = createOutput(val.returns,role)
		
		switch(val.method.toUpperCase()){
			case 'POST':
				methodGrps['POST'].add(generatePostMethod(key,cacheName))
				break;
			case 'GET':
				methodGrps['GET'].add(generateGetMethod(key,cacheName))
				break;
			case 'PUT':
				methodGrps['PUT'].add(generatePutMethod(key,cacheName))
				break;
			case 'DELETE':
				methodGrps['DELETE'].add(generateDeleteMethod(key,cacheName))
				break;
		}

	}
	return methodGrps
}

String generatePostMethod(String methodName, String className){
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Post.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output = ""
		// inputMap,outputList
		def templateAttributes = [className: className,methodName:methodName]
		println("POST")
		output = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

def String generateGetMethod(String methodName, String className){
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Get.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output = ""
		def templateAttributes = [className: className,methodName:methodName]
		println("GET")
		output = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

String generatePutMethod(String methodName, String className){
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Put.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output = ""
		// inputMap,outputList
		def templateAttributes = [className: className,methodName:methodName]
		println("PUT")
		output = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

String generateDeleteMethod(String methodName, String className){
	templatePath = templateDir = "$apiToolkitPluginDir/src/templates/tests/methods/Delete.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String output = ""
		def templateAttributes = [className: className,methodName:methodName]
		println("DELETE")
		output = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

setDefaultTarget('scaffoldApiTest')