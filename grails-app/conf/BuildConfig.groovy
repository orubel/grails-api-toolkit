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
    }
	
    plugins {
		build(":release:3.0.0",":rest-client-builder:1.0.3") {
			 export = false
		}
		compile ":cache:1.1.1"
		compile ':spring-security-core:2.0-RC2'
		runtime ":hibernate:3.6.10.6"{
			export = false
		}
    }
}
