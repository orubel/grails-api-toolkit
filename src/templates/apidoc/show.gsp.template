<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Api Docs</title>

	</head>

	<body style="color: #393939;font: 13px/1.4em helvetica,arial,freesans,clean,sans-serif;">

		<div id="show-apidocs" class="content scaffold-show" role="main">
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
		</div>
		
		<div style="margin-left: auto; margin-right: auto;width: 800px;">
		<g:each in="${apiList}" var="controller">
			<br>
			<h3>${controller.key.toUpperCase()}</h3>
			
			<g:each in="${controller.value}" var="action">
			<g:set var="action" value="${action.key}"/> 

			<g:if test="${action.value.path}"><g:set var="path" value="${action.value.path}"/></g:if> 
			<g:if test="${action.value.json}"><g:set var="json" value="${action.value.json}"/></g:if> 
			<g:if test="${action.value.method}"><g:set var="method" value="${action.value.method}"/></g:if> 
			<g:if test="${action.value.description}"><g:set var="description" value="${action.value.description}"/></g:if>
			<g:if test="${action.value.receives}"><g:set var="receives" value="${action.value.receives}"/></g:if> 
			<g:if test="${action.value.returns}"><g:set var="returns" value="${action.value.returns}"/></g:if> 
			<g:if test="${action.value.errorcodes}"><g:set var="errors" value="${action.value.errorcodes}"/></g:if> 
			<g:if test="${action.value.links}"><g:set var="links" value="${action.value.links}"/></g:if> 

			</g:each>
			<br>
			<div style="background-color: #f7f7f7;padding:5px;">
				<div style="display: table;border: 1px solid #c5c5c5;">
				 <div style="display: table-row; background-color:#b4b8be;color:#fff;">
					   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>${action}</b></span>
					   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>${path}</b></span>
					   <span style="display: table-cell;width: 400px;padding-left: 10px;border: 1px #d7ad7b;"><b>&nbsp;</b></span>
				 </div>
				</div>
				${description}
				<br><br>
				
				<g:if test="${json}">
				<b>RETURN EXAMPLE</b>
				<div>${json}</div>
				<br>
				</g:if>

				<g:if test="${links?.size()>0}">
				<div style="display: table;border: 1px solid #c5c5c5;">
					<div style="display: table-row; background-color:#b4b8be;color:#fff;">
					   <span style="display: table-cell;width: 800px;padding-left: 10px;border: 1px #d7ad7b;"><b>Links</b></span>
					</div>
					<g:each in="${links}" var="link">
						<div style="display: table-row; background-color:#fff;">
						   <span style="display: table-cell;width: 800px;padding-left: 10px;border: 1px #d7ad7b;">${link}</span>
					    </div>
					 </g:each>
				 </div>
				</g:if>
				
				<b>Receives</b>
				<div style="display: table;border: 1px solid #c5c5c5;">
					<div style="display: table-row; background-color:#b4b8be;color:#fff;">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
					   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Required</b></span>
					    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
					</div>
					<g:each in="${receives}" var="value">
						<div style="display: table-row;background-color:#fff;">
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.paramType}</span>
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.name}</span>
						   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">${value.description}</span>
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.required}</span>
						    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
					    </div>
					 </g:each>
	
				 </div>
				 
				<g:if test="${returns?.size()>0}">
				<b>Returns</b>
				<div style="display: table;border: 1px solid #c5c5c5;">
					<div style="display: table-row; background-color:#b4b8be;color:#fff;">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
					   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Required</b></span>
					    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
					</div>
					<g:each in="${returns}" var="rturn">
						<div style="display: table-row; background-color:#fff;">
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.paramType}</span>
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.name}</span>
						   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.description}</span>
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.required}</span>
						    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
					    </div>
					 </g:each>
				 </div>
				 </g:if>
				
				<g:if test="${errors?.size()>0}">
				<b>Errors</b>
				<div style="display: table;border: 1px solid #c5c5c5;">
					<div style="display: table-row; background-color:#b4b8be;color:#fff;">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Code</b></span>
					   <span style="display: table-cell;width: 700px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
					</div>
					<g:each in="${errors}" var="error">
						<div style="display: table-row; background-color:#fff;">
						   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${error.code}</span>
						   <span style="display: table-cell;width: 700px;padding-left: 10px;border: 1px #d7ad7b;">${error.description}</span>
					    </div>
					 </g:each>
				 </div>
				</g:if>
			</div>

		</g:each>
		
		</div>
	</body>
</html>