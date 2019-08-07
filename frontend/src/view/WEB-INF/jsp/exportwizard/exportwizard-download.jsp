<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ExportWizardAction"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<emm:Permission token="wizard.export"/>

<html>
	<logic:lessThan name="exportWizardForm" property="dbExportStatus" value="1000" scope="session">
		<meta http-equiv="Page-Exit" content="RevealTrans(Duration=1,Transition=1)">
	</logic:lessThan>

	<head>
		<meta charset="UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="expires" content="0">

		<tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
	</head>

	<body <logic:lessThan name="exportWizardForm" property="dbExportStatus" value="1000" scope="session">onLoad="window.setTimeout('window.location.reload()',1500)"</logic:lessThan> style="background-image:none;background-color:transparent">

		<div class="well block">
			<p><b><bean:message key="export.data"/></b></p>
			<p>
				<bean:write name="exportWizardForm" property="linesOK"/>
				<bean:message key="Recipients"/>
			</p>
		</div>

		<div class="vspacer-10"></div>
		<div class="well block">
			<bean:message key="export.finished"/>
		</div>

		<%--@elvariable id="exportWizardForm" type="org.agnitas.web.forms.ExportWizardForm"--%>
		<c:if test="${not empty exportWizardForm.downloadName}">
			<div class="vspacer-10"></div>

			<div class="well block align-center">
				<c:set var="downloadLink">
					<html:rewrite page='<%= "/exportwizard.do?action=" + ExportWizardAction.ACTION_DOWNLOAD %>'/>
				</c:set>
				<agn:agnLink href="#" styleClass="btn btn-regular btn-success" onclick="window.location.href = '${downloadLink}'; AGN.Lib.Loader.hide(); return false;"  data-prevent-load="">
					<i class="icon icon-download"></i>
					<span class="text">
						<bean:message key="button.Download"/> ${exportWizardForm.downloadName}
					</span>
				</agn:agnLink>
			</div>
		</c:if>
	</body>
</html>
