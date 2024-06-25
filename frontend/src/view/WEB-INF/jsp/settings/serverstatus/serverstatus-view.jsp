<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/serverstatus/view.action" method="post" modelAttribute="serverStatusForm" data-form="resource">
	<div style="line-height:1.8">
		<div class="col-sm-8">
			<div class="tile">
				<%@ include file="fragments/serverstatus-info-general.jspf" %>
			</div>
			<div class="tile">
				<%@ include file="fragments/serverstatus-settings.jspf" %>
			</div>
			<div class="tile">
				<%@ include file="fragments/serverstatus-config.jspf" %>
			</div>
		</div>
		<div class="col-sm-4">
			<div class="tile">
				<%@ include file="fragments/serverstatus-info-status.jspf" %>
			</div>
			<div class="tile">
				<%@ include file="fragments/serverstatus-info-db.jspf" %>
			</div>
		</div>
	</div>
</mvc:form>


<%@ include file="fragments/serverstatus-license-upload.jspf" %>
