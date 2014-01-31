<%@ page import="net.nosegrind.apitoolkit.Hook" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'webhook.label', default: 'Hook')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-webhook" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>

		<div id="list-webhook" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
						<th>Service</th>
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${webhookInstanceList}" status="i" var="webhookInstance">
					<api:checkHookRole roles="${webhookInstance.roles}">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<g:set var="service" value="${webhookInstance.controller}/${webhookInstance.action}" />
						<g:set var="userHook" value="${Hook.findByServiceAndUser(service,user)}" />
						<g:if test="${webhookInstance.name}">
						<td><a href="" title="${webhookInstance.description}" onclick="return false">${webhookInstance.name}</a></td>
						</td>
						</g:if>
						<g:else>
						<td><a href="" title="${webhookInstance.description}" onclick="return false">${service}</a></td>
						</g:else>
						
						<g:if test="${userHook}">
							<td>
								<div style="float: left;">
								<g:form action="edit" id="${userHook.id}" method="post">
								<g:hiddenField name='service' value="${service}"/>
								<span class="button"><g:actionSubmit class='edit' value='edit' action="edit"/></span>
								</g:form>
								</div>

								<div style="float: left;">
								<g:form action="edit" id="${userHook.id}" method="post">
								<g:hiddenField name='service' value="${service}"/>
								<span class="button"><g:actionSubmit class='delete' value='delete' action="delete"/></span>
								</g:form>
								</div>

								<g:if test="${userHook.attempts>=5}">
								<div style="float: left;">
								<g:form action="reset" id="${userHook.id}">
								<g:hiddenField name='service' value="${service}"/>
								<span class="button"><g:actionSubmit class='save' value='reset' action="reset"/></span>
								</g:form>
								</div>
								</g:if>
							</td>
						</g:if>
						<g:else>
						<td>
							<g:form action="create" method="post">
							<g:hiddenField name='service' value="${service}"/>
							<span class="button"><g:actionSubmit class='create' value='create' action="create"/></span>
							</g:form>
						</td>
						</g:else>
					</tr>
					</api:checkHookRole>
				</g:each>
				</tbody>
			</table>
		</div>
	</body>
</html>