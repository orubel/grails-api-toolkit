class ApiToolkitGrailsPlugin {
    def version = "2.31"
    def grailsVersion = "2.4 > *"
    def title = "Api Toolkit"
    def author = "Owen Rubel"
    def authorEmail = "orubel@gmail.com"
    def description = 'The Grails API Toolkit is a complete set of API tools and an API abstraction layer that automates alot of the tasks needed to build your API like api docs, real time notifications / webhooks, generated headers, responsive content type and more. This also abstracts the API away from the Controller/model with a more universal API Object that can be defined with roles, rules and definitions for handling the request and response at the front controller.'
    def documentation = "https://github.com/orubel/grails-api-toolkit-docs"
    def license = "Apache"
    def issueManagement = [system: 'GitHub', url: 'https://github.com/orubel/grails-api-toolkit-docs/issues']
    def scm = [url: 'https://github.com/orubel/grails-api-toolkit']
	
	/*
	def doWithSpring = {
		String host = application.config.apitoolkit.sharedCache.url
		String port = application.config.apitoolkit.sharedCache.port
		String user = application.config.apitoolkit.sharedCache.user
		String password = application.config.apitoolkit.sharedCache.password
		String bucket = 'iostate'
		
		def mongo = new com.mongodb.Mongo(host, port instanceof Integer ? post : 27017)
		def credentials = new org.springframework.data.authentication.UserCredentials(user instanceof String ? user : '', password instanceof String ? password : '')
		mongoDbFactory(org.springframework.data.mongodb.core.SimpleMongoDbFactory, mongo, bucket, credentials)
	}
	*/
}
