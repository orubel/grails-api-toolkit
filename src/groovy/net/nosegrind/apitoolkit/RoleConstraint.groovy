package net.nosegrind.apitoolkit;

import org.codehaus.groovy.grails.validation.AbstractConstraint
import org.springframework.validation.Errors

/*
 * class to add ROLES to constraints
 * merely checks to see if roles exist in spring sec 'role' table
 */
class RoleConstraint extends AbstractConstraint {
	
	def springSecurityService
	
	static NAME = 'roles'

	boolean supports(Class type) {
		true
	}

	String getName() {
		NAME
	}
	
	/** Sets the constraintParameter value
	 * (first checking if the constraint parameter is of the correct type). */
	void setParameter(Object param) {
		if (!(param instanceof Boolean)){
			throw new IllegalArgumentException("Parameter for constraint [$name] of property [$constraintPropertyName] of class [$constraintOwningClass] must be a list of roles.")
		}
		super.setParameter(param)
	}
	
	protected void processValidate(Object target, Object value, Errors errors) {
		if (constraintParameter){
			if(value && !validate(target, value)){
				rejectValue target, errors, "default.invalid.${name}.message", "${name}.invalid",[constraintPropertyName, constraintOwningClass, value] as Object[]
			}
		}
	}
		
	/** separation for better testing. */
	def validate(target, value) {
		!springSecurityService.principal.authorities*.authority.any { value.contains(it) }
	}

}
