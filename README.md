# Grails RestRPC Plugin

In traditional REST, you can one have 4 REST calls per service/class because there ARE only 4 REST requests: GET, PUT, POST, DELETE. Each of these can only be paired with ONE method in your class creating a ONE-TO-ONE relationship with the request method. RESTRPC is an interceptor api that allows the user to associate more than one method/function per request method while still being compliant with the REST standard thus creating a ONE-TO-MANY pairing.

## Source

Source code can be found at https://github.com/orubel/restrpc

## Issues

Issues can be found at https://github.com/orubel/restrpc/issues

## Feedback

Please provide feedback through the plugins [main author](https://github.com/orubel)

## Implementation

To add RestRPC annotations to your controller, merely add the service to your controller:
```
def restRPCService
```

Then add an annotation to the method you wish to be called via an api with the request method you are going to call it through:
```
@RestRPC(request=RpcMethod.GET)
def show(Long id) { ... }
```

NOTE: Do not add annotations to methods that REDIRECT as this will throw an error; Obviosly this is bad form but to avoid alot of questions in the forums, this would be why you got that error.


With RestRPC, you can add as many GET, POST, PUT and DELETE methods as you want in your controller. As with REST, it is good form to make sure that you are matching the request method with a 'proper' function (ie DELETE request method with a 'delete' function). Naturally you can deviate from this (just as with REST) but I'm sure you have good reasons, right? :)

## Authenticate
You can test the api from curl using something similar to this
```
curl --data "j_username=admin&j_password=admin" http://localhost:8080/<yourapp>/j_spring_security_check --cookie-jar cookies.txt
```

## API

**GET**
```
curl --verbose --request GET http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/JSON/1 --cookie-jar cookies.txt
curl --verbose --request GET http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/XML/1 --cookie-jar cookies.txt
```
**POST** (accepts formats of 'XML' or 'JSON')
```
curl --verbose --request POST --header "Content-Type: application/json" -d '{fname: "Richard",lname:"Mozzarella"}' http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/JSON/1 --cookies-jar cookies.txt
curl --verbose --request POST --header "Content-Type: application/xml" -d '{fname:"Richard",lname:"Mozzarella"}' http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/XML/1 --cookies-jar cookies.txt
```

**PUT** (accepts formats of 'XML' or 'JSON')
```
curl --verbose --request PUT --header "Content-Type: application/json" -d '{fname: "Richard",lname:"Mozzarella"}' http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/JSON/1 --cookies-jar cookies.txt
curl --verbose --request PUT --header "Content-Type: application/xml" -d '{fname:"Richard",lname:"Mozzarella"}' http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/XML/1 --cookies-jar cookies.txt
```

**DELETE**
```
curl --verbose --request DELETE http://localhost:8080/<yourapp>/restrpc/<controller>/<action>/JSON/1 --cookies-jar cookies.txt
```

## Troubleshooting


The most common problem is forgetting to remove 'static allowedMethods' from your Controller. If you are having problems accessing your API, make sure you have removed this from your controller.


