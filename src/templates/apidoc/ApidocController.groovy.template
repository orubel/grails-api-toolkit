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
					String actionname = it.key
					it.value.each(){ it2 ->
						def newDocs=apiToolkitService.generateDoc(controllername, actionname,it2.key)
						if(newDocs){
							if(!docs["\$controllername"]){
								docs["\${controllername}"] = [:]
							}
							if(!docs["\$controllername"]["\${actionname}"]){
								docs["\${controllername}"]["\${actionname}"] = [:]
							}
							docs["\${controllername}"]["\${actionname}"]["\${it2.key}"]=newDocs["\${actionname}"]["\${it2.key}"]
						}
					}
				}

			}
		}
		
		String authority = springSecurityService.principal.authorities*.authority[0]
		authority = (authority=='ROLE_ANONYMOUS')?'permitAll':authority
		[apiList:docs,authority:authority]
	}
}

