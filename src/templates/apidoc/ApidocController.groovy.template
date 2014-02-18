package net.nosegrind.apitoolkit

import org.springframework.security.access.annotation.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

@Secured('permitAll')
class ApidocController {

	def apiToolkitService
	def apiCacheService
	
	def index(){
		redirect(action:'show')
	}
	
	@Secured('permitAll')
	def show(){
		Map docs = [:]
		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			def cache = apiCacheService.getApiCache(controllername)
			if(cache){
				cache.each(){ it ->
					def newDocs=apiToolkitService.generateDoc(controllername, it.key)
					if(newDocs){
						println(newDocs)
						docs["${controllername}"] = [:]
						docs["${controllername}"]["${it.key}"]=newDocs["${it.key}"]
					}
				}

			}
		}
		[apiList:docs]
	}
}

