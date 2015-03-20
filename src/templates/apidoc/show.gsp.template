<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Api Docs</title>
		
	    <g:javascript library="jquery" plugin="jquery"/>

		<style media="screen" type="text/css">
		.controllerList{
			margin-left: auto;
			margin-right: auto;
			width: 800px;
			font-weight: normal;
		}
		
		.methodList{
			display:inline-box;
		}
		
		.controllerName{
			float:left;
			width: 600px;
			line-height:20px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: bold;
			color:black;
		}
		
		.controllerName a{
				text-decoration:none;
				color: #848484 !important;
		}
		.controllerName a:hover{
			color: #000 !important;
		}
		
		.functions{
			text-align:right;
			float:right;
			width: 200px;
			line-height:20px;
		    font-size:12px;
		    height:20px;
			font-family: "Droid Sans", sans-serif;
			color:grey;
		}
		
		.method{
			position:relative;
			z-index:999;
			-webkit-border-top-left-radius: 5px !important;
			-webkit-border-bottom-left-radius: 5px !important;
			border-top-left-radius: 5px !important;
			border-bottom-left-radius: 5px !important;
			-moz-border-radius-topleft: 7px !important;
			-moz-border-radius-bottomleft: 5px !important;
			float:left;
			width: 75px;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: bold;
			color:white;
			border-top: 1px solid white;
			border-bottom: 1px solid white;
		}
		
		.method a{
				text-decoration:none;
				color: #fff !important;
		}
		.method a:hover{
			color: #000 !important;
		}
		
		.path{
			position:relative;
			z-index:999;
			-webkit-border-top-right-radius: 5px !important;
			-webkit-border-bottom-right-radius: 5px !important;
			border-top-right-radius: 5px !important;
			border-bottom-right-radius: 5px !important;
			-moz-border-radius-topright: 5px !important;
			-moz-border-radius-bottomright: 5px !important;
			float:right;
			width: 707px;
			line-height:20px;
		    font-size:15px;
		    height:20px;
			font-family: "Droid Sans", sans-serif;
			color:black;
			border-top: 1px solid white;
			border-bottom: 1px solid white;
		}
			
		.methodSeparator{
			z-index:999;
			float:left;
			background-color:white;
			height:20px;
			width:2px;
		}
			
		.getDark{
			background-color: #66cc33 !important;
		}
		.getLight{
			background-color: #b3e19c !important;
		}
		.getDesc{
			text-align:right;
			float:right;
			padding-right:10px;
			color: #4e9d27;
		}
		
		.putDark{
			background-color: #ff9900 !important;
		}
		.putLight{
			background-color: #fedaa3 !important;
		}
		.putDesc{
			text-align:right;
			float:right;
			padding-right:10px;
			color: #cf7c00;
		}
		
		.postDark{
			background-color: #6699ff !important;
		}
		.postLight{
			background-color: #b1c8fe !important;
		}
		.postDesc{
			text-align:right;
			float:right;
			padding-right:10px;
			color: #4c73bf
		}
		
		.deleteDark{
			background-color: #ff3300 !important;
		}
		.deleteLight{
			background-color: #fdbfb1 !important;
		}
		.deleteDesc{
			text-align:right;
			float:right;
			padding-right:10px;
			color: #c52700;
		}
		
		.infoBox{
			z-index:1;
			position:relative;top:-3px;
			-webkit-border-bottom-left-radius: 5px !important;
			-webkit-border-bottom-right-radius: 5px !important;
			border-bottom-left-radius: 5px !important;
			border-bottom-right-radius: 5px !important;
			-moz-border-radius-bottomleft: 5px !important;
			-moz-border-radius-bottomright: 5px !important;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: bold;
			background-color:#e0e0e0;
			width:782px;
			clear: both;
			display:inline-box;
			padding-top:3px;
		}

		.table{
			display: table;
		}
		.getTableContrast{
			border: 1px solid #66cc33;
			background-color:#fedaa3;
			width:730px;
			color:#000;
			line-height:20px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.getHeadContrast{
			background-color:#66cc33;
			color:#fff;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.getRowContrast{
			background-color:#fedaa3;
			color:#000;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		
		.putTableContrast{
			border: 1px solid #ff9900;
			background-color:#fedaa3;
			width:730px;
			color:#000;
			line-height:20px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.putHeadContrast{
			background-color:#ff9900;
			color:#fff;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.putRowContrast{
			background-color:#fedaa3;
			color:#000;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		
		.postTableContrast{
			border: 1px solid #6699ff;
			background-color:#fedaa3;
			width:730px;
			color:#000;
			line-height:20px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.postHeadContrast{
			background-color:#6699ff;
			color:#fff;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.postRowContrast{
			background-color:#fedaa3;
			color:#000;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		
		.deleteTableContrast{
			border: 1px solid #ff3300;
			background-color:#fedaa3;
			width:730px;
			color:#000;
			line-height:20px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.deleteHeadContrast{
			background-color:#ff3300;
			color:#fff;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		.deleteRowContrast{
			background-color:#fedaa3;
			color:#000;
			line-height:20px;
			padding-left:18px;
			font-size:15px;
			font-family: "Droid Sans", sans-serif;
			font-weight: normal;
		}
		</style>
	</head>

	<body>

		<div id="show-apidocs" class="content scaffold-show" role="main">
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
		</div>
		
		<g:each in="${apiList}" var="controller">
			<div class="controllerList">
				<div class="controllerName"><a href='#' onClick="toggleControllerList(this);">${controller.key.toUpperCase()}</a></div>
				<div class="functions"><a href='#' onClick="toggleMethodList(this);">show/hide</a></div>
			
			<g:set var="temp" value="${controller.value.collect()}"/>
			<g:set var="actionCount" value="${temp.size()}"/>

			<g:each in="${controller.value}" var="act">
				<g:each in="${act}" var="action">
					<g:set var="actionName" value="${action.key}"/>
					<g:each in="${action.value}" var="vers">
						<g:set var="version" value="${vers.key}"/>
						<g:if test="${vers.value.method}"><g:set var="methods" value="${vers.value.method}"/></g:if>
						<g:if test="${vers.value.method}"><g:set var="method" value="${vers.value.method.replace('[','').replace(']','').split(',').findAll{ it =~ /(GET|PUT|POST|DELETE|PATCH|TRACE)/ }[0]?.toLowerCase()}"/></g:if>
						<g:if test="${vers.value.path}">
							<g:if test="${method!='put'}"><g:set var="path" value="${vers.value.path}/ID"/></g:if>
							<g:else><g:set var="path" value="${vers.value.path}"/></g:else>
						</g:if>
						<g:if test="${vers.value.json}"><g:set var="json" value="${vers.value.json}"/></g:if>
						<g:if test="${vers.value.description}"><g:set var="description" value="${vers.value.description}"/></g:if>
						<g:if test="${vers.value.receives}"><g:set var="receives" value="${vers.value.receives}"/></g:if> 
						<g:if test="${vers.value.returns}"><g:set var="returns" value="${vers.value.returns}"/></g:if> 
						<g:if test="${vers.value.errorcodes}"><g:set var="errors" value="${vers.value.errorcodes}"/></g:if> 

						<div class="methodList" style="display:none;">
						
							<div class="method ${method}Dark"><a href="#" onClick="toggleInfoBox(this);">${method?.toUpperCase()}</a></div>
							<div class="path ${method}Light">${path}<img src="/images/pix.gif" class="methodSeparator"/><div class="${method}Desc"">${description}</div></div>
							<div class="infoBox ${method}Light" style="display:none;">
								<br/>
								JSON Example <div class="table ${method}TableContrast"><%=json%></div>
								<br/>
								<g:if test="${links?.size()>0}">
								<div class="table ${method}TableContrast">
									<div class="${method}HeadContrast">
									   <span style="display: table-cell;width: 740px;padding-left: 10px;border: 1px #d7ad7b;"><b>Links</b></span>
									</div>
									<g:each in="${links}" var="link">
										<div class="${method}RowContrast">
										   <span style="display: table-cell;width: 740px;padding-left: 10px;border: 1px #d7ad7b;">${link}</span>
									    </div>
									 </g:each>
								 </div>
								</g:if>
								
								Receives
								<div class="table ${method}TableContrast">
									<div class="${method}HeadContrast">
									   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
									   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
									   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
									    <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
									</div>
									<g:each in="${receives}" var="value">
										<div class="${method}RowContrast">
										   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;">${(['PKEY','FKEY','INDEX'].contains(value.paramType.toString()))?'ID':value.paramType}</span>
										   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;">${value.name}</span>
										   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;">${value.description}</span>
										    <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
									    </div>
									 </g:each>
					
								 </div>
								 
								<g:if test="${returns?.size()>0}">
								Returns
								<div class="table ${method}TableContrast">
									<div class="${method}HeadContrast">
									   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;"><b>Type</b></span>
									   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;"><b>Name</b></span>
									   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
									    <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;"><b>Params</b></span>
									</div>
									
									<g:each status="i" in="${returns}" var="rturn">
										<div class="${method}RowContrast">
										   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;">${(['PKEY','FKEY','INDEX'].contains(rturn.paramType.toString()))?'ID':rturn.paramType}</span>
										   <span style="display: table-cell;width: 150px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.name}</span>
										   <span style="display: table-cell;width: 200px;padding-left: 10px;border: 1px #d7ad7b;">${rturn.description}</span>
										    <span style="display: table-cell;width:200px;padding-left: 10px;border: 1px #d7ad7b;">[]</span>
									    </div>
									 </g:each>
								 </div>
								 </g:if>
								
								<g:if test="${errors?.size()>0}">
								Errors
								<div class="table ${method}TableContrast">
									<div class="${method}HeadContrast">
									   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;"><b>Code</b></span>
									   <span style="display: table-cell;width: 630px;padding-left: 10px;border: 1px #d7ad7b;"><b>Description</b></span>
									</div>
									<g:each in="${errors}" var="error">
										<div class="${method}RowContrast">
										   <span style="display: table-cell;width: 100px;padding-left: 10px;border: 1px #d7ad7b;">${error.code}</span>
										   <span style="display: table-cell;width: 630px;padding-left: 10px;border: 1px #d7ad7b;">${error.description}</span>
									    </div>
									 </g:each>
								 </div>
								 </br>
								</g:if>
							</div>
						</div>
				
					</g:each>
				</g:each>
			</g:each>
			
			</div>
		</g:each>
		
		<script>
		function toggleControllerList(elem) {
			if($(elem).parent('.controllerName').parent('.controllerList').find('.methodList').is(':visible')){
				$(elem).parent('.controllerName').parent('.controllerList').find('.methodList').fadeOut()
			} else {
				$(elem).parent('.controllerName').parent('.controllerList').find('.methodList').fadeIn()
			}
		}
		
		function toggleMethodList(elem) {
			if($(elem).parent('.functions').parent('.controllerList').find('.methodList').is(':visible')){
				$(elem).parent('.functions').parent('.controllerList').find('.methodList').fadeOut()
			} else {
				$(elem).parent('.functions').parent('.controllerList').find('.methodList').fadeIn()
			}
		}

		function toggleInfoBox(elem) {
			if($(elem).parent('.method').parent('.methodList').find('.infoBox').is(':visible')){
				$(elem).parent('.method').parent('.methodList').find('.infoBox').fadeOut()
			} else {
				$(elem).parent('.method').parent('.methodList').find('.infoBox').fadeIn()
			}
		}
		</script>
	</body>
</html>
