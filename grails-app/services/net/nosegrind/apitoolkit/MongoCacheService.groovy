/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *****************************************************************************/
package net.nosegrind.apitoolkit

import grails.util.Holders
import grails.converters.JSON
import grails.converters.XML
import groovy.json.JsonOutput
import org.codehaus.groovy.grails.web.json.JSONObject

import java.lang.reflect.Method
import java.util.HashSet
import java.net.URI
import java.util.ArrayList
import java.util.LinkedHashMap;
import java.util.List

import com.mongodb.DB
import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.Mongo
import com.mongodb.MongoClient

import net.nosegrind.apitoolkit.*
import grails.util.Environment

class MongoCacheService{

	static transactional = false
	
	def grailsApplication
	//SpringSecurityService springSecurityService
	def mongoDbFactory
	DB db
	
	public initialize(){
		try {
			db = mongoDbFactory.getDb()
			String ioPath

			if(grailsApplication.isWarDeployed()){
				ioPath = Holders.servletContext.getRealPath('/')
				if(Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST){
					ioPath += 'WEB-INF/classes/iostate'
				}else{
					// test in Environment.PRODUCTION
					ioPath += 'WEB-INF/classes/iostate'
				}
			}else{
				ioPath = grails.util.BuildSettingsHolder.settings?.resourcesDir?.path
				if(Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST){
					ioPath += '/iostate'
				}else{
					// test in Environment.PRODUCTION
					ioPath += '/iostate'
				}
			}
			parseFiles(ioPath)
			
			String apiObjectSrc = grailsApplication.config.apitoolkit.iostate.preloadDir.toString()
			parseFiles(apiObjectSrc)
		} catch (Exception e) {
			throw new Exception("[MongoCacheService :: initialize] : Exception - full stack trace follows:",e)
		}
	}

	private Map parseFile(String file){
		JSONObject json = JSON.parse(file.text)
		return json
	}
	
	private parseFiles(String path){
		new File(path).eachFile() { file ->
			try{
				JSONObject json = JSON.parse(file.text)
				if(!db.collectionExists(json.NAME.toString())){
					Map methods = [:]
					methods = parseJson(json.NAME.toString(),json);
					createIoState(json.NAME.toString(),methods)
				}
			}catch(Exception e){
				throw new Exception("[MongoCacheService :: initialize] : Unacceptable file '${file.name}' - full stack trace follows:",e)
			}
		}
	}
	
	/*
	 * Placeholder Function for Proxy; not used in API Application
	 */
	public List getIoStateNames(){
		List<String> collectionNames = db.getCollectionNames() as List
		return collectionNames
	}
	
	public getIoState(String name){
		def collection = db.getCollectionFromString(name)
	}
	
	/*
	 * Only called at init
	 */
	private createIoState(String apiObjectName, Map methods){
		def json = JsonOutput.toJson(methods)
		//org.json.JSONObject jObj = (org.json.JSONObject) 
		DBObject dbObject = (DBObject) com.mongodb.util.JSON.parse(json)
		
		//DBObject dbObject = (DBObject) new BasicDBObject(methods)

		db.createCollection(apiObjectName,dbObject)
		db.getCollection(apiObjectName).insert(dbObject)
		
	}
	
	/*
	 * overwrite original FILE and update cache
	 */
	public updateIoState(String file){
		JSONObject json1 = parseFile(file)
		String apiObjectName = json1.NAME.toString()
		
		Map methods = parseJson(json1.NAME.toString(),json1)
		def json2 = JsonOutput.toJson(methods)

		try{
		DBObject dbObject = (DBObject) com.mongodb.util.JSON.parse(json2)
		db.getCollection(apiObjectName).update( '{ _id : { $exists : true } }', dbObject, upsert, true);
		}catch(Exception e){
			log.error("[ApiDomainService :: updateInstance] : Could not find domain package '${domainPackage}' - full stack trace follows:", e);
		}
	}
	
	/*
	 * rather than delete, change to unsupported ROLE
	 * until functionality is removed
	public deleteIoState(String name){
		def collection = db.getCollectionFromString(name)
	}
	*/
	
	public Map parseJson(String apiName,JSONObject json){
		Map methods = [:]

		json.VERSION.each() { vers ->
			String defaultAction = (vers.value.DEFAULTACTION)?vers.value.DEFAULTACTION:'index'
			List deprecated = (vers.value.DEPRECATED)?vers.value.DEPRECATED:[]
			String domainPackage = (vers.value.DOMAINPACKAGE!=null || vers.value.DOMAINPACKAGE?.size()>0)?vers.value.DOMAINPACKAGE:null
			vers.value.URI.each() { it ->

				JSONObject apiVersion = json.VERSION[vers.key]
				
				//List temp = it.key.split('/')
				//String actionname = temp[1]
				String actionname = it.key
				
				ApiStatuses error = new ApiStatuses()
				
				Map apiDescriptor
				Map apiParams
				
				String apiMethod = it.value.METHOD
				String apiDescription = it.value.DESCRIPTION
				List apiRoles = it.value.ROLES
				List batchRoles = it.value.BATCH
				
				String uri = it.key
				apiDescriptor = checkApiDescriptor(apiName, apiMethod, apiDescription, apiRoles, batchRoles, uri, json.get('VALUES'), apiVersion)
				if(!methods[vers.key]){
					methods[vers.key] = [:]
				}
				
				if(!methods['currentStable']){
					methods['currentStable'] = [:]
					methods['currentStable']['value'] = json.CURRENTSTABLE
				}
				if(!methods[vers.key.toString()]['deprecated']){
					methods[vers.key.toString()]['deprecated'] = []
					methods[vers.key.toString()]['deprecated'] = deprecated
				}
				
				if(!methods[vers.key.toString()]['defaultAction']){
					methods[vers.key.toString()]['defaultAction'] = defaultAction
				}

				if(!methods[vers.key.toString()]['domainPackage']){
					methods[vers.key.toString()]['domainPackage'] = domainPackage
				}

				methods[vers.key.toString()][actionname] = apiDescriptor

			}
			if(methods){
				return methods
				//apiLayerService.setApiCache(apiName,methods)
			}
		}
	}
	
	String getKeyType(String reference, String type){
		String keyType = (reference.toLowerCase()=='self')?((type.toLowerCase()=='long')?'PKEY':'INDEX'):((type.toLowerCase()=='long')?'FKEY':'INDEX')
		return keyType
	}
	
	private Map checkApiDescriptor(String apiname,String apiMethod, String apiDescription, List apiRoles, List batchRoles, String uri, JSONObject values, JSONObject json){
		LinkedHashMap<String,ParamsDescriptor> apiObject = [:]
		ApiParams param = new ApiParams()
		Map descriptor = [:]
		values.each{ k,v ->
			String references = ""
			String hasDescription = ""
			String hasMockData = ""
			
			v.type = (v.references)?getKeyType(v.references, v.type):v.type

			param.setParam(v.type,k)
			
			def configType = grailsApplication.config.apitoolkit.apiobject.type."${v.type}"
			
			hasDescription = (configType?.description)?configType.description:hasDescription
			hasDescription = (v?.description)?v.description:hasDescription
			if(hasDescription){ param.hasDescription(hasDescription) }
			
			references = (configType?.references)?configType.references:""
			references = (v?.references)?v.references:references
			if(references){ param.referencedBy(references) }
			
			hasMockData = (v?.mockData)?v.mockData:hasMockData
			if(hasMockData){ param.hasMockData(hasMockData) }

			// collect api vars into list to use in apiDescriptor
			def test = param.toObject()
			apiObject[param.param.name.toString()] = param.toObject()
		}
		
		LinkedHashMap receives = getIOMap(json.URI[uri]?.REQUEST,apiObject)
		LinkedHashMap returns = getIOMap(json.URI[uri]?.RESPONSE,apiObject)
		
		try{
			descriptor = [
				'method':"$apiMethod".toString(),
				'description':"$apiDescription".toString(),
				'roles':[],
				'batchRoles':[],
				'doc':[:],
				'receives':receives,
				'returns':returns
			]
			descriptor['roles'] = apiRoles
			descriptor['batchRoles'] = batchRoles
			
			ApiDescriptor service = new ApiDescriptor(
				'method':descriptor.method,
				'description':descriptor.description,
				'roles':descriptor.roles,
				'batchRoles':descriptor.batchRoles,
				'doc':descriptor.doc,
				'receives':descriptor.receives,
				'returns':descriptor.returns
			)
			
		}catch(Exception e){
			throw new Exception("[MongoCacheService :: checkApiDescriptor] : Exception - full stack trace follows:",e)
		}
		return descriptor
	}
	
	private LinkedHashMap getIOMap(JSONObject io,LinkedHashMap apiObject){
		LinkedHashMap<String,ParamsDescriptor> ioMap = [:]

		io.each{ k, v ->
			// init
			if(!ioMap[k]){
				ioMap[k] = []
			}
			

			def roleVars=v.toList()
			roleVars.each{ val ->
				if(v.contains(val)){
					if(!ioMap[k].contains(apiObject[val])){
						ioMap[k].add(apiObject[val].toString())
					}
				}
			}

		}
		
		// add permitAll vars to other roles after processing
		ioMap.each(){ key, val ->
			if(key!='permitAll'){
				ioMap['permitAll'].each(){ it ->
						ioMap[key].add(it.toString())
				}
			}
		}
		
		return ioMap
	}
}
