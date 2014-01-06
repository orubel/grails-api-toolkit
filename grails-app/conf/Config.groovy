
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
	"/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/JSON/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/XML/**" : ['permitAll'],
	"/${grailsApplication.config.apitoolkit.apiName}/${grailsApplication.metadata['app.version']}/HTML/**" : ['permitAll'],
	"/hook/**" : ['permitAll'] 
]

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
