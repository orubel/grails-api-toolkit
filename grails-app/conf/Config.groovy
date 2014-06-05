
String apiName = grailsApplication.config.apitoolkit.apiName
String apiVersion = grailsApplication.metadata['app.version']

log4j = {
    error 'grails.app.services.net.nosegrind.apitoolkit',
		'grails.app.taglib.net.nosegrind.apitoolkit',
		'grails.app.conf',
		'grails.app.filters'
}

grails.converters.default.pretty.print = true
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

apitoolkit.apiName = 'api'
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

apitoolkit.apiobject.method = [
	"GET":["id":["require":"true"]],
	"PUT":["id":["require":"false"]],
	"POST":["id":["require":"true"]],
	"DELETE":["id":["require":"true"]]
]

apitoolkit.apiobject.type = [
	"PKEY":["type":"Long","references":"self","description":"Primary Key"],
	"FKEY":["type":"Long","description":""],
	"INDEX":["type":"String","references":"self","description":"Foreign Key"],
	"String":["type":"String","description":"String"],
	"Long":["type":"Long","description":"Long"],
	"Boolean":["type":"Boolean","description":"Boolean"],
	"Float":["type":"Float","description":"Floating Point"],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal"],
	"URL":["type":"URL","description":"URL"],
	"Email":["type":"Email","description":"Email"]
]

grails.plugin.springsecurity.filterChain.chainMap = [
	"/${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
	"/v${grailsApplication.metadata['app.version']}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"${grailsApplication.config.apitoolkit.apiName}_v${grailsApplication.metadata['app.version']}/**" : ['permitAll'],
	"v${grailsApplication.metadata['app.version']}/**" : ['permitAll'],
	"/hook/**" : ['permitAll'] 
]
