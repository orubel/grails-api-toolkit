import net.nosegrind.apitoolkit.ApiHandlerArtefactHandler

class ApiToolkitGrailsPlugin {
    def version = "1.0.20"
    def grailsVersion = "2.3 > *"
    def title = "Api Toolkit"
    def author = "Owen Rubel"
    def authorEmail = "orubel@gmail.com"
    def description = 'The Grails API Toolkit simplifies api development within Grails while adding in much needed functionality such as apidocs, real time notifications/webhooks, role checking and more. The Api Toolkit is meant to provide all the tools you need in a simplified manner to get up and running with your api without spending days trying to configure and setup.'
    def documentation = "https://github.com/orubel/grails-api-toolkit"

    def license = "Apache"
    def issueManagement = [system: 'GitHub', url: 'https://github.com/orubel/grails-api-toolkit/issues']
    def scm = [url: 'https://github.com/orubel/grails-api-toolkit']
	
	def artefacts = [ApiHandlerArtefactHandler]

	// watch for any changes in these directories
	def watchedResources = [
		"file:./grails-app/apiHandlers/**/*ApiHandler.groovy"
	]
	
	def onChange = { event ->
		if (application.isArtefactOfType(ApiHandlerArtefactHandler.TYPE, event.source)) {
			def oldClass = application.getApiHandlerClass(event.source.name)
			application.addArtefact(ApiHandlerArtefactHandler.TYPE, event.source)
 
			// Reload subclasses
			application.apiHandlerClasses.each {
				if (it.clazz != event.source && oldClass.clazz.isAssignableFrom(it.clazz)) {
					def newClass = application.classLoader.reloadClass(it.clazz.name)
					application.addArtefact(ApiHandlerArtefactHandler.TYPE, newClass)
				}
			}
		}
	}
}
