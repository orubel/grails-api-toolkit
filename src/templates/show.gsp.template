<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<title><g:message code="default.show.label" args="[entityName]" /></title>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
		<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
		<script>
			$(function() {
			$( "#accordion" ).accordion({
				active: false,
	            autoheight: false,
	            heightStyle: "content",
	            collapsible: true,
	            alwaysOpen: false
			});
			});
		</script>
	</head>
	<body>

		<div id="show-apidocs" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
		</div>
		
		<div style="margin-left: auto; margin-right: auto;width: 800px;">
		<div id="accordion">
		<g:each in="${api}" var="a">

			<h3>${a.parent}</h3>

			<div style="display: table;border: 1px solid black;">

			<g:each in="${a.api}" var="apiList">
			<hr width=100%>
			 <div>
			 	<span style="display: table-row; background-color:#769dcd;color:#fff;">
				 	<span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Method</b></span>
				   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Path</b></span>
				   <span style="display: table-cell;width: 400px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
			   </span>
			   <span style="display: table-row; background-color:#9adb9e">
				   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;">${apiList.method}</span>
				   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;">${apiList.path}</span>
				   <span style="display: table-cell;width: 400px;padding-left: 10px;border: 1px #d7ad7b;">${apiList.description}</span>
			 	</span>
			 </div>
		
			<span style="display: table-row; background-color:#769dcd;color:#fff;">
				<span style="display: table-cell;width: 800px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
			</span>
			<div>
				<div style="display: table-row; background-color:#b4b8be;color:#fff;">
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
				   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Required</b></span>
				    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
				</div>
				<g:each in="${apiList.values}" var="value">
					<div style="display: table-row">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.type}</span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.name}</span>
					   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">${value.description}</span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${value.required}</span>
					    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
				    </div>
				 </g:each>
			 </div>
		
			<span style="display: table-row; background-color:#769dcd;color:#fff;">
				<span style="display: table-cell;width: 800px;padding-left: 10px;border: 1px #d7ad7b;"><b>Returns</b></span>
			</span>
			<div>
				<div style="display: table-row; background-color:#b4b8be;color:#fff;">
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
				   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Required</b></span>
				    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
				</div>
				<g:each in="${apiList.returns}" var="rturn">
					<div style="display: table-row">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.type}</span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.name}</span>
					   <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.description}</span>
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.required}</span>
					    <span style="display: table-cell;width: 250px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
				    </div>
				 </g:each>
			 </div>
			 
			<span style="display: table-row; background-color:#769dcd;color:#fff;">
				<span style="display: table-cell;width: 800px;padding-left: 10px;border: 1px #d7ad7b;"><b>Errors</b></span>
			</span>
			<div>
				<div style="display: table-row; background-color:#b4b8be;color:#fff;">
				   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Code</b></span>
				   <span style="display: table-cell;width: 700px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
				</div>
				<g:each in="${apiList.errors}" var="error">
					<div style="display: table-row">
					   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${error.code}</span>
					   <span style="display: table-cell;width: 700px;padding-left: 10px;border: 1px #d7ad7b;">${error.description}</span>
				    </div>
				 </g:each>
			 </div>
			
			
			</g:each>
			</div>
			
		</g:each>
		</div>
		</div>

	</body>
</html>
