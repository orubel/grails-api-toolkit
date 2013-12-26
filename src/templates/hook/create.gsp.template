<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'webhook.label', default: 'Hook')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#create-webhook" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="create-webhook" class="content scaffold-create" role="main">
			<h1><g:message code="default.create.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${webhookInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${webhookInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form action="save" >
			<g:hiddenField name='service' value="${params.service}"/>
				<fieldset class="form">
				
					<div class="fieldcontain ${hasErrors(bean: webhookInstance, field: 'url', 'error')} ">
						<label for="url">
							<g:message code="webhook.callback.label" default="Callback" />
						</label>
						<g:if test="${grailsApplication.config.apitoolkit.roles}">
							<api:checkRole roles="${grailsApplication.config.apitoolkit.roles}">
								<textarea cols="50" id="callback" name="callback" rows="4">${params.callback}</textarea>
							</api:checkRole>
						</g:if>
						<g:else>
							<g:textField name="callback" value="${params.callback}"/>
						</g:else>
					</div>

					<div class="fieldcontain ${hasErrors(bean: webhookInstance, field: 'format', 'error')} ">
						<label for="format">
							<g:message code="webhook.format.label" default="Format" />
						</label>
						<span class="styled-select"><g:select name="format" from="${format}" value="${webhookInstance?.format}"/></span>
					</div>

				</fieldset>
				<fieldset class="buttons">
					<g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
