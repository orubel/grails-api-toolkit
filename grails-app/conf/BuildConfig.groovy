grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
		mavenCentral()
		grailsPlugins()
		grailsHome()
		mavenRepo "http://repo.spring.io/milestone/"
    }
	
    plugins {
		compile ':spring-security-core:2.0-RC2'
		build(':release:2.3.5', ':rest-client-builder:1.0.3',':cache:1.1.1') {
		  export = false
		}
    }
}
