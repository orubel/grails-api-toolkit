import grails.util.Environment
import groovy.util.ConfigObject

apiName = grailsApplication.config.apitoolkit.apiName
apiVersion = grailsApplication.metadata['app.version']
apiEnv = Environment.current.name

/*
def deps = [
	"couchbase-java-client-2.1.1.jar"
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
	"PKEY":["type":"Long","references":"self","description":"Primary Key"],
	"FKEY":["type":"Long","description":""],
	"INDEX":["type":"String","references":"self","description":"Foreign Key"],
	"String":["type":"String","description":"String"],
	"Date":["type":"String","description":"String"],
	"Long":["type":"Long","description":"Long"],
	"Boolean":["type":"Boolean","description":"Boolean"],
	"Float":["type":"Float","description":"Floating Point"],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal"],
	"URL":["type":"URL","description":"URL"],
	"Email":["type":"Email","description":"Email"]
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
