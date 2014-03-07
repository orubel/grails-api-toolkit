package net.nosegrind.apitoolkit;


import grails.util.GrailsNameUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException;
import org.codehaus.groovy.grails.validation.ConstraintsEvaluator;
import org.codehaus.groovy.grails.validation.DefaultConstraintEvaluator;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Validator;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
  
public class DefaultApiHandlerClass extends AbstractGrailsClass implements ApiHandlerClass {
 

	private GrailsDomainClassProperty[] properties;
	private Map<String, GrailsDomainClassProperty> propertyMap;
	private Map roleExcludeMap;

	
    public DefaultApiHandlerClass(Class clazz) {
    	super(clazz, ApiHandlerArtefactHandler.SUFFIX);
    }
     
    /**
     * Populates the domain class properties map
     *
     * @param propertyDescriptors The property descriptors
     */
    private void populateApiClassProperties(PropertyDescriptor[] propertyDescriptors) {
        for (PropertyDescriptor descriptor : propertyDescriptors) {

            if (descriptor.getPropertyType() == null) {
                // indexed property
                continue;
            }

            GrailsDomainClassProperty property = new DefaultGrailsDomainClassProperty(this, descriptor, defaultConstraints);
            propertyMap.put(property.getName(), property);

        }
    }
    
    // This method will get the static property 'key' on the underlying 
    // ProductHandler class that it represents.
    /*
    public String getKey() { 
        Object key = GrailsClassUtils.getStaticPropertyValue(getClazz(), "key");
        if (key == null) {
            return null;
        } else {
            return key.toString();
        }
    }
    */
}
