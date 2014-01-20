
String apiName = grailsApplication.config.apitoolkit.apiName
String apiVersion = grailsApplication.metadata['app.version']

log4j = {
	debug 'org.springframework.security'
    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}

grails.cache.enabled = true
grails.cache.clearAtStartup	= true
grails.cache.config = {
	cache {
		name 'ApiCache'
		eternal true
		overflowToDisk true
		maxElementsInMemory 10000
		maxElementsOnDisk 10000000
	}
 }

apitoolkit.apichain.limit=3
apitoolkit.protocol='http'

apitoolkit.defaultData.PKEY = '26'
apitoolkit.defaultData.FKEY = '32'
apitoolkit.defaultData.INDEX = '26'
apitoolkit.defaultData.STRING = 'Hello World'
apitoolkit.defaultData.BOOLEAN = 'true'
apitoolkit.defaultData.FLOAT = '1.00'
apitoolkit.defaultData.BIGDECIMAL = '123567828794.87'
apitoolkit.defaultData.LONG = '18926'
apitoolkit.defaultData.EMAIL = 'example@yoursite.com'
apitoolkit.defaultData.URL = 'http://www.yoursite.com'

grails.plugin.springsecurity.filterChain.chainMap = [
	"/${apiName}_${apiVersion}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/**" : ['permitAll'],
	"/hook/**" : ['permitAll'] 
]
