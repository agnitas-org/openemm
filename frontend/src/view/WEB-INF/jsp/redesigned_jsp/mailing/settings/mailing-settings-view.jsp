<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"    uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="selectedRemovedMailinglist" type="org.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="isCampaignEnableTargetGroups" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="wmSplit" type="java.lang.Boolean"--%>

<c:set var="EMAIL_MEDIATYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>"/>
<c:set var="ACTIONBASED_MAILING_TYPE" value="<%= MailingType.ACTION_BASED %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>
<c:set var="FOLLOWUP_MAILING_TYPE" value="<%= MailingType.FOLLOW_UP %>"/>
<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL %>"/>
<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}" scope="request"/>
<c:set var="mailingFollowUpAllowed" value="${false}"/>
<%@include file="fragments/mailing-followup-allowed-flag.jspf" %>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<mvc:form cssClass="tiles-container hidden d-flex" servletRelativeAction="/mailing/${mailingId}/settings.action?isGrid=${isMailingGrid}&isTemplate=${isTemplate}" id="mailingSettingsForm" modelAttribute="mailingSettingsForm" data-form="resource" data-disable-controls="save"
          data-controller="mailing-settings-view"
          data-form-focus="${not isPostMailing and not isMailingGrid and mailingId ne 0 ? '' : 'shortname'}"
          data-action="save"
          data-field="toggle-vis"
          data-editable-view="${agnEditViewKey}">
    <mvc:hidden path="parentId"/>
    <emm:workflowParameters/>

    <script data-initializer="mailing-settings-view" type="application/json">
        {
            "mailingId": ${mailingId},
            "isCopying": ${not empty isCopying and isCopying},
            "isMailingGrid": ${isMailingGrid},
            "FOLLOWUP_MAILING_TYPE": "${FOLLOWUP_MAILING_TYPE}",
            "ACTIONBASED_MAILING_TYPE": "${ACTIONBASED_MAILING_TYPE}",
            "DATEBASED_MAILING_TYPE": "${DATEBASED_MAILING_TYPE}",
            "followUpAllowed": ${mailingFollowUpAllowed},
            "wmSplit": ${not empty wmSplit and wmSplit},
            "workflowDriven": ${workflowDriven},
            "mailingType": "${mailingSettingsForm.mailingType}",
            "selectedRemovedMailinglistId": ${emm:toJson(selectedRemovedMailinglist.id)},
            <emm:ShowByPermission token="mailinglists.addresses">
                "mailinglists": ${emm:toJson(mailinglists)},
                "allowedMailinglistsAddresses": ${emm:toJson(mailinglists)},
            </emm:ShowByPermission>
            "campaignEnableTargetGroups": ${isCampaignEnableTargetGroups},
            "TARGET_MODE_OR": "${TARGET_MODE_OR}"
        }
    </script>

    <jsp:include page="tiles/mailing-general-settings-tile.jsp" />

    <jsp:include page="tiles/mailing-sending-settings-tile.jsp" />

    <jsp:include page="tiles/mailing-targeting-settings-tile.jsp" />

    <jsp:include page="tiles/mailing-extended-settings-tile.jsp" />
</mvc:form>

<script id="edit-with-campaign-btn" type="text/x-mustache-template">
    <c:if test="${workflowId > 0}">
        <c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="request"/>
        <c:url var="editWithCampaignLink" value="/workflow/${workflowId}/view.action">
            <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingId}"/>
        </c:url>
        <a href="${editWithCampaignLink}" class="status-badge mailing.status.cm"
           data-tooltip="<mvc:message code='mailing.EditWithCampaignManager'/>"
           style="cursor: pointer"></a>
    </c:if>
</script>