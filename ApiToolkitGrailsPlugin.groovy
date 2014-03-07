class ApiToolkitGrailsPlugin {
    def version = "1.0.18"
    def grailsVersion = "2.3 > *"
    def title = "Api Toolkit"
    def author = "Owen Rubel"
    def authorEmail = "orubel@gmail.com"
    def description = 'The Grails API Toolkit is a set of tools that automate alot of the tasks needed to build your API by combining the functionality of REST, RPC and HATEOAS. Some included functionality includes api docs, real time notifications / webhooks, generated headers, responsive content type and more.'
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
