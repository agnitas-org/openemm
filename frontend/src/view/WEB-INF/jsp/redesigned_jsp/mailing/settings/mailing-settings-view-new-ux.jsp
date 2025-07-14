<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="selectedRemovedMailinglist" type="com.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="isCampaignEnableTargetGroups" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="wmSplit" type="java.lang.Boolean"--%>

<c:set var="ACTIONBASED_MAILING_TYPE" value="<%= MailingType.ACTION_BASED %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>
<c:set var="FOLLOWUP_MAILING_TYPE" value="<%= MailingType.FOLLOW_UP %>"/>
<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL %>"/>
<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="EMAIL_MEDIATYPE" value="<%= MediaTypes.EMAIL %>" scope="page"/>
<c:set var="SMS_MEDIATYPE" value="<%= MediaTypes.SMS %>"/>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}" scope="request"/>
<c:set var="mailingFollowUpAllowed" value="${false}"/>
<%@include file="fragments/mailing-followup-allowed-flag.jspf" %>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<c:set var="isNewUx" value="false"/>
<emm:ShowByPermission token="ux.updates">
    <c:set var="isNewUx" value="true"/>
</emm:ShowByPermission>

<c:set var="showExtendedTab" value="${emm:permissionAllowed('mailing.parameter.show', pageContext.request)}" />
<%@include file="fragments/show-extended-tab.jspf" %>

<div class="tiles-container" data-editable-view="${agnEditViewKey}">
    <mvc:form id="mailingSettingsForm" cssClass="tile" modelAttribute="mailingSettingsForm" cssStyle="flex: 1 1 55%"
              servletRelativeAction="/mailing/${mailingId}/settings.action?isGrid=${isMailingGrid}&isTemplate=${isTemplate}"
              data-form="resource"
              data-disable-controls="save"
              data-controller="mailing-settings-view"
              data-form-focus="${not isPostMailing and not isMailingGrid and mailingId ne 0 ? '' : 'shortname'}"
              data-action="save"
              data-editable-tile="">

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
                "campaignEnableTargetGroups": ${isCampaignEnableTargetGroups},
                "TARGET_MODE_OR": "${TARGET_MODE_OR}",
                "isNewUx": "${isNewUx}"
            }
        </script>

        <script data-initializer="mailing-settings-base-view" type="application/json">
            {
                "selectedRemovedMailinglistId": ${emm:toJson(selectedRemovedMailinglist.id)},
                "mailinglists": ${emm:toJson(mailinglists)}
            }
        </script>

        <nav class="tile-header navbar navbar-expand-lg border-bottom">
            <a class="btn btn-header-tab active" href="#"><span class="text text-truncate"></span></a>
            <button class="navbar-toggler btn-icon" type="button" data-bs-toggle="offcanvas" data-bs-target="#mailing-settings-nav" aria-controls="mailing-settings-nav" aria-expanded="false">
                <i class="icon icon-bars"></i>
            </button>
            <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="mailing-settings-nav">
                <ul class="navbar-nav offcanvas-body">
                    <li class="nav-item">
                        <a class="btn btn-outline-primary active" href="#" data-toggle-tab="#general-tab" data-bs-dismiss="offcanvas">
                            <span class="text-truncate"><mvc:message code="General"/></span>
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="btn btn-outline-primary" href="#" data-toggle-tab="#sending-tab" data-bs-dismiss="offcanvas">
                            <span class="text-truncate"><mvc:message code="GWUA.sending"/></span>
                        </a>
                    </li>

                    <c:if test="${not isPostMailing}">
                        <li class="nav-item">
                            <a class="btn btn-outline-primary" href="#" data-toggle-tab="#frame-content-tab" data-bs-dismiss="offcanvas">
                                <span class="text-truncate"><mvc:message code="mailing.frame"/></span>
                            </a>
                        </li>
                    </c:if>

                    <c:if test="${showExtendedTab}">
                        <li class="nav-item">
                            <a class="btn btn-outline-primary" href="#" data-toggle-tab="#extended-tab" data-bs-dismiss="offcanvas">
                                <span class="text-truncate"><mvc:message code="default.advanced"/></span>
                            </a>
                        </li>
                    </c:if>
                </ul>
            </div>
        </nav>

        <div id="general-tab" class="tile-body form-column js-scrollable">
            <jsp:include page="tiles/mailing-general-settings-tab-new-ux.jsp" />
        </div>
        <div id="sending-tab" class="tile-body form-column js-scrollable hidden">
            <jsp:include page="tiles/mailing-sending-settings-tab-new-ux.jsp" />
        </div>

        <c:if test="${not isPostMailing}">
            <div id="frame-content-tab" class="tile-body js-scrollable hidden">
                <jsp:include page="tiles/mailing-frame-content-tab-new-ux.jsp">
                    <jsp:param name="showText" value="true"/>
                    <jsp:param name="showHtml" value="${not isMailingGrid}"/>
                    <jsp:param name="showSms" value="${not isMailingGrid}"/>
                </jsp:include>
            </div>
        </c:if>

        <c:if test="${showExtendedTab}">
            <div id="extended-tab" class="tile-body form-column js-scrollable hidden">
                <jsp:include page="tiles/mailing-extended-settings-tab-new-ux.jsp" />
            </div>
        </c:if>
    </mvc:form>

    <c:if test="${mailingId ne 0 and not isPostMailing}">
        <div class="tiles-block flex-column" style="flex: 1 1 45%">
            <div data-load='<c:url value="/mailing/preview/${mailingId}/view.action?pure=true"/>' data-load-replace data-load-target="#preview-form"></div>
        </div>
    </c:if>
</div>

<script id="edit-with-campaign-btn" type="text/x-mustache-template">
    <%@include file="fragments/edit-with-campaign-btn.jspf" %>
</script>
