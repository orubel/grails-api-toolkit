<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>

		<div id="show-apidocs" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>

				<g:each in="${api}" var="a">
					<b>PATH</b> : ${a.path}<br>
					<b>METHOD</b> : ${a.method}<br>
					<b>DESCRIPTION</b> : ${a.description}<br><br>
					
					<b>PARAMS</b><br>
					<g:each in="${a.values}" var="value">
					type : ${value.type}<br>
					name : ${value.name}<br>
					description : ${value.description}<br>
					required : ${value.required}<br>
					params:[]<br><br>
					</g:each>
					
					<b>RETURNS</b><br>
					<g:each in="${a.returns}" var="rturn">
					type : ${rturn.type}<br>
					name : ${rturn.name}<br>
					description : ${rturn.description}<br>
					required : ${rturn.required}<br>
					params:[]<br><br>
					</g:each>
					
					<b>ERRORS</b><br>
					<g:each in="${a.errors}" var="error">
					code : ${error.code}<br>
					description : ${error.description}<br>
					</g:each>
				</g:each>


		</div>
	</body>
</html>
