<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="iframeUrl" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.logon.forms.LogonForm"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<s:message var="title" code="logon.title" />

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title></title>
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css" integrity="sha384-oS3vJWv+0UjzBfQzYUhtDYW+Pj2yciDJxpsK1OYPAYjqT085Qq/1cq5FLXAZQ7Ay" crossorigin="anonymous">

<tiles:insert page="/WEB-INF/jsp/assets.jsp" />

<style>
/*General START*/
	body {
		margin: 0;
		font-size: 62.5%;
		font-family: Arial, sans-serif;
		background: #ffffff;
		color: #777777;
		overflow-x: hidden;
	}

	p {
		margin: 0;
		font-size: 1.8em;
	}

	h1 {
		margin: 0;
		font-size: 4.2em;
	}

	h2 {
		margin: 0;
		font-size: 3.6em;
	}

	h3 {
		margin: 0;
		font-size: 2.8em;
	}
	
	div {
		text-align: left;
	}

	.main {
		display: flex;
		flex-direction: column;
		text-align: center;
	}

	.flexcolumn {
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.flexrow {
		display: flex;
		flex-direction: row;
		justify-content: center;
		align-items: flex-end;
	}

	.flexitem {
		flex: 1;	
	}

	.wrapper {
		width: 100%;
		max-width: 100vw;
		flex: auto;
	}

	.wrapper--content {
		max-width: 115em;
		padding: 0 3em;
	}

	.blue {
		background: #0071b9;
		color: #ffffff;
	}

	.white {
		background: #ffffff;
		color: #0071b9;
	}
	
	.divider--down {
		width: 0;
		height: 0;
		border-bottom: 23em solid #0071B9;
		border-right: 100vw solid transparent;
		overflow: hidden;
	}
	
	.divider--up {
		width: 0;
		height: 0;
		border-bottom: 23em solid transparent;
		border-right: 100vw solid #0071B9;
		overflow: hidden;
	}
/*General END*/

/*Header START*/
	.header {
		width: 100%;
		align-items: center;
	}
	
	.header--wrapper {
		align-items: center;
		padding-top: 3em;
		padding-bottom: 3em;
	}
	
	.header__logo {
		
	}
	
	.header__logo > img{
		max-width: 27.9em;
	}
	
	.header__link {
		text-align: end;
	}
	
	.header__link > :first-child {
		color: #0071b9;
	}
/*Header END*/

/*Version START*/
	.version {
		width: 100%;
	}
	
	.version--wrapper {
		background-image: url("/assets/core/images/logonPageOffline/Version.jpg");
		background-position: 100% 100%;
		background-repeat: no-repeat;
		background-size: cover;
		align-items: center;
		padding-top: 3em;
	}
	
	.version__text {
		color: #FFFFFF;
		padding: 2em 50% 2em 0;
	}
	
	.version__text--headline {
		color: #FFFFFF;
		padding-bottom: 1em;
	}
	
	.version__text--headline > h1{
		color: #FFFFFF;
	}
	
	.version__text--content {
			
	}
/*Version END*/

/*Help START*/
	.help {
		column-gap: 5em;
		align-items: flex-start;
		width: 100%;
	}
	
	.help--wrapper {
		align-items: center;
		padding-bottom: 3em;
	}
	
	.help__head {
		padding-bottom: 2em;
	}
	
	.help__head > :first-child {
		color: #ffffff;
	}
	
	.help__content {
		
	}
	
	.help__content--head {
		padding-bottom: 1em;
	}
	
	.help__content--head > :first-child {
		color: #ffffff;
	}
	
	.help__content--image {
		padding-bottom: 1em;
	}
	
	.help__content--image > :first-child {
		height: 100%;
		width: 100%;
		min-height: 19em;
		min-width: 33em;
	}
	
	.help__content--subline {
			
	}	
	
	.help__content--subline > :first-child {
		color: #ffffff;
	}
/*Help END*/

/*Footer START*/
	.footer {
		width: 100%;
	}
	
	.footer > p {
		flex: 1;
	}
	
	.footer--wrapper {
		align-items: center;
		background: #222222;
		padding: 1em 0;
	}
/*Footer END*/
</style>
</head>

<body>
	<mvc:form servletRelativeAction="/logonoffline.action" method="post" modelAttribute="form" data-form="resource">
		<div class="main">
			<!--Structure: Wrapper - Content - Contentblocks - Content for Contentblocks-->
			
			<!--Header START-->
			<div class="header--wrapper wrapper flexcolumn flexitem">
				<div class="header wrapper--content flexrow flexitem">
					<div class="header__logo flexitem">
						<img src="/assets/core/images/logonPageOffline/AgnitasLogo.jpg"/>
					</div>
					<div class="header__link flexitem">
						<h2>www.agnitas.de</h2>
					</div>
				</div>
			</div>
			<!--Header END-->
			
			<!--Version START-->
			<div class="version--wrapper wrapper flexcolumn flexitem">
				<div class="version wrapper--content flexrow flexitem">
					<div class="version__text flexcolumn flexitem">
						<div class="version__text--headline flexitem">
							<h1><mvc:message code="logon.offline.versionHeadline" /></h1>
						</div>
						<div class="version__text--content flexitem">
							<p>
								<mvc:message code="logon.offline.versionText" />
							</p>
						</div>
					</div>
				</div>
				<div class="divider--down flexitem">
					<!--Divider-->
				</div>
			</div>
			<!--Version END-->
			
			<!--Help START-->
			<div class="help--wrapper wrapper flexcolumn flexitem blue">
				<div class="help wrapper--content flexrow flexitem">
					<div class="help__head flexitem">
						<h2><mvc:message code="logon.offline.helpHeadline" /></h2>
					</div>
				</div>
				<div class="help wrapper--content flexrow flexitem">
					<div class="help__content flexcolumn flexitem">
						<div class="help__content--head flexitem">
							<h3 class="blue"><mvc:message code="logon.offline.helpContent.headline1" /></h3>
						</div>
						<div class="help__content--image flexitem">
							<img src="/assets/core/images/logonPageOffline/Support.jpg" />
						</div>
						<div class="help__content--subline flexitem">
							<h3>support@agnitas.de</h3>
						</div>
					</div>
					<div class="help__content flexcolumn flexitem">
						<div class="help__content--head flexitem">
							<h3 class="blue"><mvc:message code="logon.offline.helpContent.headline2" /></h3>
						</div>
						<div class="help__content--image flexitem">
							<img src="/assets/core/images/logonPageOffline/Tutorials.jpg" />
						</div>
						<div class="help__content--subline flexitem">
							<h3>www.agnitas.de/services/tutorials/</h3>
						</div>
					</div>
				</div>
			</div>
			<!--Help END-->
			
			<!--Footer START-->
			<div class="footer--wrapper wrapper flexcolumn flexitem">
				<div class="footer wrapper--content flexrow flexitem">
					<p>Copyright <span id="displayDate">#</span> by AGNITAS AG</p>
				</div>
			</div>
			<!--Footer END-->
			
		</div>
		<script>
			var date = new Date();
			date.setDate(date.getDate());
			
			document.getElementById('displayDate').innerHTML = date.getFullYear();
		</script>
	</mvc:form>
</body>
</html>
