package net.nosegrind.apitoolkit;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
  
public class DefaultApiHandlerClass extends AbstractGrailsClass implements ApiHandlerClass {
 
    public DefaultApiHandlerClass(Class clazz) {
    	super(clazz, ApiHandlerArtefactHandler.SUFFIX);
    }
     
    // This method will get the static property 'key' on the underlying 
    // ProductHandler class that it represents.
    public String getKey() { 
        Object key = GrailsClassUtils.getStaticPropertyValue(getClazz(), "key");
        if (key == null) {
            return null;
        } else {
            return key.toString();
        }
    }
}
