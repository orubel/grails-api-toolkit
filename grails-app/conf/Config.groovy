
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

apitoolkit.apichain.limit=5

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

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/JSON/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/XML/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}_${grailsApplication.metadata['app.version']}/HTML/**" : ['permitAll'],
	"/hook/**" : ['permitAll'] 
]
