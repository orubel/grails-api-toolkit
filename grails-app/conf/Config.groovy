import grails.util.Environment
import groovy.util.ConfigObject

apiName = grailsApplication.config.apitoolkit.apiName
apiVersion = grailsApplication.metadata['app.version']
apiEnv = Environment.current.name

/*
def deps = [
	"org.json-20120521.jar"
]

grails.war.dependencies = {
	fileset(dir: "lib") {
		deps.each { pattern -> include(name: pattern) }
	}
}
*/

log4j = {
	all 'org.codehaus.groovy.grails.web.mapping'
    error 'grails.app.controllers.net.nosegrind',
			  'grails.app.domain.net.nosegrind',
			  'grails.app.services.net.nosegrind.apitoolkit',
			  'grails.app.taglib.net.nosegrind.apitoolkit',
			  'grails.app.conf.your.package',
			  'grails.app.filters.your.package'
			  
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

apitoolkit.chaining.enabled=true
apitoolkit.batching.enabled=true

apitoolkit.apiobject.type = [
	"PKEY":["type":"Long","references":"self","description":"Primary Key","mockData":""],
	"FKEY":["type":"Long","description":"","mockData":""],
	"INDEX":["type":"String","references":"self","description":"Foreign Key","mockData":""],
	"String":["type":"String","description":"String","mockData":""],
	"Date":["type":"String","description":"String","mockData":""],
	"Long":["type":"Long","description":"Long","mockData":""],
	"Boolean":["type":"Boolean","description":"Boolean","mockData":""],
	"Float":["type":"Float","description":"Floating Point","mockData":""],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal","mockData":""],
	"URL":["type":"URL","description":"URL","mockData":""],
	"Email":["type":"Email","description":"Email","mockData":""],
	"Array":["type":"Array","description":"Array","mockData":""],
	"Composite":["type":"Composite","description":"Composite","mockData":""]
]

apitoolkit.apiobject.mockData = [
	"PKEY":"1",
	"FKEY":"1",
	"INDEX":'index',
	"String":'mockString',
	"Date":"1970-01-01 00:00:01",
	"Long":"1",
	"Boolean":"true",
	"Float":"0.01",
	"BigDecimal":"1234567890000",
	"URL":"www.mockdata.com",
	"Email":"test@mockdata.com",
	"Array":["type":"Array","description":"this is mockdata"],
	"Composite":["type":"Composite","description":"this is a composite","List":[1,2,3,4,5]]
]

grails.plugin.springsecurity.auth.loginFormUrl = "/${apiName}_v${apiVersion}/login/auth"
grails.plugin.springsecurity.auth.ajaxLoginFormUrl = "/${apiName}_v${apiVersion}/login/authAjax"
grails.plugin.springsecurity.failureHandler.defaultFailureUrl = '/'
grails.plugin.springsecurity.failureHandler.ajaxAuthFailUrl = '/'

grails.plugin.springsecurity.filterChain.chainMap = [
	"/${apiName}_v${apiVersion}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
	"/${apiName}_v${apiVersion}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
	"/v${apiVersion}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
	"/v${apiVersion}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter'
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"/${apiName}_v${apiVersion}*" : ['permitAll'],
	"/${apiName}_v${apiVersion}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**" : ['permitAll'],
	"/v${apiVersion}/**" : ['permitAll'],
	"/v${apiVersion}-[0-9]?[0-9]?(\\.[0-9][0-9]?)?/**" : ['permitAll'],
	"/hook/**" : ['permitAll'] ,
	"/apidoc/**" : ['permitAll']
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	'/**':             ['IS_AUTHENTICATED_ANONYMOUSLY'],
	'/hook/**':        ['IS_AUTHENTICATED_FULLY'],
	'/iostate/**':     ['IS_AUTHENTICATED_FULLY'],
	'/':                              ['permitAll'],
	'/index':                         ['permitAll'],
	'/index.gsp':                     ['permitAll'],
	'/**/js/**':                      ['permitAll'],
	'/**/css/**':                     ['permitAll'],
	'/**/images/**':                  ['permitAll'],
	'/**/favicon.ico':                ['permitAll'],
	'/login/**':					  ['permitAll'],
	'/logout/**':          		      ['permitAll']
]

apitoolkit.apiName = 'api'
apitoolkit.attempts = 5
apitoolkit.apichain.limit=3
apitoolkit.chaining.enabled=true
apitoolkit.batching.enabled=true
apitoolkit.localauth.enabled=true
apitoolkit.user.roles = ['ROLE_USER']
apitoolkit.admin.roles = ['ROLE_ROOT','ROLE_ADMIN']
apitoolkit.architecture.roles = ['ROLE_ARCH']

apitoolkit.hook.domain = 'net.nosegrind.apitoolkit.Hook'
apitoolkit.hook.controller = 'net.nosegrind.apitoolkit.HookController'

apitoolkit.serverType='master'

