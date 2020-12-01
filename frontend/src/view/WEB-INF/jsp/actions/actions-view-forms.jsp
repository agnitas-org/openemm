<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="emmActionForm" type="com.agnitas.web.forms.ComEmmActionForm"--%>
<agn:agnForm action="/action" id="emmActionForm" data-form="resource">
	<html:hidden property="actionID"/>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><bean:message key="workflow.panel.forms"/></h2>
		</div>
		<div class="tile-content tile-content-forms">
			<c:if test="${empty emmActionForm.usedByFormsNames}">
				<div class="empty-list well">
					<i class="icon icon-info-circle"></i><strong><bean:message key="default.nomatches"/></strong>
				</div>
			</c:if>
			<c:if test="${not empty emmActionForm.usedByFormsNames}">
				<ul class="list-group">
					<c:forEach var="userFormTuple" items="${emmActionForm.usedByFormsNames}">
						<li class="list-group-item">
							<emm:ShowByPermission token="forms.show">
								<c:url var="formLink" value="/webform/${userFormTuple.first}/view.action"/>
								<a href="${formLink}"> ${userFormTuple.second} </a>
							</emm:ShowByPermission>

							<emm:HideByPermission token="forms.show">
								${userFormTuple.second}
							</emm:HideByPermission>
						</li>
					</c:forEach>
				</ul>
			</c:if>
		</div>

		<c:if test="${not empty emmActionForm.usedByImportNames}">
			<div class="tile-header">
				<h2 class="headline"><bean:message key="import.ImportProfile"/></h2>
			</div>
			<div class="tile-content tile-content-forms">
				<ul class="list-group">
					<c:forEach var="importTuple" items="${emmActionForm.usedByImportNames}">
						<li class="list-group-item">
							<c:url var="importLink" value="/importprofile.do?action=2&profileId=${importTuple.first}" />
							<a href="${importLink}"> ${importTuple.second} </a>
						</li>
					</c:forEach>
				</ul>
			</div>
		</c:if>
	</div>
</agn:agnForm>
