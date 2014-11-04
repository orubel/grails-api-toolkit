package net.nosegrind.apitoolkit

import org.springframework.security.access.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException
import net.nosegrind.apitoolkit.*
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import net.nosegrind.apitoolkit.Hook

@Secured(['ROLE_ADMIN','ROLE_USER'])
class HookController {

	def springSecurityService
	def apiCacheService
	def apiToolkitService

	def list() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}
		
		List services = []
		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				Map cont = apiCacheService.getApiCache(controllername)
				cont.each{
					Method method = controllerClass.getClazz().getDeclaredMethod(it.key)
					if(method.isAnnotationPresent(Api)) {
						def api = method.getAnnotation(Api)
						String action = method.getName()
						if(cont["${action}"]["hookRoles"].getClass()){
							if(apiToolkitService.checkHookAuthority(cont["${action}"]["hookRoles"])){
								Map temp = ['controller':controllername,'action':action,'roles':cont["${action}"]["hookRoles"],'name':cont["${action}"]["name"],'description':cont["${action}"]["description"],'json':cont["${action}"]['doc']['json']]
								services.add(temp)
							}
						}
					}
				}
			}
		}
		
		//def webhookList = isSuperuser() ? Hook.list(params) : Hook.findAllByUser(user, [max:params.max, sort:params.sort, order:params.order, offset:params.offset] )
		//params.max = Math.min(params.max ? params.int('max') : 10, 100)

		[webhookInstanceList: services,params:params,user:user]
	}

	def create() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}

		List formats = ['JSON','XML']
		
		List temp = params.service.split('/')
		def cont = apiCacheService.getApiCache(temp[0])
		if(cont){
			if(cont["${temp[1]}"]["hookRoles"]){
				if(apiToolkitService.checkHookAuthority(cont["${temp[1]}"]["hookRoles"])){
					Hook service = Hook.findByServiceAndUser(params.service,user)
					if(service){
						flash.message = "You are already subscribed to this service. Please edit it instead."
						render(view:"edit",model:[id:service.id,params:params,format:formats])
					}
				}else{
					render([error: 'access denied'])
				}
			}else{
				render([error: 'access denied'])
			}
		}else{
			render([error: 'access denied'])
		}

		[format:formats,params:params]
	}


	def save() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}
		List formats = ['JSON','XML']
		params.callback = params.callback.split("\n")
		List temp = params.service.split('/')
		def cont = apiCacheService.getApiCache(temp[0])
		if(cont){
			if(cont["${temp[1]}"]["hookRoles"]){
				if(apiToolkitService.checkHookAuthority(cont["${temp[1]}"]["hookRoles"])){
					Hook hook = Hook.findByServiceAndUser(params.service,user)
					if(hook){
						flash.message = "You are already subscribed to this service. Please edit it instead."
						render(view:"edit",model:[id:hook.id,params:params,format:formats])
					}else{
						params.callback.each{ url ->
							if(!apiToolkitService.validateUrl(url)){
								flash.message = "BAD PROTOCOL: URL '${url}'MUST BE FULLY QUALIFIED DOMAIN NAME (OR IP ADDRESS) FORMATTED WITH HTTP/HTTPS. PLEASE TRY AGAIN."
								render(view:"create",model:[params:params,service:params.service,format:formats])
								return
							}
						}

						hook.callback=(params.callback instanceof String)?params.callback:params.callback.join(',')
						hook.service=params.service
						hook.user=user
				
						if (hook.save(flush: true)) {
							flash.message = message(code: 'default.created.message', args: [message(code: 'hook.label', default: 'Hook'), hook.id])
							chain(action:"list", params:params)
							return
						}
						
						flash.message = "INVALID/MALFORMED DATA: PLEASE SEE DOCS FOR 'JSON' FORMED STRING AND PLEASE TRY AGAIN."
						render(view:"create",model:[params:params,service:params.service,format:formats])
					}
				}else{
					render([error: 'access denied'])
				}
			}else{
				render([error: 'access denied'])
			}
		}else{
			render([error: 'access denied'])
		}
	}

	def edit() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}
		
		Hook hook = Hook.get(params.id)
		String callback
		List temp = params.service.split('/')
		def cont = apiCacheService.getApiCache(temp[0])
		if(cont){
			if(cont["${temp[1]}"]["hookRoles"]){
				if(apiToolkitService.checkHookAuthority(cont["${temp[1]}"]["hookRoles"])){
					Hook service = Hook.findByServiceAndUser(params.service,user)
					if(hook){
						callback = hook.callback.replace(',','\n')
					}else{
						flash.message = "WEBHOOK NOT FOUND: NO WEBHOOK WITH THAT ID FOUND BELONGING TO CURRENT USER."
						render(view:"create",model:[params:params])
						return
					}
				}else{
					render([error: 'access denied'])
				}
			}else{
				render([error: 'access denied'])
			}
		}else{
			render([error: 'access denied'])
		}

		[webhookInstance: hook,callback:callback,params:params,format:['JSON','XML']]
	}
	
	def update() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}

		List callback = params.callback.split('\n')
		List temp = params.service.split('/')
		def cont = apiCacheService.getApiCache(temp[0])
		if(cont){
			if(cont["${temp[1]}"]["hookRoles"]){
				if(apiToolkitService.checkHookAuthority(cont["${temp[1]}"]["hookRoles"])){
					Hook hook = Hook.findByServiceAndUser(params.service,user)
					if(hook){
						// continue editing service
						callback.each{ url ->
							if(!apiToolkitService.validateUrl(url)){
								flash.message = "BAD PROTOCOL: URL '${url}' MUST BE FULLY QUALIFIED DOMAIN NAME (OR IP ADDRESS) FORMATTED WITH HTTP/HTTPS. PLEASE TRY AGAIN."
								render(view:"edit",model:[params:params,service:params.service,format:formats])
								return
							}
						}
						
						hook.callback = (callback instanceof String)?callback:callback.join(',')
						hook.format = params.hookformat
						hook.service = params.service
						
						if (hook.save(flush: true)) {
							//apiToolkitService.callHook('hook',hook,'update')
							flash.message = message(code: 'default.updated.message', args: [message(code: 'webhook.label', default: 'Hook'), hook.id])
							chain(controller:'hook',action:'list', params:params)
							return
						}else{
							hook.errors.each { 
								log.info("[Error]: net.nosegrind.apitoolkit.HookController.update : unable to save Hook : ${it}")
							}
						}
						
						flash.message = "INVALID/MALFORMED DATA: PLEASE SEE DOCS FOR 'JSON' FORMED STRING AND PLEASE TRY AGAIN."
						render(view:"list",model:[params:params])
						return
					}else{
						flash.message = "WEBHOOK NOT FOUND: NO WEBHOOK WITH THAT ID FOUND BELONGING TO CURRENT USER."
						render(view:"create",model:[params:params,format:['JSON','XML']])
						return
					}
				}else{
					render([error: 'access denied'])
				}
			}else{
				render([error: 'access denied'])
			}
		}else{
			render([error: 'access denied'])
		}

	}

	def delete() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}

		def webhookInstance = isSuperuser() ? Hook.get(params.id) : Hook.findByPersonAndId(user,params.id.toLong())

		if (!webhookInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'webhook.label', default: 'Hook'), params.id])
			redirect(action: "list")
			return
		}

		try {
			webhookInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'webhook.label', default: 'Hook'), params.id])
			render(view:"list",model:[params:params])
			return
		}catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'webhook.label', default: 'Hook'), params.id])
			render(view:"list",model:[params:params])
			return
		}
	}

	def reset() {
		def user = loggedInUser()
		if(!user){
			redirect(uri: "/")
			return
		}

		Hook webhookInstance = Hook.findByIdAndUser(params.id, user)
		if(!webhookInstance){
			flash.message = "WEBHOOK NOT FOUND: NO WEBHOOK WITH THAT ID FOUND BELONGING TO CURRENT USER."
			render(view:"list",model:[params:params])
			return
		}

		webhookInstance.attempts = 0

		if (webhookInstance.save(flush: true)) {
			flash.message = message(code: 'default.reset.message', args: [message(code: 'webhook.label', default: 'Hook'), webhookInstance.id])
			//render(view:"list",model:[params:params])
			chain(controller:'hook',action:'list', params:params)
			return
		}
	}

	protected loggedInUser() {
		springSecurityService.isLoggedIn() ? Person.load(springSecurityService.principal.id) : null
	}

	protected boolean isSuperuser() {
		springSecurityService.principal.authorities*.authority.any { grailsApplication.config.apitoolkit.roles.contains(it) }
	}
}
