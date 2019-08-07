<%@ page language="java" import="com.agnitas.web.ComMailingBaseAction" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="editTargetForm" type="com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm"--%>

<c:set var="ACTION_VIEW_MAILING" value="<%= ComMailingBaseAction.ACTION_VIEW %>" scope="page" />

<agn:agnForm action="/targetQB" id="targetForm" data-form="resource" >
	<html:hidden property="targetID" />
	<html:hidden property="workflowForwardParams" />
	<html:hidden property="workflowId" />

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="Mailings" />
			</h2>
		</div>
		<div class="tile-content tile-content-forms">
			<c:if test="${empty editTargetForm.usedInMailings}">
				<div class="empty-list well">
					<i class="icon icon-info-circle"></i><strong><bean:message key="default.nomatches"/></strong>
				</div>
			</c:if>
			<c:if test="${not empty editTargetForm.usedInMailings}">
				<ul class="list-group">
					<c:forEach var="mailing" items="${editTargetForm.usedInMailings}">
						<li class="list-group-item">
							<emm:ShowByPermission token="mailing.show">
								<c:url var="mailingLink" value="/mailingbase.do">
									<c:param name="action" value="${ACTION_VIEW_MAILING}"/>
									<c:param name="mailingID" value="${mailing.mailingID}"/>
								</c:url>
								<a href="${mailingLink}"> ${mailing.shortname} </a>
							</emm:ShowByPermission>
							<emm:HideByPermission token="mailing.show">
								${mailing.shortname}
							</emm:HideByPermission>
						</li>
					</c:forEach>
				</ul>
			</c:if>
		</div>
	</div>
</agn:agnForm>
