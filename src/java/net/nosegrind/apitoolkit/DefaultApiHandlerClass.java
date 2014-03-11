package net.nosegrind.apitoolkit;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
  
import grails.util.GrailsNameUtils;
import grails.util.GrailsUtil;
import grails.web.Action;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaProperty;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.exceptions.NewInstanceCreationException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.ClassPropertyFetcher;
import org.codehaus.groovy.grails.commons.GrailsMetaClassUtils;

import groovy.lang.MetaProperty;

import org.springframework.beans.BeanWrapper;

@SuppressWarnings("rawtypes")
public class DefaultApiHandlerClass extends AbstractGrailsClass implements ApiHandlerClass {
	
	/*
    public Class clazz;
    public String fullName;
    public String name;
    public final String packageName;
    public final String naturalName;
    public final String shortName;
    public final String propertyName;
    public final String logicalPropertyName;
    public final ClassPropertyFetcher classPropertyFetcher;
    public boolean isAbstract;
	*/
	
    @SuppressWarnings("rawtypes")
    public DefaultApiHandlerClass(Class<?> clazz) {
    	super(clazz, ApiHandlerArtefactHandler.SUFFIX);
    }


}