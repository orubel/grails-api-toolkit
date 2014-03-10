package net.nosegrind.apitoolkit;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
  
public class DefaultApiHandlerClass extends AbstractGrailsClass implements ApiHandlerClass {
	
    public DefaultApiHandlerClass(Class clazz) {
    	super(clazz, ApiHandlerArtefactHandler.SUFFIX);
    }

}
