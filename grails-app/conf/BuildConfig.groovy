grails.project.work.dir = 'target'
grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

	inherits("global")
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
		compile("net.sf.ehcache:ehcache-core:2.4.6")
	}
	
    plugins {
		build(":release:3.0.0",":rest-client-builder:1.0.3") {
			 export = false
		}
		compile(":cache:latest.release")
		compile(":spring-security-core:2.0-RC3")
		compile(":rest:0.8"){
			export=false
		}
    }
}

