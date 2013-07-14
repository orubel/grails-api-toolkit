
package net.nosegrind.restrpc

import grails.converters.JSON
import net.sf.ehcache.Element
import org.springframework.dao.DataIntegrityViolationException
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import java.lang.reflect.Method
import javax.lang.model.element.TypeElement
import grails.web.Action

class ApidocController {

	def grailsApplication

	def index(){
		redirect(action:'show')
	}
	
	def show(){
		def controllerActionNames = [:]
		def cont
		def action
		def api = []

		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controller ->
		
			Class controllerClass = controller.clazz
			cont = grailsApplication.getArtefactByLogicalPropertyName('Controller', controller.logicalPropertyName)
			def parent =[]
			// skip controllers in plugins
			if (!controllerClass.name.startsWith('net.nosegrind')) {
				String logicalControllerName = controller.logicalPropertyName
				
				// get the actions defined as methods (Grails 2)
				controllerClass.methods.each { Method method ->
		
					if (method.getAnnotation(Action)) {

						action = controller?.getClazz()?.getMethod(method.name)

						if (action.isAnnotationPresent(Api)) {
							if(parent?.parent!="${controller.logicalPropertyName}"){
								parent = [parent:"${controller.logicalPropertyName}",api:[]]
							}
							
							Api api2 = action.getAnnotation(Api)
							def apiList = [path:"${controller.logicalPropertyName}/${method.name}",method:api2.method().name(),description:api2.description(),values:[],returns:[],errors:[]]
							
							// Params
							def vals = api2.values()
							vals.each{ p ->
								def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",required:"${p.required()}",params:[]]
								def values = (p?.values())?:null
								if(values){
									values.each{ p2 ->
										if (p2.isAnnotationPresent(Params)) {
											def param = p2.getAnnotation(Params)
											def list2 = [type:param.paramType,name:param.name,description:param.description,required:param.required,params:[]]
											list.params.add(list2)
										}
									}
								}
								apiList.values.add(list)
							}
							
							// Returns
							def rets = api2.returns()
							rets.each{ p ->
								def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",required:"${p.required()}",params:[]]
								def returns = (p?.values())?:null
								if(returns){
									returns.each{ p2 ->
										if (p2.isAnnotationPresent(Params)) {
											def param = p2.getAnnotation(Params)
											def list2 = [type:param3.paramType,name:param3.name,description:param3.description,required:param3.required]
											list.params.add(list2)
										}
									}
								}
								apiList.returns.add(list)
							}

							// Errors
							def errs = api2.errors()
							errs.each{ p ->
								def list = [code:"${p.code()}",description:"${p.description()}"]
								apiList.errors.add(list)
							}
							parent.api.add(apiList)
						}
					}
				}
				if(parent?.parent){
					api.add(parent)
				}
			}

		}

		[api:api]
	}
}

package net.nosegrind.restrpc

import grails.converters.JSON
import net.sf.ehcache.Element
import org.springframework.dao.DataIntegrityViolationException
import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import java.lang.reflect.Method
import javax.lang.model.element.TypeElement
import grails.web.Action

class ApidocController {

	def grailsApplication

	def index(){
		redirect(action:'show')
	}
	
	def show(){
		def controllerActionNames = [:]
		def cont
		def action
		def api = []

		grailsApplication.controllerClasses.each { DefaultGrailsControllerClass controller ->
		
			Class controllerClass = controller.clazz
			cont = grailsApplication.getArtefactByLogicalPropertyName('Controller', controller.logicalPropertyName)
			def parent = []
			// skip controllers in plugins
			if (!controllerClass.name.startsWith('net.nosegrind')) {
				String logicalControllerName = controller.logicalPropertyName
				
				// get the actions defined as methods (Grails 2)
				controllerClass.methods.each { Method method ->
		
					if (method.getAnnotation(Action)) {

						action = controller?.getClazz()?.getMethod(method.name)

						if (action.isAnnotationPresent(Api)) {
							if(parent.parent!="${controller.logicalPropertyName}"){
								parent = [parent:"${controller.logicalPropertyName}",api:[]]
							}
							
							Api api2 = action.getAnnotation(Api)
							def apiList = [path:"${controller.logicalPropertyName}/${method.name}",method:api2.method().name(),description:api2.description(),values:[],returns:[],errors:[]]
							
							// Params
							def vals = api2.values()
							vals.each{ p ->
								def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",required:"${p.required()}",params:[]]
								def values = (p?.values())?:null
								if(values){
									values.each{ p2 ->
										if (p2.isAnnotationPresent(Params)) {
											def param = p2.getAnnotation(Params)
											def list2 = [type:param.paramType,name:param.name,description:param.description,required:param.required,params:[]]
											list.params.add(list2)
										}
									}
								}
								apiList.values.add(list)
							}
							
							// Returns
							def rets = api2.returns()
							rets.each{ p ->
								def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",required:"${p.required()}",params:[]]
								def returns = (p?.values())?:null
								if(returns){
									returns.each{ p2 ->
										if (p2.isAnnotationPresent(Params)) {
											def param = p2.getAnnotation(Params)
											def list2 = [type:param3.paramType,name:param3.name,description:param3.description,required:param3.required]
											list.params.add(list2)
										}
									}
								}
								apiList.returns.add(list)
							}

							// Errors
							def errs = api2.errors()
							errs.each{ p ->
								def list = [code:"${p.code()}",description:"${p.description()}"]
								apiList.errors.add(list)
							}
							parent.api.add(apiList)
						}
					}
				}
			
				api.add(parent)
			}

		}

		[api:api]
	}
}