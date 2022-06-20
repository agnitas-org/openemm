<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@page import="org.agnitas.dao.FollowUpType"%>
<%@ page import="com.agnitas.beans.AdminPreferences" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.beans.TargetLight" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>  <%-- Required for view_base_settings-follow3.jspf --%>
<%@ page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="MAILING_SETTINGS_EXPANDED" value="<%=AdminPreferences.MAILING_SETTINGS_EXPANDED%>"/>
<c:set var="MAILING_SETTINGS_COLLAPSED" value="<%=AdminPreferences.MAILING_SETTINGS_COLLAPSED%>"/>

<c:set var="TARGET_MODE_OR" value="<%=Mailing.TARGET_MODE_OR%>"/>
<c:set var="TARGET_MODE_AND" value="<%=Mailing.TARGET_MODE_AND%>"/>

<c:set var="LIST_SPLIT_PREFIX" value="<%= TargetLight.LIST_SPLIT_PREFIX %>"/>
<c:set var="LIST_SPLIT_CM_PREFIX" value="<%= TargetLight.LIST_SPLIT_CM_PREFIX %>"/>

<c:set var="MAILING_TYPE_NORMAL" value="<%= MailingType.NORMAL.getCode() %>"/>
<c:set var="MAILING_TYPE_FOLLOWUP" value="<%= MailingType.FOLLOW_UP.getCode() %>"/>
<c:set var="MAILING_TYPE_ACTIONBASED" value="<%= MailingType.ACTION_BASED.getCode() %>"/>
<c:set var="MAILING_TYPE_DATEBASED" value="<%= MailingType.DATE_BASED.getCode() %>"/>
<c:set var="MAILING_TYPE_INTERVAL" value="<%= MailingType.INTERVAL.getCode() %>"/>

<c:set var="FOLLOWUP_TYPE_OPENER" value="<%= FollowUpType.TYPE_FOLLOWUP_OPENER.getKey() %>"/>
<c:set var="FOLLOWUP_TYPE_NON_OPENER" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey() %>"/>
<c:set var="FOLLOWUP_TYPE_CLICKER" value="<%= FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey() %>"/>
<c:set var="FOLLOWUP_TYPE_NON_CLICKER" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey() %>"/>

<c:set var="COMPLEXITY_GREEN" value="<%= TargetComplexityGrade.GREEN.toString() %>" scope="page"/>
<c:set var="COMPLEXITY_YELLOW" value="<%= TargetComplexityGrade.YELLOW.toString() %>" scope="page"/>
<c:set var="COMPLEXITY_RED" value="<%= TargetComplexityGrade.RED.toString() %>" scope="page"/>

<c:set var="allSubscribersMessage"><bean:message key="statistic.all_subscribers"/></c:set>

<c:if test="${mailingBaseForm.isTemplate}">
    <c:set target="${mailingBaseForm}" property="showTemplate" value="${true}"/>
</c:if>

<c:set var="isEmailSettingsEditable" value="${mailingBaseForm.canChangeEmailSettings}"/>
<c:set var="isMailinglistEditable" value="${mailingBaseForm.canChangeMailinglist}"/>

<c:set var="formWorkflowId" value="${mailingBaseForm.workflowId}"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, mailingBaseForm.workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>

<c:url var="editWithCampaignManagerLink" value="/workflow/${workflowId}/view.action" scope="page">
    <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}" />
</c:url>

<script type="text/javascript">
    (function(){
        var splits = {};
        var splitIndex;

        <c:set var="aktName" value=""/>
        <logic:iterate id="target" name="mailingBaseForm" property="splitTargets">
            <c:set var="aNamePage" value="${target.targetName}"/>

            <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX), fn:length(aNamePage))}"/>
            <c:set var="aNameBase" value="${fn:substring(aNameCode, 0, fn:indexOf(aNameCode, '_'))}"/>
            <c:set var="aNamePart" value="${fn:substring(aNameCode, fn:indexOf(aNameCode, '_') + 1, fn:length(aNameCode))}"/>

            <c:choose>
                <c:when test="${fn:contains(aNameBase, '.')}">
                    <c:set var="aLabel" value=""/>

                    <fmt:parseNumber var="aSplitPieceIndex" value="${aNamePart}" type="number" integerOnly="true"/>
                    <fmt:parseNumber var="aSplitPiece" value="${fn:split(aNameBase, ';')[aSplitPieceIndex - 1]}" type="number" integerOnly="true"/>
                    <c:set var="aLabel" value="${aNamePart}. ${aSplitPiece}%"/>
                </c:when>
                <c:otherwise>
                    <c:set var="aLabel"><bean:message key="listsplit.${aNameBase}.${aNamePart}"/></c:set>
                </c:otherwise>
            </c:choose>

            // aLabel: ${aLabel}

            splitIndex = '${aNameBase}';
            splits[splitIndex] = $.extend(splits[splitIndex], {
                '${aNamePart}': '${aLabel}'
            });
            </c:if>
        </logic:iterate>

        AGN.Opt.MailingBaseFormSplits = splits;
    })();
</script>

<div class="tile" data-action="change-mailing-settings">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingGeneral">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="mailing.generalSettings"/>
        </a>
    </div>
    <div id="tile-mailingGeneral" class="tile-content tile-content-forms" data-action="scroll-to">
        <div class="form-group" data-field="validator">
            <div class="col-sm-4">
                <label class="control-label">
                    <label for="settingsGeneralMailingList"><bean:message key="Mailinglist"/>*</label>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MailingListMsg.xml" tabindex="-1" type="button"></button>
                    <c:if test="${not isMailinglistEditable}">
                        <div class="icon icon-warning" data-tooltip="<bean:message key="warning.mailinglist.disabled"/>"></div>
                    </c:if>
                </label>
            </div>
            <div class="col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <c:set var="isMailinglistDenied" value="${not isMailinglistEditable or not IS_MAILING_EDITABLE or not isEmailSettingsEditable or isWorkflowDriven}"/>
                            <agn:agnSelect styleId="settingsGeneralMailingList" styleClass="form-control js-select" property="mailinglistID" size="1"
                                           data-action="save-mailing-list-id"
                                           disabled="${isMailinglistDenied}"
                                           data-field-validator="mailinglist-exist"
                                           data-validator-options='removedMailinglistId: ${not empty mailingBaseForm.selectedRemovedMailinglist ? mailingBaseForm.selectedRemovedMailinglist.id : ""}'>
                                <c:if test="${not empty mailingBaseForm.selectedRemovedMailinglist and not mailingBaseForm.copiedMailing}">
                                    <agn:agnOption value="${mailingBaseForm.selectedRemovedMailinglist.id}" styleId="${mailingBaseForm.selectedRemovedMailinglist.id}-mailinglist">${mailingBaseForm.selectedRemovedMailinglist.shortname}</agn:agnOption>
                                </c:if>
                                <agn:agnOption value="0" styleId="0-mailinglist">--</agn:agnOption>
                                <logic:iterate id="mailinglist" name="mailingBaseForm" property="mailingLists">
                                    <agn:agnOption value="${mailinglist.id}" styleId="${mailinglist.id}-mailinglist">${mailinglist.shortname}</agn:agnOption>
                                </logic:iterate>
                            </agn:agnSelect>
                    </div>
                    <c:if test="${isWorkflowDriven}">
                        <div class="input-group-btn">
                            <a href="${editWithCampaignManagerLink}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                <i class="icon icon-linkage-campaignmanager"></i>
                                <strong><bean:message key="campaign.manager.icon"/></strong>
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <emm:ShowByPermission token="campaign.show">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="settings_general_campaign"><bean:message key="mailing.archive"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/CampaignMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <html:select styleId="settings_general_campaign" styleClass="form-control js-select" property="campaignID" disabled="${isWorkflowDriven}">
                                <html:option value="0"><bean:message key="mailing.NoCampaign"/></html:option>
                                <logic:iterate id="campaign" name="mailingBaseForm" property="campaigns" length="500">
                                    <html:option value="${campaign.id}">${campaign.shortname}</html:option>
                                </logic:iterate>
                            </html:select>
                        </div>

                        <c:if test="${isWorkflowDriven}">
                            <div class="input-group-btn">
                                <a href="${editWithCampaignManagerLink}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                    <i class="icon icon-linkage-campaignmanager"></i>
                                    <strong><bean:message key="campaign.manager.icon"/></strong>
                                </a>
                            </div>
                        </c:if>
                    </div>
                </div>
                <div class="col-sm-4">
                    <input type="hidden" name="__STRUTS_CHECKBOX_archived" value="0"/>
                    <label class="toggle">
                        <html:checkbox property="archived" disabled="${not isEmailSettingsEditable}"/>
                        <div class="toggle-control"></div>
                        <span class="text">
                            <bean:message key="mailing.archived"/>
                        	<button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/ShowInOnlineArchive.xml" tabindex="-1" type="button" data-original-title="" title=""></button>
                    	</span>
                	</label>
            	</div>
            </div>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="mailing.show.types">

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="settingsGeneralMailType"><bean:message key="mailing.Mailing_Type"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MailingTypeMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <agn:agnSelect property="mailingType" id="settingsGeneralMailType" class="form-control" data-action="change-general-mailing-type"
                                           disabled="${not IS_MAILING_EDITABLE or isWorkflowDriven or not isEmailSettingsEditable}">
                                <html:option value="${MAILING_TYPE_NORMAL}">
                                    <bean:message key="Normal_Mailing"/>
                                </html:option>
                                <html:option value="${MAILING_TYPE_ACTIONBASED}">
                                    <bean:message key="mailing.action.based.mailing"/>
                                </html:option>
                                <html:option value="${MAILING_TYPE_DATEBASED}">
                                    <bean:message key="mailing.Rulebased_Mailing"/>
                                </html:option>
								<%@include file="view_base_settings-follow.jspf" %>
								<%@include file="view_base_settings-interval.jspf" %>
                            </agn:agnSelect>
                        </div>
                        <c:if test="${isWorkflowDriven}">
                            <div class="input-group-btn">
                                <a href="${editWithCampaignManagerLink}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                    <i class="icon icon-linkage-campaignmanager"></i>
                                    <strong><bean:message key="campaign.manager.icon"/></strong>
                                </a>
                            </div>
                        </c:if>
                    </div>
                    <%@include file="view_base_settings-follow2.jspf" %>
                </div>
            </div>
            <%@include file="view_base_settings-follow3.jspf" %>
        </emm:ShowByPermission>
    </div>
</div>

<%@include file="fragments/access-limiting-target-groups-tile.jspf" %>

<div id="mailingTargets" class="tile" data-action="change-mailing-settings" >
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingTargets">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="Targets"/>
        </a>
    </div>
    <div id="tile-mailingTargets" class="tile-content tile-content-forms" data-action="scroll-to">
        <c:set var="targets" value="${mailingBaseForm.targets}" scope="page"/>
        <c:set var="complexTargetExpression" value="${mailingBaseForm.complexTargetExpression}" scope="page"/>
        <c:set var="targetComplexities" value="${mailingBaseForm.targetComplexities}" scope="page"/>
        <c:set var="targets" value="${mailingBaseForm.targets}" scope="page"/>
        <c:set var="worldMailingSend" value="${mailingBaseForm.worldMailingSend}" scope="page"/>
        <%@include file="fragments/mailing-targets-select.jspf" %>

        <c:set var="isTargetModeCheckboxDisabled" value="${isWorkflowDriven or mailingBaseForm.worldMailingSend}"/>

		<c:if test="${SHOW_TARGET_MODE_CHECKBOX}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                	<html:hidden property="__STRUTS_CHECKBOX_targetMode" value="${TARGET_MODE_OR}"/>
                    <div class="checkbox">
                        <label>
	                        <html:checkbox styleId="targetModeCheck" property="targetMode" value="${TARGET_MODE_AND}" disabled="${DISABLE_TARGET_MODE_CHECKBOX}" />
                            <bean:message key="mailing.targetmode.and"/>
                        </label>
                    </div>
                </div>
            </div>
        </c:if>

        <c:if test="${mailingBaseForm.mailingID > 0}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <p class="help-block">
                        <bean:message key="report.numberRecipients"/>:
                        <strong id="calculatedRecipientsBadge">?</strong>
                    </p>

                    <button type="button" class="btn btn-regular" data-action="calculateRecipients">
                        <span><bean:message key="button.Calculate"/></span>
                    </button>
                </div>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${mailingBaseForm.isTemplate}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="mailing.needsTarget"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:hidden property="__STRUTS_CHECKBOX_needsTarget" value="false"/>

                        <c:set var="isNeedsTargetCheckboxDisabled" value="${not IS_MAILING_EDITABLE or mailingBaseForm.mailingType eq MAILING_TYPE_DATEBASED or  mailingBaseForm.mailingType eq MAILING_TYPE_INTERVAL}"/>
                        <label class="toggle">
                            <html:checkbox property="needsTarget" disabled="${isNeedsTargetCheckboxDisabled}"/>
                            <div class="toggle-control"></div>
                        </label>

                        <c:if test="${isNeedsTargetCheckboxDisabled}">
                            <html:hidden property="needsTarget"/>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <html:hidden property="needsTarget"/>
            </c:otherwise>
        </c:choose>

        <div class="form-group" data-field="double-select" data-provider="opt" data-provider-src="MailingBaseFormSplits">
            <div class="col-sm-4">
                <label class="control-label">
                    <label for="settingsTargetgroupsListSplit"><bean:message key="mailing.listsplit"/></label>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/ListSplitMsg.xml" tabindex="-1" type="button"></button>
                </label>
            </div>

            <div class="col-sm-8">
                <html:select styleId="settingsTargetgroupsListSplit" styleClass="form-control js-double-select-trigger"
                             property="splitBase" disabled="${not IS_MAILING_EDITABLE or mailingBaseForm.wmSplit or isWorkflowDriven}">
                    <c:if test="${mailingBaseForm.splitId eq -1}">
                        <html:option value="yes"><bean:message key="default.Yes"/></html:option>
                    </c:if>
                    <html:option value="none"><bean:message key="listsplit.none"/></html:option>

                    <c:set var="aktName" value=""/>
                    <logic:iterate id="target" name="mailingBaseForm" property="splitTargets" length="500">
                        <c:set var="aNamePage" value="${target.targetName}" />

                        <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
                            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX), fn:length(aNamePage))}"/>
                            <c:set var="aNameBase" value="${fn:substring(aNameCode, 0, fn:indexOf(aNameCode, '_'))}"/>

                            <c:choose>
                                <c:when test="${fn:contains(aNameBase, '.')}">
                                    <c:set var="aLabel" value=""/>
                                    <c:forEach var="aSplitPiece" items="${fn:split(aNameBase, ';')}" varStatus="status">
                                        <fmt:parseNumber var="aSplitPiece" value="${aSplitPiece}" type="number" integerOnly="true"/>
                                        <c:set var="aLabel" value="${aLabel}${aSplitPiece}%"/>
                                        <c:if test="${not status.last}">
                                            <c:set var="aLabel" value="${aLabel} / "/>
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="aLabel"><bean:message key="listsplit.${aNameBase}"/></c:set>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${aNameBase ne aktName}">
                                <c:set var="aktName" value="${aNameBase}"/>
                                <html:option value="${aNameBase}">${aLabel}</html:option>
                            </c:if>
                        </c:if>
                    </logic:iterate>
                    <logic:equal name="mailingBaseForm" property="wmSplit" value="true">
                        <html:option value="${mailingBaseForm.splitBase}">${mailingBaseForm.splitBaseMessage}</html:option>
                    </logic:equal>
                </html:select>
            </div>

            <div class="col-sm-offset-4 col-sm-8">
                <div class="input-group">

                    <div class="input-group-controls">
                        <html:select styleId="settingsTargetgroupsListSplitPart" property="splitPart" disabled="${not IS_MAILING_EDITABLE or mailingBaseForm.wmSplit or isWorkflowDriven}"
                                     styleClass="form-control js-double-select-target">

                            <c:choose>
                                <c:when test="${mailingBaseForm.wmSplit}">
                                    <html:option value="${mailingBaseForm.splitPart}">${mailingBaseForm.splitPartMessage}</html:option>
                                </c:when>

                                <c:otherwise>
                                    <logic:iterate id="target" name="mailingBaseForm" property="splitTargetsForSplitBase" length="500">
                                        <c:set var="aNamePage" value="${target.targetName}"/>
                                        <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
                                            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX) + 1, fn:length(aNamePage))}"/>
                                            <c:set var="aNamePart" value="${fn:substring(aNameCode, fn:indexOf(aNameCode, '_') + 1, fn:length(aNameCode))}"/>
                                            <html:option value="${aNamePart}">
                                                <bean:message key="listsplit.${mailingBaseForm.splitBase}.${aNamePart}"/>
                                            </html:option>
                                        </c:if>
                                    </logic:iterate>
                                </c:otherwise>
                            </c:choose>

                        </html:select>
                    </div>

                    <c:if test="${isWorkflowDriven}">
                        <div class="input-group-btn">
                            <a href="${editWithCampaignManagerLink}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                <i class="icon icon-linkage-campaignmanager"></i>
                                <strong><bean:message key="campaign.manager.icon"/></strong>
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>

        </div>

    </div>
</div>
