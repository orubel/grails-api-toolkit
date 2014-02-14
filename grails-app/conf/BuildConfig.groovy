grails.project.work.dir = 'target'
grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
		mavenCentral()
		grailsPlugins()
		grailsHome()
		mavenRepo "http://repo.spring.io/milestone/"
		mavenRepo "http://repository.codehaus.org/"
    }
	
	dependencies {
		compile "net.sf.ehcache:ehcache-core:2.4.6"
		test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
	}
	
    plugins {
		build(":release:3.0.0",":rest-client-builder:1.0.3") {
			 export = false
		}
		compile(":cache:1.1.1")
		compile ':spring-security-core:2.0-RC2'
		
		// used for testing
	    test(":spock:0.7") {
			export=false
			exclude "spock-grails-support"
	    }
		compile(":rest:0.8"){
			export=false
		}
    }
}
