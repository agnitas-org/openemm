<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="fileName" type="java.lang.String"--%>
<%--@elvariable id="tmpFileName" type="java.lang.String"--%>
<%--@elvariable id="success" type="java.lang.Boolean"--%>

<div class="tile">
	<div class="tile-header">
		<h2 class="headline"><mvc:message code="report.data"/></h2>
	</div>

	<div class="tile-content tile-content-forms">
		<div class="row">
			<c:if test="${success}">

				<div class="well block">
					<mvc:message code="report.evaluation.finished"/>
				</div>

				<div class="vspacer-10"></div>


				<div class="well block align-center">
					<c:url var="download" value="/statistics/report/download.action">
						<c:param name="fileName" value="${fileName}"/>
						<c:param name="tmpFileName" value="${tmpFileName}"/>
					</c:url>

					<a href="${download}" class="btn btn-regular btn-success" data-prevent-load="">
						<i class="icon icon-download"></i>
						<span class="text">
						<mvc:message code="button.Download"/> ${fileName}
					</span>
					</a>
				</div>
			</c:if>

			<c:if test="${not success}">
				<div class="well block">
					<mvc:message code="error.report.evaluation"/>
					</br>
					<mvc:message code="error.file.missingOrEmpty"/>
				</div>
			</c:if>
		</div>
	</div>
</div>
