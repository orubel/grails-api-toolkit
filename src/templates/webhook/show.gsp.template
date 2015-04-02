<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'webhook.label', default: 'Hook')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-webhook" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-webhook" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list webhook">
			
				<g:if test="${webhookInstance?.user}">
				<li class="fieldcontain">
					<span id="user-label" class="property-label"><g:message code="webhook.user.label" default="User" /></span>
					<span class="property-value" aria-labelledby="user-label"><g:fieldValue bean="${webhookInstance}" field="user"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="webhook.name.label" default="Name" /></span>
					<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${webhookInstance}" field="name"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.url}">
				<li class="fieldcontain">
					<span id="url-label" class="property-label"><g:message code="webhook.url.label" default="Url" /></span>
					<span class="property-value" aria-labelledby="url-label"><g:fieldValue bean="${webhookInstance}" field="url"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.format}">
				<li class="fieldcontain">
					<span id="format-label" class="property-label"><g:message code="webhook.format.label" default="Format" /></span>
					<span class="property-value" aria-labelledby="format-label"><g:fieldValue bean="${webhookInstance}" field="format"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.service}">
				<li class="fieldcontain">
					<span id="service-label" class="property-label"><g:message code="webhook.service.label" default="Service" /></span>
					<span class="property-value" aria-labelledby="service-label"><g:fieldValue bean="${webhookInstance}" field="service"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.attempts}">
				<li class="fieldcontain">
					<span id="attempts-label" class="property-label"><g:message code="webhook.attempts.label" default="Attempts" /></span>
					<span class="property-value" aria-labelledby="attempts-label"><g:fieldValue bean="${webhookInstance}" field="attempts"/></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.dateCreated}">
				<li class="fieldcontain">
					<span id="dateCreated-label" class="property-label"><g:message code="webhook.dateCreated.label" default="Date Created" /></span>
					<span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${webhookInstance?.dateCreated}" /></span>
				</li>
				</g:if>
			
				<g:if test="${webhookInstance?.lastModified}">
				<li class="fieldcontain">
					<span id="lastModified-label" class="property-label"><g:message code="webhook.lastModified.label" default="Date Modified" /></span>
					<span class="property-value" aria-labelledby="lastModified-label"><g:formatDate date="${webhookInstance?.lastModified}" /></span>
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${webhookInstance?.id}" />
					<g:link class="edit" action="edit" id="${webhookInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
