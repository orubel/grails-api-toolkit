# Grails API Toolkit

- <a href='https://github.com/orubel/grails-api-toolkit/wiki/Installation'>Installation</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/Configuration'>Config changes</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Docs'>Bootstrapping apidocs</a>
- <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Annotations'>Api Usage</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Annotations#wiki-api-roles'>Api Roles</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Annotations#wiki-multiple-methods'>Multiple Methods</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Annotations#wiki-sending-data-to-api'>Sending Data to API</a>
- Statuses
- <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Docs'>Apidocs</a>
    - <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Docs#wiki-apidocs-roles'>Apidocs Roles</a>
- <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Hooks'>Hooks</a>
- <a href='https://github.com/orubel/grails-api-toolkit/wiki/API-Chaining'>Api Chaining</a>
- <a href='https://github.com/orubel/grails-api-toolkit/wiki/TroubleShooting'>Troubleshooting</a>

Some of the features in the apitoolkit include:

- header generation
- support for different encoding types for return data (as set by client)
- content-type handling/output through 'content-type' header
- FULL REST methods support (HEAD, OPTIONS, GET, PUT, POST, DELETE, TRACE, PATCH)
- apidoc generation
- webhooks/real time notifications
- api linking (see documentation)
- improved perfomance
- integrated spring security for setting roles on your APIs, docs and hooks
- simplified api generation and usage

1.0 Release - 

- supports all REST methods : GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH
- dynamic link relations in uri and navigation of dataset
- adding roles to dataset to allow apidocs to generate/show based on role as api would
- improved performance
- apply convention-over-config rules to reduce amount of config for items that will only be config'd 10% of the time
- HTML doctype with links to sub datasets and linkrels
- added PKEY / FKEY / INDEX ParamTypes to show relationships in apidocs
- prettyprint JSON in apidocs
- precaching generated apidocs
- separated apidocs from annotations for separate functionality and easier annotion writing
- apis must have roles; no open api's allowed. This keeps api's from being unlimited external resources which can bog down your servers.
- api toolkit will now require spring security 2.0 >
- webhooks/notifications are now built in to the api's; you just need to set roles and authorized users need to subscribe and set a callback url.
- added roles to api annotation and unified hooks into api annotation
- allows multiple REST methods to be declared on one method call: optional REST methods and ONE required
- separation of response headers
- interpretting format through header CONTENT-TYPE (per HATEOAS specs/examples)
- simplified api call for easier routing
- generating and outputting proper headers with api calls


