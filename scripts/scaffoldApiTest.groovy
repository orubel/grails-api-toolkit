import grails.util.GrailsNameUtils
import grails.util.Metadata
import grails.util.Holders as HOLDER
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.GrailsApplication


/*
 * Get apicache names and create scaffolded tests for controllers
 * based on cache names and I/O data
 */

includeTargets << grailsScript("_GrailsClean")
includeTargets << grailsScript("RefreshDependencies")
includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("Compile")
includeTargets << grailsScript("_GrailsClasspath")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_PluginDependencies")
includeTargets << grailsScript("_GrailsCreateArtifacts")
includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsPackage")

includeTargets << grailsScript('_GrailsClean')
includeTargets << grailsScript('_GrailsTest')

includeTargets << new File(apiToolkitPluginDir, 'scripts/_S2Common.groovy')

USAGE = """
Usage: grails scaffold-api-test

Scaffolds API Tests based on I/O State

Example: grails scaffold-api-test
"""


def templateDir = "$apiToolkitPluginDir/src/templates/tests"
def testDir = "$basedir/test/functional"

def grailsApplication
def ctx

target('scaffoldApiTest': 'Scaffolds API Objects based on Controllers') {
	depends(cleanAll,checkVersion, configureProxy, packageApp, classpath, parseArguments)
	runApp()

		//startPluginScanner()

		grailsApplication = HOLDER.getGrailsApplication()
		ctx = HOLDER.applicationContext
		//ctx = grailsApplication.mainContext

		ant.mkdir(dir: "${testDir}")
		
		def cacheNames = ctx.getBean('apiCacheService').getCacheNames()
		def adminRoles = grailsApplication.config.apitoolkit.admin.roles
		List role = getLoginRole()
		
		for(controller in grailsApplication.controllerClasses) {
			String cacheName = controller.logicalPropertyName
			if(!['iostate','hook'].contains(cacheName)){
				LinkedHashMap templateMethods = [:]

				if(cacheNames.contains(cacheName)){
					
					def cache = ctx.getBean('apiCacheService').getApiCache(cacheName)
					def version = cache.currentStable.value
	
					def methods = cache[version]
					methods.remove('deprecated')
					methods.remove('defaultAction')
					methods.remove('domainPackage')
					
					// sort methods back into proper order to walk through methods, keys, values
					templateMethods = createMethods(methods,cacheName,role)
					if(templateMethods){
						fkeys = methods.fkeys

						String resource = cacheName.capitalize()
						templateAttributes = [className: resource,templateMethods: templateMethods,fkeys:fkeys]
						generateFile "$templateDir/FunctionalSpec.groovy.template", "$testDir/${resource}FunctionalSpec.groovy"
						
						println "*** ... Functional test generated for '"+resource+"'"
						
					}
				}
			}
		}

		//watchContext()
	//}


	println """
	*************************************************************
	* API Tests successfully scaffolded.                        *
	*************************************************************
	"""
	stopServer()
}

target('getLoginRole': 'Get Role for Default Login') {
	// set these variables in your config or external properties file (preferable)
	String login = grailsApplication.config.root.login
	String password = grailsApplication.config.root.password
	
	def personClass = grailsApplication.getDomainClass(grailsApplication.config.grails.plugin.springsecurity.userLookup.userDomainClassName).clazz
	def principal = personClass.findByUsername(login)
	Long userId = principal.id.toLong()

	def personRoleClazz = grailsApplication.getDomainClass(grailsApplication.config.grails.plugin.springsecurity.userLookup.authorityJoinClassName).clazz

	def roles = personRoleClazz.executeQuery("select R.authority from PersonRole as PR LEFT JOIN PR.role as R where PR.person.id=${userId}")
	
	return roles
}

LinkedHashMap createInput(Map receives,List role){
	def grailsApplication = HOLDER.getGrailsApplication()
	LinkedHashMap input = [:]
	role.add('permitAll')
	receives.each(){ key,val ->
		if(role.contains(key)){
			val.each(){ val2 ->
				def mockData = (val2?.mockData)?val2?.mockData:grailsApplication.config.apitoolkit.apiobject.type."${val2.paramType}".mockData
				input["${val2.name}"] = mockData
			}
		}
	}
	return input
}

List createOutput(Map returns,List role){
	//def grailsApplication = HOLDER.getGrailsApplication()
	List output = []
	role.add('permitAll')
	returns.each(){ key,val ->
		if(role.contains(key)){
			val.each(){ val2 ->
				output.add(val2.name)
			}
		}
	}
	return output
}

LinkedHashMap createMethods(LinkedHashMap methods,String cacheName,List role){
	LinkedHashMap tempGrps = ['GET':[],'POST':[],'PUT':[],'DELETE':[]]
	LinkedHashMap methodGrps = ['GET':'','POST':'','PUT':'','DELETE':'']
	List fkeys = []
	List tempKeys = []
	methods.each(){ key,val ->

		def input = createInput(val.doc.receives,role)
		def output = createOutput(val.doc.returns,role)
		
		switch(val.method.toUpperCase()){
			case 'POST':
				tempGrps['POST'].add(generatePostMethod(key,cacheName,input,output))
				break;
			case 'GET':
				tempGrps['GET'].add(generateGetMethod(key,cacheName,input,output))
				break;
			case 'PUT':
				tempGrps['PUT'].add(generatePutMethod(key,cacheName,input,output))
				break;
			case 'DELETE':
				tempGrps['DELETE'].add(generateDeleteMethod(key,cacheName,input,output))
				break;
		}

	}
	
	// formatting for template
	tempGrps.each(){ key,val ->
		val.each(){
			methodGrps[key] += it+"\r\n"
		}
	}
	return methodGrps
}

String generatePostMethod(String methodName, String className, LinkedHashMap input, List output){
	def templatePath = "$apiToolkitPluginDir/src/templates/tests/methods/Post.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String out = ""
		def templateAttributes = [className: className,methodName:methodName,inputData:input,outputData:output]
		out = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return out
	}
}

def String generateGetMethod(String methodName, String className, LinkedHashMap input, List output){
	def templatePath = "$apiToolkitPluginDir/src/templates/tests/methods/Get.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String out = ""
		def templateAttributes = [className: className,methodName:methodName,inputData:input,outputData:output]
		out = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return out
	}
}

String generatePutMethod(String methodName, String className, LinkedHashMap input, List output){
	def templatePath = "$apiToolkitPluginDir/src/templates/tests/methods/Put.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String out = ""
		def templateAttributes = [className: className,methodName:methodName,inputData:input,outputData:output]
		out = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return output
	}
}

String generateDeleteMethod(String methodName, String className, LinkedHashMap input, List output){
	def templatePath = "$apiToolkitPluginDir/src/templates/tests/methods/Delete.groovy.template"
	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		println("\nERROR: $templatePath doesn't exist")
		return null
	}else{
		String out = ""
		def templateAttributes = [className: className,methodName:methodName,inputData:input,outputData:output]
		out = templateEngine.createTemplate(templateFile.text).make(templateAttributes).toString()
		return out
	}
}

setDefaultTarget('scaffoldApiTest')