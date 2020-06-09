<%@ page language="java" import="com.agnitas.web.*" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<c:set var="ACTION_CONFIRM_DELETE"
	value="<%=ComTargetAction.ACTION_CONFIRM_DELETE%>" scope="page" />
<c:set var="ACTION_DELETE_RECIPIENTS_CONFIRM"
	value="<%=ComTargetAction.ACTION_DELETE_RECIPIENTS_CONFIRM%>"
	scope="page" />
<c:set var="ACTION_CREATE_ML"
	value="<%=ComTargetAction.ACTION_CREATE_ML%>" scope="page" />
<c:set var="ACTION_LIST" value="<%=ComTargetAction.ACTION_LIST%>"
	scope="request" />

<emm:CheckLogon />
<emm:Permission token="targets.show" />

<c:set var="isTabsMenuShown" value="false" scope="request" />
<c:set var="agnNavHrefAppend"
	value="&targetID=${editTargetForm.targetID}" scope="request" />
<c:set var="agnTitleKey" value="Target" scope="request" />
<c:set var="agnSubtitleKey" value="Target" scope="request" />
<c:set var="sidemenu_active" value="Targetgroups" scope="request" />
<c:set var="isBreadcrumbsShown" value="true" scope="request" />
<c:set var="agnBreadcrumbsRootKey" value="Targetgroups" scope="request" />
<c:set var="agnHelpKey" value="targetGroupView" scope="request" />

<%--@elvariable id="editTargetForm" type="com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm"--%>
<c:choose>
	<c:when test="${editTargetForm.targetID ne 0}">
		<c:set var="isTabsMenuShown" value="true" scope="request" />
		<c:set var="agnNavigationKey" value="TargetQBEdit" scope="request" />
		<c:set var="sidemenu_sub_active" value="none" scope="request" />
		<c:set var="agnHighlightKey" value="target.Edit" scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="agnNavigationKey" value="targets" scope="request" />
		<c:set var="sidemenu_sub_active" value="target.NewTarget"
			scope="request" />
		<c:set var="agnHighlightKey" value="target.NewTarget" scope="request" />
	</c:otherwise>
</c:choose>

<c:set var="submitType" value="data-form-submit" />
<c:if
	test="${workflowForwardParams != null && workflowForwardParams != ''}">
	<c:set var="submitType" value="data-form-submit-static" />
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap"
	scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0"
			value="${agnBreadcrumb}" />
		<c:set target="${agnBreadcrumb}" property="textKey"
			value="default.Overview" />
		<c:set target="${agnBreadcrumb}" property="url">
			<c:url value="/target.do">
				<c:param name="action" value="${ACTION_LIST}" />
			</c:url>
		</c:set>
	</emm:instantiate>

	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="1"
			value="${agnBreadcrumb}" />
		<c:choose>
			<c:when test="${editTargetForm.targetID eq 0}">
				<c:set target="${agnBreadcrumb}" property="textKey"
					value="target.NewTarget" />
			</c:when>
			<c:otherwise>
				<c:set target="${agnBreadcrumb}" property="text"
					value="${editTargetForm.shortname}" />
			</c:otherwise>
		</c:choose>
	</emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
	<c:if test="${editTargetForm.workflowForwardParams != null && editTargetForm.workflowForwardParams != ''}">
		<emm:instantiate var="element" type="java.util.LinkedHashMap">
			<c:set target="${itemActionsSettings}" property="4" value="${element}"/>

			<c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular" />
			<c:set target="${element}" property="iconBefore" value="icon-angle-left" />
			<c:set target="${element}" property="type" value="href" />
			<c:set target="${element}" property="url">
                <c:url value="/workflow/${editTargetForm.workflowId}/view.action">
                    <c:param name="forwardParams" value="${editTargetForm.workflowForwardParams};elementValue=${editTargetForm.targetID}"/>
                </c:url>
			</c:set>
			<c:set target="${element}" property="name">
				<bean:message key="button.Back" />
			</c:set>
		</emm:instantiate>
	</c:if>

	<emm:instantiate var="element" type="java.util.LinkedHashMap">
		<c:set target="${itemActionsSettings}" property="0" value="${element}" />

		<c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle" />
		<c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'" />
		<c:set target="${element}" property="iconBefore" value="icon-wrench" />
		<c:set target="${element}" property="name">
			<bean:message key="action.Action" />
		</c:set>
		<c:set target="${element}" property="iconAfter" value="icon-caret-down" />

		<emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
			<c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
		</emm:instantiate>

		<%--options for dropdown--%>
		<c:if test="${not editTargetForm.locked}">
			<c:if test="${not empty editTargetForm.targetID and editTargetForm.targetID != 0}">
				<emm:ShowByPermission token="targets.change">
					<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
						<c:set target="${dropDownItems}" property="2" value="${dropDownItem}" />
						<c:set target="${dropDownItem}" property="url">
							<c:url value="/targetQB.do">
								<c:param name="method" value="copy" />
								<c:param name="targetID" value="${editTargetForm.targetID}" />
							</c:url>
						</c:set>
						<c:set target="${dropDownItem}" property="icon" value="icon-copy" />
						<c:set target="${dropDownItem}" property="name">
							<bean:message key="button.Copy" />
						</c:set>
					</emm:instantiate>
				</emm:ShowByPermission>

				<c:if test="${editTargetForm.mailingId gt 0}">
					<emm:ShowByPermission token="targets.change">
						<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
							<c:set target="${dropDownItems}" property="1" value="${dropDownItem}" />
							<c:set target="${dropDownItem}" property="url">
								<c:url value="/mailingbase.do">
									<c:param name="action" value="2" />
									<c:param name="mailingID" value="${editTargetForm.mailingId}" />
								</c:url>
							</c:set>
							<c:set target="${dropDownItem}" property="icon" value="icon-reply" />
							<c:set target="${dropDownItem}" property="name">
								<bean:message key="button.tomailing" />
							</c:set>
						</emm:instantiate>
					</emm:ShowByPermission>
				</c:if>

				<c:if test="${not empty editTargetForm.targetID and editTargetForm.targetID != 0}">
					<emm:ShowByPermission token="targets.createml">
						<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
							<c:set target="${dropDownItems}" property="5" value="${dropDownItem}" />
							<c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm=''" />
							<c:set target="${dropDownItem}" property="url">
								<c:url value="/target.do">
									<c:param name="action" value="${ACTION_CREATE_ML}" />
									<c:param name="targetID" value="${editTargetForm.targetID}" />
								</c:url>
							</c:set>
							<c:set target="${dropDownItem}" property="icon" value="icon-file-o" />
							<c:set target="${dropDownItem}" property="name">
								<bean:message key="createMList" />
							</c:set>
						</emm:instantiate>
					</emm:ShowByPermission>
				</c:if>

				<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
					<c:set target="${dropDownItems}" property="7" value="${dropDownItem}" />
					<c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-secondary" />
					<c:set target="${dropDownItem}" property="type" value="href" />
					<c:set target="${dropDownItem}" property="url">
						<c:url value="/statistics/recipient/view.action">
							<c:param name="mailinglistId" value="0"/>
							<c:param name="targetId" value="${editTargetForm.targetID}"/>
						</c:url>
					</c:set>
					<c:set target="${dropDownItem}" property="icon" value="icon-bar-chart-o" />
					<c:set target="${dropDownItem}" property="name">
						<bean:message key="Statistics" />
					</c:set>
				</emm:instantiate>

				<emm:ShowByPermission token="targets.lock">
					<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
						<c:set target="${dropDownItems}" property="3" value="${dropDownItem}" />
						<c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-inverse"/>

						<c:set target="${dropDownItem}" property="extraAttributes" value="data-form-target='#targetForm' data-form-set='method:lock' data-form-submit-static" />
						<c:set target="${dropDownItem}" property="url">
							<c:url value="/targetQB.do"/>
						</c:set>
						<c:set target="${dropDownItem}" property="icon" value="icon-lock" />
						<c:set target="${dropDownItem}" property="name">
							<bean:message key="target.lock" />
						</c:set>
					</emm:instantiate>
				</emm:ShowByPermission>
			</c:if>
		</c:if>

		<c:if test="${editTargetForm.locked}">
			<emm:ShowByPermission token="targets.lock">
				<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
					<c:set target="${dropDownItems}" property="4" value="${dropDownItem}" />

					<c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-inverse"/>
					<c:set target="${dropDownItem}" property="extraAttributes" value="data-form-target='#targetForm' data-form-set='method:unlock' data-form-submit-static" />
					<c:set target="${dropDownItem}" property="url">
						<c:url value="/targetQB.do"/>
					</c:set>
					<c:set target="${dropDownItem}" property="icon" value="icon-unlock" />
					<c:set target="${dropDownItem}" property="name">
						<bean:message key="button.Unlock" />
					</c:set>
				</emm:instantiate>
			</emm:ShowByPermission>
		</c:if>

		<c:if test="${not empty editTargetForm.targetID and editTargetForm.targetID != 0}">
			<emm:ShowByPermission token="recipient.delete">
				<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
					<c:set target="${dropDownItems}" property="6" value="${dropDownItem}" />
					<c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm" />
					<c:set target="${dropDownItem}" property="type" value="href" />
					<c:set target="${dropDownItem}" property="url">
						<c:url value="/target.do">
							<c:param name="action" value="${ACTION_DELETE_RECIPIENTS_CONFIRM}" />
							<c:param name="targetID" value="${editTargetForm.targetID}" />
						</c:url>
					</c:set>
					<c:set target="${dropDownItem}" property="icon" value="icon-trash-o" />
					<c:set target="${dropDownItem}" property="name">
						<bean:message key="target.delete.recipients" />
					</c:set>
				</emm:instantiate>
			</emm:ShowByPermission>
		</c:if>

		<c:if test="${not editTargetForm.locked}">
			<emm:ShowByPermission token="targets.delete">
				<emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
					<c:set target="${dropDownItems}" property="0" value="${dropDownItem}" />
					<c:set target="${dropDownItem}" property="icon" value="icon-trash-o" />
					<c:set target="${dropDownItem}" property="url">
						<c:url value="/target.do">
							<c:param name="action" value="${ACTION_CONFIRM_DELETE}" />
							<c:param name="targetID" value="${editTargetForm.targetID}" />
						</c:url>
					</c:set>
					<c:set target="${dropDownItem}" property="name">
						<bean:message key="button.Delete" />
					</c:set>
					<c:set target="${dropDownItem}" property="extraAttributes" value=" data-confirm=''" />
				</emm:instantiate>
			</emm:ShowByPermission>
		</c:if>

	</emm:instantiate>

	<c:if test="${not editTargetForm.locked}">
		<emm:ShowByPermission token="targets.change">
				<c:set var="SHOW_SAVE_BUTTON" value="true" scope="page" />
		</emm:ShowByPermission>
		<emm:HideByPermission token="targets.change">
			<c:set var="SHOW_SAVE_BUTTON" value="false" scope="page" />
		</emm:HideByPermission>

		<c:if test="${SHOW_SAVE_BUTTON}">
			<emm:instantiate var="element" type="java.util.LinkedHashMap">
				<c:set target="${itemActionsSettings}" property="3" value="${element}" />
				<c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse" />
				<c:set target="${element}" property="extraAttributes" value="data-form-set='method:save' data-form-target='#targetForm' ${submitType}" />
				<c:set target="${element}" property="iconBefore" value="icon-save" />
				<c:set target="${element}" property="name">
					<bean:message key="button.Save" />
				</c:set>
				<c:set target="${element}" property="url">
					<html:rewrite page="/targetQB.do" />
				</c:set>
			</emm:instantiate>
		</c:if>
	</c:if>

</emm:instantiate>
