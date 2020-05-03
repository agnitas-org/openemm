<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.beans.ComAdminPreferences" %>
<%@ page import="com.agnitas.beans.TargetLight" %>
<%@ page import="com.agnitas.web.ComMailingBaseAction" %> <%-- ComMailingBaseAction Required for view_base_settings-follow3.jspf --%>
<%@ page import="com.agnitas.web.ComTargetAction" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="ACTION_VIEW" value="<%= ComTargetAction.ACTION_VIEW %>" scope="request" />
<c:set var="ACTION_REMOVE_TARGET" value="<%= MailingBaseAction.ACTION_REMOVE_TARGET%>" scope="page"/>

<c:set var="WORKFLOW_ID" value="<%= WorkflowParametersHelper.WORKFLOW_ID %>" scope="page"/>
<c:set var="WORKFLOW_FORWARD_PARAMS" value="<%= WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS %>" scope="page"/>

<c:set var="MAILING_SETTINGS_EXPANDED" value="<%= ComAdminPreferences.MAILING_SETTINGS_EXPANDED %>"/>
<c:set var="MAILING_SETTINGS_COLLAPSED" value="<%= ComAdminPreferences.MAILING_SETTINGS_COLLAPSED %>"/>

<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND %>"/>

<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND %>"/>

<c:set var="LIST_SPLIT_PREFIX" value="<%= TargetLight.LIST_SPLIT_PREFIX %>"/>
<c:set var="LIST_SPLIT_CM_PREFIX" value="<%= TargetLight.LIST_SPLIT_CM_PREFIX %>"/>

<c:set var="MAILING_TYPE_NORMAL" value="<%= Mailing.TYPE_NORMAL %>"/>
<c:set var="MAILING_TYPE_FOLLOWUP" value="<%= Mailing.TYPE_FOLLOWUP %>"/>
<c:set var="MAILING_TYPE_ACTIONBASED" value="<%= Mailing.TYPE_ACTIONBASED %>"/>
<c:set var="MAILING_TYPE_DATEBASED" value="<%= Mailing.TYPE_DATEBASED %>"/>
<c:set var="MAILING_TYPE_INTERVAL" value="<%= Mailing.TYPE_INTERVAL %>"/>

<c:set var="FOLLOWUP_TYPE_OPENER" value="<%= Mailing.TYPE_FOLLOWUP_OPENER %>"/>
<c:set var="FOLLOWUP_TYPE_NON_OPENER" value="<%= Mailing.TYPE_FOLLOWUP_NON_OPENER %>"/>
<c:set var="FOLLOWUP_TYPE_CLICKER" value="<%= Mailing.TYPE_FOLLOWUP_CLICKER %>"/>
<c:set var="FOLLOWUP_TYPE_NON_CLICKER" value="<%= Mailing.TYPE_FOLLOWUP_NON_CLICKER %>"/>

<c:set var="allSubscribersMessage"><bean:message key="statistic.all_subscribers"/></c:set>
<c:set var="editWithCampaignManagerMessage" scope="page"><bean:message key="mailing.EditWithCampaignManager"/></c:set>

<c:if test="${mailingBaseForm.isTemplate}">
    <c:set target="${mailingBaseForm}" property="showTemplate" value="${true}"/>
</c:if>

<c:choose>
    <c:when test="${sessionScope[WORKFLOW_ID] ne null and sessionScope[WORKFLOW_ID] ne 0}">
        <c:set var="workflowId" value="${sessionScope[WORKFLOW_ID]}" scope="page"/>
        <c:set var="usedInCampaign" value="true" scope="page"/>
    </c:when>
    <c:when test="${mailingBaseForm.workflowId ne 0}">
        <c:set var="workflowId" value="${mailingBaseForm.workflowId}" scope="page"/>
        <c:set var="usedInCampaign" value="true" scope="page"/>
    </c:when>
    <c:otherwise>
        <c:set var="workflowId" value="0" scope="page"/>
        <c:set var="usedInCampaign" value="false" scope="page"/>
    </c:otherwise>
</c:choose>

<c:set var="isMailingEditable" value="${not mailingBaseForm.worldMailingSend}"/>
<emm:ShowByPermission token="mailing.content.change.always">
    <c:set var="isMailingEditable" value="${true}"/>
</emm:ShowByPermission>

<c:set var="isEmailSettingsEditable" value="${mailingBaseForm.canChangeEmailSettings}"/>

<c:set var="isMailinglistEditable" value="${mailingBaseForm.canChangeMailinglist}"/>

<c:set var="workflowParameters" value="${emm:getWorkflowParams(pageContext.request)}"/>

<c:url var="editWithCampaignManagerLink" value="/workflow/${workflowParameters.workflowId}/view.action">
    <c:param name="forwardParams" value="${workflowParameters.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}"/>
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
    <div id="tile-mailingGeneral" class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <label for="settingsGeneralMailingList"><bean:message key="Mailinglist"/></label>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MailingListMsg.xml" tabindex="-1" type="button"></button>
                    <c:if test="${not isMailinglistEditable}">
                        <div class="icon icon-warning" data-tooltip="<bean:message key="warning.mailinglist.disabled"/>"></div>
                    </c:if>
                </label>
            </div>
            <div class="col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <c:set var="isMailinglistDisabled" value="${not isMailingEditable or usedInCampaign or not isEmailSettingsEditable or not isMailinglistEditable}"/>
                        <html:select styleId="settingsGeneralMailingList" styleClass="form-control js-select" property="mailinglistID" size="1"
                                     disabled="${isMailinglistDisabled}">
                            <logic:iterate id="mailinglist" name="mailingBaseForm" property="mailingLists">
                                <html:option value="${mailinglist.id}">${mailinglist.shortname}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                    <c:if test="${usedInCampaign}">
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
                            <html:select styleId="settings_general_campaign" styleClass="form-control js-select" property="campaignID" disabled="${usedInCampaign}">
                                <html:option value="0"><bean:message key="mailing.NoCampaign"/></html:option>
                                <logic:iterate id="campaign" name="mailingBaseForm" property="campaigns" length="500">
                                    <html:option value="${campaign.id}">${campaign.shortname}</html:option>
                                </logic:iterate>
                            </html:select>
                        </div>

                        <c:if test="${usedInCampaign}">
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
                                           disabled="${not isMailingEditable or usedInCampaign or not isEmailSettingsEditable}">
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
                        <c:if test="${usedInCampaign}">
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

<div id="mailingTargets" class="tile" data-action="change-mailing-settings" >
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingTargets">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="Targets"/>
        </a>
    </div>
    <div id="tile-mailingTargets" class="tile-content tile-content-forms">
        <c:choose>
            <c:when test="${mailingBaseForm.complexTargetExpression or usedInCampaign}">
                <div class="form-group ${mailingBaseForm.complexTargetExpression ? 'has-feedback has-alert' : ''}">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="Targets"/>
                        </label>
                    </div>

                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <c:choose>
                                    <c:when test="${mailingBaseForm.complexTargetExpression}">
                                        <div class="well block">
                                            <bean:message key="targetgroup.tooComplex"/>
                                        </div>
                                        <i class="icon icon-state-alert form-control-feedback"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="well block">
                                            <span><bean:message key="mailing.EditWithCampaignManager"/></span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <c:if test="${usedInCampaign}">
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

                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <%-- Target groups aren't editable if mailing has complex target expression or is managed by campaign --%>
                        <agn:agnHidden styleId="assignTargetGroups" property="assignTargetGroups" value="false"/>

                        <agn:agnSelect styleId="targetGroupIds" property="targetGroupIds" styleClass="form-control js-select" multiple="true" data-placeholder="${allSubscribersMessage}" disabled="true">
                            <logic:iterate id="target" name="mailingBaseForm" property="targets">
                            
                            	<%-- Build link to target group editor --%>
                                <c:url var="targetLink" value="/targetQB.do">
                                    <c:param name="method" value="show"/>
                                    <c:param name="targetID" value="${target.id}"/>
                                </c:url>

                                <agn:agnOption value="${target.id}" data-url="${targetLink}">${target.targetName} (${target.id})</agn:agnOption>
                            </logic:iterate>
                        </agn:agnSelect>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="Targets"/>
                        </label>
                    </div>

                    <div class="col-sm-8">
                        <%-- Target groups are editable unless mailing is sent --%>
                        <agn:agnHidden styleId="assignTargetGroups" property="assignTargetGroups" value="${not mailingBaseForm.worldMailingSend}"/>

                        <agn:agnSelect styleId="targetGroupIds" property="targetGroupIds" styleClass="form-control js-select" multiple="true" data-placeholder="${allSubscribersMessage}" disabled="${mailingBaseForm.worldMailingSend}" data-action="selectTargetGroups">
                            <logic:iterate id="target" name="mailingBaseForm" property="targets">

                            	<%-- Build link to target group editor --%>
                                <c:url var="targetLink" value="/targetQB.do">
                                    <c:param name="method" value="show"/>
                                    <c:param name="targetID" value="${target.id}"/>
                                </c:url>

                                <agn:agnOption value="${target.id}" data-url="${targetLink}">${target.targetName} (${target.id})</agn:agnOption>
                            </logic:iterate>
                        </agn:agnSelect>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <c:set var="isTargetModeCheckboxDisabled" value="${usedInCampaign or mailingBaseForm.worldMailingSend}"/>

        <c:if test="${not (mailingBaseForm.complexTargetExpression or (isTargetModeCheckboxDisabled and fn:length(mailingBaseForm.targetGroupIds) <= 1))}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <html:hidden property="__STRUTS_CHECKBOX_targetMode" value="${TARGET_MODE_OR}"/>

                    <div class="checkbox">
                        <label>
                            <html:checkbox styleId="targetModeCheck" property="targetMode" value="${TARGET_MODE_AND}" disabled="${isTargetModeCheckboxDisabled}"/>
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

                        <c:set var="isNeedsTargetCheckboxDisabled" value="${not isMailingEditable or mailingBaseForm.mailingType eq MAILING_TYPE_DATEBASED or  mailingBaseForm.mailingType eq MAILING_TYPE_INTERVAL}"/>
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
                             property="splitBase" disabled="${not isMailingEditable or mailingBaseForm.wmSplit or usedInCampaign}">
                    <c:if test="${mailingBaseForm.splitId eq -1}">
                        <html:option value="yes"><bean:message key="Yes"/></html:option>
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
                        <html:select styleId="settingsTargetgroupsListSplitPart" property="splitPart" disabled="${not isMailingEditable or mailingBaseForm.wmSplit or usedInCampaign}"
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

                    <c:if test="${usedInCampaign}">
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
