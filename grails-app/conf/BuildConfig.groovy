grails.project.work.dir = 'target'
grails.project.dependency.resolver = "ivy" // or ivy
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"

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
		mavenRepo 'http://repo.spring.io/libs-milestone/'
    }
	
	management {
		dependency 'org.springframework:spring-beans:4.0.7.RELEASE'
	}
	
	dependencies {
		compile("net.sf.ehcache:ehcache-core:2.4.6")
		test ("org.spockframework:spock-grails-support:0.7-groovy-2.0"){
			exclude 'groovy-all'
		}
		//compile ('org.springframework.data:spring-data-mongodb:1.3.0.RELEASE') {
		//	excludes('spring-core', 'spring-context', 'spring-expression')
		//}
	}
	
    plugins {
		build(":release:3.0.0",":rest-client-builder:1.0.3") {
			 export = false
		}
		compile(":cache:latest.release")
		compile(":spring-security-core:2.0-RC3")
		//test ":funky-spock:0.2.2"
		compile(":functional-spock:0.7"){
			exclude "spock-grails-support"
		}
		compile(":rest:0.8"){
			export=false
		}
    }
}

