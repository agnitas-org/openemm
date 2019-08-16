<%@page import="org.agnitas.util.AgnUtils"%>
<%@ page language="java" import="org.agnitas.web.*, com.agnitas.web.*, org.agnitas.target.*" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="editTargetForm" type="com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm"--%>

<c:set var="ACTION_SAVE" 						value="<%= ComTargetAction.ACTION_SAVE %>" 						scope="page" />
<c:set var="ACTION_CLONE" 						value="<%= ComTargetAction.ACTION_CLONE %>" 					scope="page" />
<c:set var="ACTION_BULK_DELETE" 				value="<%= ComTargetAction.ACTION_CONFIRM_DELETE %>" 			scope="page" />
<c:set var="ACTION_VIEW" 						value="<%= ComTargetAction.ACTION_VIEW %>" 						scope="page" />
<c:set var="ACTION_CREATE_ML" 					value="<%= ComTargetAction.ACTION_CREATE_ML %>" 				scope="page" />
<c:set var="ACTION_DELETE_RECIPIENTS_CONFIRM" 	value="<%= ComTargetAction.ACTION_DELETE_RECIPIENTS_CONFIRM %>" scope="page" />
<c:set var="ACTION_LOCK" 						value="<%= ComTargetAction.ACTION_LOCK_TARGET_GROUP %>" 		scope="page" />
<c:set var="ACTION_UNLOCK" 						value="<%= ComTargetAction.ACTION_UNLOCK_TARGET_GROUP %>" 		scope="page" />

<c:set var="COLUMN_TYPE_DATE" value="<%= TargetForm.COLUMN_TYPE_DATE %>" scope="page" />
<c:set var="COLUMN_TYPE_NUMERIC" value="<%= TargetForm.COLUMN_TYPE_NUMERIC %>" scope="page" />
<c:set var="COLUMN_TYPE_INTERVAL_MAILING" value="<%= TargetForm.COLUMN_TYPE_INTERVAL_MAILING %>" scope="page" />
<c:set var="COLUMN_TYPE_STRING" value="<%= TargetForm.COLUMN_TYPE_STRING %>" scope="page" />

<c:set var="OPERATOR_IS" value="<%= TargetNode.OPERATOR_IS.getOperatorCode() %>" scope="page" />
<c:set var="OPERATOR_MOD" value="<%= TargetNode.OPERATOR_MOD.getOperatorCode() %>" scope="page" />

<c:if test="${editTargetForm.locked}">
	<c:set var="TARGET_LOCKED" value="true" scope="request" />
	<%-- "request" required, because this flag is used in included JSP --%>
</c:if>
<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>" />

<%-- Determine active editor tab --%>
<c:choose>
	<c:when test="${editTargetForm.format == 'qb'}">
		<c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="active" scope="page" />
		<c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
		<c:set var="QB_EDITOR_DIV_SHOW_STATE" value="data-tab-show='true'" scope="page" />
		<c:set var="EQL_EDITOR_DIV_SHOW_STATE" value="data-tab-hide='true'" scope="page" />
	</c:when>
	<c:when test="${editTargetForm.format == 'eql'}">
		<c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
		<c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="active" scope="page" />
		<c:set var="QB_EDITOR_DIV_SHOW_STATE" value="data-tab-hide='true'" scope="page" />
		<c:set var="EQL_EDITOR_DIV_SHOW_STATE" value="data-tab-show='true'" scope="page" />
	</c:when>
	<c:otherwise>
		<%-- This should never happen... --%>
		<c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
		<c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
	</c:otherwise>
</c:choose>

<agn:agnForm action="/targetQB" id="targetForm" data-form="resource" >
	<html:hidden property="targetID" />
	<html:hidden property="format" />
	<html:hidden property="workflowForwardParams" />
	<html:hidden property="workflowId" />
	<html:hidden property="locked" />


	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="target.Edit" />
			</h2>
		</div>
		<div class="tile-content tile-content-forms">
			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="shortname">
						<bean:message key="Name"/>
					</label>
				</div>
				<div class="col-sm-8">
					<html:text styleId="shortname" styleClass="form-control" property="shortname" maxlength="99" size="42" readonly="${editTargetForm.locked}"/>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="description">
						<bean:message key="default.description" />
					</label>
				</div>
				<div class="col-sm-8">
					<html:textarea styleId="description" styleClass="form-control" property="description" rows="5" cols="32" readonly="${editTargetForm.locked}"/>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-4" for="admin_and_test_delivery">
					<label class="control-label"><bean:message key="target.adminAndTestDelivery"/></label>
				</div>
				<div class="col-sm-8">
					<label class="toggle">
						<html:checkbox styleId="admin_and_test_delivery" property="useForAdminAndTestDelivery"
									   disabled="${TARGET_LOCKED}"/>
						<div class="toggle-control"></div>
					</label>
				</div>
			</div>
		</div>
	</div>
	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="target.TargetDefinition" />
			</h2>
			<ul class="tile-header-nav">
				<li class="${QB_EDITOR_TAB_ACTIVE_CLASS}">
					<a href="#" data-toggle-tab="#tab-targetgroupQueryBuilderEditor" data-form-set="method: 'viewQB'" data-form-submit><bean:message key="default.basic" /></a>
				</li>
				<emm:ShowByPermission token="targets.eql.edit">
					<li class="${EQL_EDITOR_TAB_ACTIVE_CLASS}">
						<a href="#" data-toggle-tab="#tab-targetgroupEqlEditor" data-form-set="method: 'viewEQL'" data-form-submit>
							<bean:message key="default.advanced" />
						</a>
					</li>
				</emm:ShowByPermission>
			</ul>
		</div>
		<div class="tile-content tile-content-forms">
			<c:if test="${editTargetForm.format == 'qb'}">
				<div id="tab-targetgroupQueryBuilderEditor" ${QB_EDITOR_DIV_SHOW_STATE}>
					<div class="row">
						<div class="col-md-12">
							<div class="form-group">
								<div class="col-md-12">
									<label class="control-label"></label>
								</div>
								<div class="col-md-12">
									<div id="targetgroup-querybuilder">
										<html:hidden property="queryBuilderRules" styleId="queryBuilderRules"/>
										<html:hidden property="queryBuilderFilters" styleId="queryBuilderFilters"/>
										<input type="hidden" value="${pageContext.session.id}" id="jSessionId">
										<input type="hidden" value="${helplanguage}" id="helpLanguage">
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:if>
			<c:if test="${editTargetForm.format == 'eql'}">				
				<div id="tab-targetgroupEqlEditor" ${EQL_EDITOR_DIV_SHOW_STATE}>
					<div class="row">
						<div class="col-md-12">
							<div class="form-group">
								<logic:messagesPresent property="eqlErrors">
									<ul>
										<html:messages id="msg" property="eqlErrors">
											<div class="tile">
												<li class="tile-notification tile-notification-alert">${msg}</li>
											</div>
										</html:messages>
									</ul>
								</logic:messagesPresent>
								<html:textarea styleId="eql" property="eql" rows="14" cols="${TEXTAREA_WIDTH}" styleClass="form-control js-editor-eql" readonly="${editTargetForm.locked}" />
							</div>
						</div>
					</div>
				</div>
			</c:if>
		</div>
	</div>

	<emm:ShowByPermission token="targets.change">
		<c:if test="${not empty editTargetForm.targetID and editTargetForm.targetID != 0 and not editTargetForm.locked}">
			<div class="tile">
				<div class="tile-header">
					<div class="headline">
						<html:select property="mailinglistId" size="1" styleClass="js-select">
							<option value="0"><bean:message key="statistic.All_Mailinglists" /></option>
							<logic:iterate id="mailinglist" name="editTargetForm" property="mailinglists" scope="request">
								<html:option value="${mailinglist.id}">${mailinglist.shortname}</html:option>
							</logic:iterate>
						</html:select>
					</div>
					<ul class="tile-header-actions">
						<li>
							<button type="button" class="btn btn-regular btn-primary" data-form-set="showStatistic: true, method: save" data-form-submit-static>
								<i class="icon icon-refresh"></i>
								<span class="text"><bean:message key="button.save.evaluate" /></span>
							</button>
						</li>
					</ul>
				</div>
				<div class="tile-content">
					<c:if test="${editTargetForm.showStatistic == 'true' and not empty editTargetForm.statisticUrl}">
						<iframe src="${editTargetForm.statisticUrl}" border="0" scrolling="auto" width="100%" height="500px" frameborder="0"> Your
							Browser does not support IFRAMEs, please update! </iframe>
					</c:if>
				</div>
			</div>
		</c:if>
	</emm:ShowByPermission>

</agn:agnForm>
