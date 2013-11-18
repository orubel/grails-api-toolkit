grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
		mavenCentral()
		grailsPlugins()
		grailsHome()
    }

    plugins {
		build(':release:2.2.1', ':rest-client-builder:1.0.3',":tomcat:$grailsVersion",
			":hibernate:$grailsVersion") {
		  export = false
		}

    }
}
