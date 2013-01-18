# Grails RestRPC Plugin

In traditional REST, you can one have 4 REST calls per service/class because there ARE only 4 REST requests: GET, PUT, POST, DELETE. Each of these can only be paired with ONE method in your class creating a ONE-TO-ONE relationship with the request method. RESTRPC is an interceptor api that allows the user to associate more than one method/function per request method while still being compliant with the REST standard thus creating a ONE-TO-MANY pairing.

## Source

Source code can be found at https://github.com/orubel/restrpc

## Issues

Issues can be found at https://github.com/orubel/restrpc/issues

## Feedback

Please provide feedback through the plugins [main author](https://github.com/orubel)

## Implementation

To add a webhook to your controller, you merely add the service to your controller:
```
def restRPCService
```

Then after a successful add/edit/update/delete (or other successful operation), either send the domain instance you wish via the command below:
```
webhookService.postData('<ControllerName>', <domainInstance>,'<action>')
```

... or use a Map of data in its place:
```
webhookService.postData('<ControllerName>', <Map>,',<action>')
```

Below is an example of it used in an update:
```
def update = {
    ...
    if (!bookInstance.hasErrors() && bookInstance.save(flush: true)) {
        webhookService.postData('Book', bookInstance,'update')
        ...
        redirect(action: "show", id: bookInstance.id)
    }else{
        ...
    }
}
```

This will :
* send the 'service', data and 'action taken' out to all people subscribed in the format specified by them

If you wish to cleanup your domain object prior to sending it out (to remove field data from the domain object), you can first format the domain object into a map using 'formatDomainObject' and then edit the map using the 'processMap' method and send the edited map to the service:
```
Map book = webhookService.formatDomainObject(bookInstance)
Map bookData = webhookService.processMap(book, ['author':'W Shakespeare','user':null])
webhookService.postData('Book', bookData,'save')
```

or you can do it manually...

```
def data = webhookService.formatDomainObject(bookInstance)
def Author = data.find{it.key == "Author"}?.key
data[Author] = W Shakespeare' 
def User = data.find{it.key == "User"}?.key
data.remove(User)
webhookService.postData('Book', data,'update')
```

## Usage

The Grails webhook plugin is a fully RESTful plugin and allows webhooks to be registered to individual users and for each specific service. Services MUST be declared in the config so that registered users can access and register for them. These controllers must then be setup with with one additional line upon successful completion to submit the data to the URL provided.

You can access the interface one of two ways. Either via
* the provided web interface at http://yourapp/webhook. This provides user a simple web interface to listtheir webhooks and add/edit and delete them
* REST API (see below)

## API

To use the REST api, you will need to use curl and replace 'username' and 'password' in the following command:
```
curl --data "j_username=<username>&j_password=<password>" http://localhost:8080/springSecurityApp/j_spring_security_check --cookie-jar cookies.txt
```
Once you have logged in, you can now access the webhook api with the following commands(naturally, once the server is live, replace localhost in the following with your domain or IP):

**GET**
```
curl --verbose --request GET http://localhost:8080/api/webhook/JSON/1 --cookie cookies.txt
```
**POST** (accepts formats of 'XML' or 'JSON')
```
curl --verbose --request POST --header "Content-Type: application/json" -d '{url: "http://localhost:8080/",name:"my webhook",service:'node',format:'JSON'}' http://localhost:8080/api/webhook/JSON --cookie cookies.txt
```
**PUT** (accepts formats of 'XML' or 'JSON')
```
curl --verbose --request PUT --header "Content-Type: application/json" -d "{id:4,name:'webhook change',url:'http://localhost:8080/test',service:'node',format:'JSON'}" http://localhost:8080/api/webhook/JSON --cookie cookies.txt
```
**DELETE**
```
curl --verbose --request DELETE http://localhost:8080/api/webhook/none/1 --cookie cookies.txt
```

## Troubleshooting


The most common problem is forgetting to remove 'static allowedMethods' from your Controller. If you are having problems accessing your API, make sure you have removed this from your controller.


