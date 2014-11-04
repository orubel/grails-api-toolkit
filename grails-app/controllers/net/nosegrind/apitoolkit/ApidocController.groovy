package net.nosegrind.apitoolkit

import org.springframework.security.access.annotation.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass

@Secured('permitAll')
class ApidocController {

	def apiToolkitService
	def apiCacheService
	def springSecurityService
	
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
						if(!docs["$controllername"]){
							docs["${controllername}"] = [:]
						}
						docs["${controllername}"]["${it.key}"]=newDocs["${it.key}"]
					}
				}

			}
		}
		
		String authority = springSecurityService.principal.authorities*.authority[0]
		authority = (authority=='ROLE_ANONYMOUS')?'permitAll':authority
		[apiList:docs,authority:authority]
	}
}

