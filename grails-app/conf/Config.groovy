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
	"PKEY":["type":"Long","references":"self","description":"Primary Key","mockData":"1"],
	"FKEY":["type":"Long","description":"","mockData":"1"],
	"INDEX":["type":"String","references":"self","description":"Foreign Key","mockData":"1"],
	"String":["type":"String","description":"String","mockData":"mockString"],
	"Date":["type":"String","description":"String","mockData":"1970-01-01 00:00:01"],
	"Long":["type":"Long","description":"Long","mockData":"1234"],
	"Boolean":["type":"Boolean","description":"Boolean","mockData":"true"],
	"Float":["type":"Float","description":"Floating Point","mockData":"0.01"],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal","mockData":"1234567890"],
	"URL":["type":"URL","description":"URL","mockData":"www.mockdata.com"],
	"Email":["type":"Email","description":"Email","mockData":"test@mockdata.com"],
	"Array":["type":"Array","description":"Array","mockData":["this","is","mockdata"]],
	"Composite":["type":"Composite","description":"Composite","mockData":["type":"Composite","description":"this is a composite","List":[1,2,3,4,5]]]
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

