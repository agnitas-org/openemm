<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action" %>
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

<tiles:insertTemplate template="../template.jsp">
    <tiles:putAttribute name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:putAttribute>

    <tiles:putAttribute name="content" type="string">
        <div class="${isMailingGrid ? "tile-content-padded" : "row"}">
            <c:set var="mainBoxClass" value=""/>
            <c:set var="mainBoxViewModes" value=""/>

            <c:if test="${not isPostMailing}">
                <c:choose>
                    <c:when test="${isMailingGrid}">
                        <c:set var="mainBoxClass" value="col-xs-10 col-xs-push-1 col-md-8 col-md-push-2 col-lg-6 col-lg-push-3"/>
                        <c:set var="mainBoxViewModes" value=""/>
                    </c:when>
                    <c:when test="${mailingId ne 0}">
                        <c:set var="mainBoxClass" value="col-md-6 split-1-1"/>
                        <c:set var="mainBoxViewModes" value='data-view-block="col-md-12" data-view-split="col-md-6 split-1-1" data-view-hidden="col-xs-12"'/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="mainBoxClass" value=""/>
                        <c:set var="mainBoxViewModes" value='data-view-block="col-md-12" data-view-split="col-md-6 split-1-1" data-view-hidden="col-xs-12"'/>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <div id="mailing-main-wrapper" class="${mainBoxClass}" data-controller="mailing-settings-view" ${mainBoxViewModes}>
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
                <mvc:form servletRelativeAction="/mailing/${mailingId}/settings.action?isGrid=${isMailingGrid}&isTemplate=${isTemplate}" id="mailingSettingsForm" modelAttribute="mailingSettingsForm" data-form="resource" data-disable-controls="save"
                             data-form-focus="${not isPostMailing and not isMailingGrid and mailingId ne 0 ? '' : 'shortname'}" data-action="save">
                    <mvc:hidden path="parentId"/>
                    <emm:workflowParameters/>

                    <jsp:include page="tiles/mailing-settings-base-info-tile.jsp" />

                    <jsp:include page="tiles/mailing-settings-frame-tile.jsp" />

                    <script id="email-tile-template" type="text/html">
                        <jsp:include page="tiles/mailing-settings-email-tile.jsp" />
                    </script>

                    <jsp:include page="tiles/mailing-general-settings-tile.jsp" />
                    
                    <jsp:include page="tiles/mailing-settings-altg-tile.jsp" />
                    
                    <jsp:include page="tiles/mailing-settings-targets-tile.jsp" />

                    <jsp:include page="tiles/mailing-settings-mediatypes-tile.jsp" />

                    <emm:ShowByPermission token="mailing.parameter.show">
                        <jsp:include page="tiles/mailing-settings-parameter-tile.jsp" />
                    </emm:ShowByPermission>

                    <jsp:include page="tiles/mailing-settings-reference-content-tile.jsp" />
                </mvc:form>
            </div>
            
            <c:if test="${not isPostMailing and not isMailingGrid and mailingId ne 0}">
                <emm:ShowByPermission token="mailing.send.show">
                    <div id="c-wrapper" class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                        <c:url var="previewLink" value="/mailing/preview/${mailingId}/view.action">
                            <c:param name="pure" value="true"/>
                        </c:url>
                        <div data-load="<c:url value="/mailing/preview/${mailingId}/view.action?pure=true"/>" data-load-target="#preview"></div>
                    </div>
                </emm:ShowByPermission>
            </c:if>
        </div>
    </tiles:putAttribute>
    
    <tiles:putListAttribute name="footerItems">
        <tiles:addAttribute>
            <c:choose>
                <c:when test="${mailingId gt 0}">
                    <a href="<c:url value="/mailing/${mailingId}/confirmDelete.action"/>" class="btn btn-large pull-left" data-confirm=''>
                        <span class="text"><mvc:message code="button.Delete"/></span>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="#" class="btn btn-large pull-left" onclick="history.back()">
                        <span class="text"><mvc:message code="button.Back"/></span>
                    </a>
                </c:otherwise>
            </c:choose>
        </tiles:addAttribute>
        <emm:ShowByPermission token="mailing.change">
            <tiles:addAttribute>
                <button type="button" class="btn btn-large btn-primary pull-right" data-form-target='#mailingSettingsForm' data-form-submit-event data-controls-group="save">
                    <span class="text"><mvc:message code="button.Save"/></span>
                </button>
            </tiles:addAttribute>
        </emm:ShowByPermission>
    </tiles:putListAttribute>
</tiles:insertTemplate>
