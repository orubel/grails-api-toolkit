
log4j = {
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

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/JSON/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/XML/**" : ['permitAll'],
	"/hook/**" : ['permitAll'],
]