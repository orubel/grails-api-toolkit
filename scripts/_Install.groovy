import grails.util.GrailsNameUtils
import grails.util.Metadata

// add default views for apitoolkit apidocs

ant.mkdir(dir: "${basedir}/src/apiObject")
ant.mkdir(dir: "${System.properties.'user.home'}/.iostate")

def configFile = new File("${basedir}/grails-app", 'conf/Config.groovy')
if (configFile.exists()) {
	configFile.withWriterAppend {
		it.writeLine """
###Added by the Api Toolkit plugin
apitoolkit.apiName = 'api'
apitoolkit.apichain.limit=3
apitoolkit.rest.postcrement=false
apitoolkit.attempts = 5
apitoolkit.chaining.enabled=true
apitoolkit.batching.enabled=true
apitoolkit.user.roles = ['ROLE_USER']
apitoolkit.admin.roles = ['ROLE_ROOT','ROLE_ADMIN','ROLE_ARCH']

apitoolkit.apiobject.type = [
	"PKEY":["type":"Long","references":"self","description":"Primary Key","mockData":"1"],
	"FKEY":["type":"Long","description":"","mockData":"1"],
	"INDEX":["type":"String","references":"self","description":"Foreign Key","mockData":"1"],
	"String":["type":"String","description":"String","mockData":"mockString"],
	"Date":["type":"String","description":"String","mockData":"1970-01-01 00:00:01"],
	"Long":["type":"Long","description":"Long","mockData":"1234"],
	"Boolean":["type":"Boolean","description":"Boolean","mockData":"true"],
	"Float":["type":"Float","description":"Floating Point","mockData":"0.01"],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal","mockData":"1234567890"],
	"URL":["type":"URL","description":"URL","mockData":"www.mockdata.com"],
	"Email":["type":"Email","description":"Email","mockData":"test@mockdata.com"],
	"Array":["type":"Array","description":"Array","mockData":["this","is","mockdata"]],
	"Composite":["type":"Composite","description":"Composite","mockData":["type":"Composite","description":"this is a composite","List":[1,2,3,4,5]]]
]
		"""
		
	}
	
	println """
	************************************************************
	* SUCCESS! You have successfully installed the API Toolkit *
	************************************************************
"""
}

