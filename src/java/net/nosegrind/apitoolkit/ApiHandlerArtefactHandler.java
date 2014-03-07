package net.nosegrind.apitoolkit;

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;

public class ApiHandlerArtefactHandler extends ArtefactHandlerAdapter {
 
    // the name for these artefacts in the application
    static public final String TYPE = "ApiHandler"; 
 
    // the suffix of all api handler classes
    static public final String SUFFIX = "ApiHandler"; 
     
    public ApiHandlerArtefactHandler() {
        super(TYPE, ApiHandlerClass.class, DefaultApiHandlerClass.class, SUFFIX);
    }
}
