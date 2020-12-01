<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.*, com.agnitas.web.*, org.agnitas.beans.*" %>
<%@ page import="com.agnitas.emm.core.report.enums.fields.MailingTypes" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:set var="ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>
<c:set var="ACTION_CONFIRM_UNDO" value="<%= ComMailingBaseAction.ACTION_CONFIRM_UNDO %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>" />
<c:set var="ACTION_PREVIEW_SELECT" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_SELECT%>"/>
<c:set var="ACTION_RECIPIENTS_CALCULATE" value="<%= ComMailingBaseAction.ACTION_RECIPIENTS_CALCULATE %>"/>

<c:set var="MAILING_COMPONENT_TYPE_THUMBNAIL_IMAGE" value="<%= MailingComponentType.ThumbnailImage.getCode() %>"/>

<c:set var="TYPE_FOLLOWUP" value="<%= MailingTypes.FOLLOW_UP.getCode() %>"/>
<c:set var="TYPE_INTERVAL" value="<%= MailingTypes.INTERVAL.getCode() %>"/>
<c:set var="TYPE_ACTIONBASED" value="<%= MailingTypes.ACTION_BASED.getCode() %>"/>
<c:set var="TYPE_DATEBASED" value="<%= MailingTypes.DATE_BASED.getCode() %>"/>

<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND %>"/>

<c:set var="isMailingGrid" value="${mailingBaseForm.isMailingGrid}" scope="request"/>
<c:set var="editWithCampaignManagerMessage" scope="request"><bean:message key='mailing.EditWithCampaignManager'/></c:set>

<c:set var="mailingFollowUpAllowed" value="${false}"/>
<%@include file="mailing-view-base-follow.jspf" %>

<c:set var="formWorkflowId" value="${mailingBaseForm.workflowId}"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, mailingBaseForm.workflowId)}" scope="request"/>
<c:set var="workflowId" value="${workflowParams.workflowId}"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}"/>
<c:url var="editWithCampaignManagerLink" value="/workflow/${workflowId}/view.action">
    <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}"/>
</c:url>

<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <%--<div class="headline">
                <i class="icon icon-th-list"></i>
            </div>--%>

            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:putList name="footerItems">
        <tiles:add>
            <c:choose>
                <c:when test="${mailingBaseForm.mailingID gt 0}">
                    <a href="<html:rewrite page="/mailingbase.do?action=${ACTION_CONFIRM_DELETE}&previousAction=${ACTION_VIEW}&mailingID=${mailingBaseForm.mailingID}"/>" class="btn btn-large pull-left" data-confirm=''>
                        <span class="text">
                            <bean:message key="button.Delete"/>
                        </span>
                            <%--<i class="icon icon-trash-o"></i>--%>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="#" class="btn btn-large pull-left" onclick="history.back()">
                        <span class="text">
                            <bean:message key="button.Back"/>
                        </span>
                    </a>
                </c:otherwise>
            </c:choose>

        </tiles:add>

        <tiles:add>
            <button type="button" class="btn btn-large btn-primary pull-right" data-form-target='#mailingBaseForm' data-form-set='save:save' data-form-submit="" data-controls-group="save">
                <span class="text">
                    <bean:message key="button.Save"/>
                </span>
                    <%--<i class="icon icon-save"></i>--%>
            </button>
        </tiles:add>
    </tiles:putList>

    <tiles:put name="content" type="string">
        <div class="${isMailingGrid ? "tile-content-padded" : "row"}">
            <c:set var="mainBoxClass" value=""/>
            <c:set var="mainBoxViewModes" value=""/>

            <c:if test="${not isPostMailing}">
                <c:choose>
                    <c:when test="${isMailingGrid}">
                        <c:set var="mainBoxClass" value="col-xs-10 col-xs-push-1 col-md-8 col-md-push-2 col-lg-6 col-lg-push-3"/>
                        <c:set var="mainBoxViewModes" value=""/>
                    </c:when>
                    <c:when test="${mailingBaseForm.mailingID ne 0}">
                        <c:set var="mainBoxClass" value="col-md-6 split-1-1"/>
                        <c:set var="mainBoxViewModes" value='data-view-block="col-md-12" data-view-split="col-md-6 split-1-1" data-view-hidden="col-xs-12"'/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="mainBoxClass" value=""/>
                        <c:set var="mainBoxViewModes" value='data-view-block="col-md-12" data-view-split="col-md-6 split-1-1" data-view-hidden="col-xs-12"'/>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <div class="${mainBoxClass}" data-controller="mailing-view-base" ${mainBoxViewModes}>
                <script data-initializer="mailing-view-base" type="application/json">
                    {
                        "mailingId": ${mailingBaseForm.mailingID},
                        "TARGET_MODE_AND": ${TARGET_MODE_AND},
                        "TARGET_MODE_OR": ${TARGET_MODE_OR},
                        "TYPE_FOLLOWUP": ${TYPE_FOLLOWUP},
                        "TYPE_INTERVAL": ${TYPE_INTERVAL},
                        "TYPE_ACTIONBASED": ${TYPE_ACTIONBASED},
                        "TYPE_DATEBASED": ${TYPE_DATEBASED},
                        "urls": {
                            "MAILINGBASE": "<c:url value="/mailingbase.do"/>"
                        },
                        "actions": {
                            "ACTION_RECIPIENTS_CALCULATE": ${ACTION_RECIPIENTS_CALCULATE}
                        },
                        "followUpAllowed": ${mailingFollowUpAllowed},
                        "isWorkflowDriven": ${isWorkflowDriven},
                        "mailingType": ${mailingBaseForm.mailingType}
                    }
                </script>
                <agn:agnForm action="/mailingbase" id="mailingBaseForm" data-form="resource" data-disable-controls="save"
                             data-form-focus="${not isPostMailing and not isMailingGrid and mailingBaseForm.mailingID ne 0 ? '' : 'shortname'}">
                    <html:hidden property="mailingID"/>
                    <html:hidden property="action"/>
                    <html:hidden property="isTemplate"/>
                    <html:hidden property="oldMailingID"/>
                    <html:hidden property="copyFlag"/>
                    <html:hidden property="isMailingGrid"/>
                    <html:hidden property="gridTemplateId"/>

                    <emm:workflowParameters/>

                    <div class="tile" data-action="scroll-to">
                        <div class="tile-header">
                            <a href="#" class="headline" data-toggle-tile="#tile-mailingBase">
                                <i class="tile-toggle icon icon-angle-up"></i>
                                <bean:message key="mailing.base.information"/>
                            </a>
                        </div>
                        <div id="tile-mailingBase" class="tile-content tile-content-forms">

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="mailingShortname">
                                        <bean:message key="default.Name"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <html:text styleId="mailingShortname" styleClass="form-control" property="shortname" maxlength="99"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="mailingDescription">
                                        <bean:message key="default.description"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <html:textarea styleId="mailingDescription" styleClass="form-control v-resizable" property="description" rows="5" cols="32"/>
                                </div>
                            </div>

                            <c:if test="${not mailingBaseForm.isTemplate}">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="mailingPlanDate">
                                            <bean:message key="mailing.plan.date"/>
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <c:set var="isReadonlyDate" value="${mailingBaseForm.worldMailingSend || isWorkflowDriven}"/>
                                            <div class="input-group-controls">
                                                <input type="text" name="planDate" value="${mailingBaseForm.planDate}" id="mailingPlanDate" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}'" ${isReadonlyDate ? "disabled='disabled'" : ""}/>
                                            </div>
                                            <div class="input-group-btn">
                                                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" ${isReadonlyDate ? "disabled='disabled'" : ""}>
                                                    <i class="icon icon-calendar-o"></i>
                                                </button>

                                                <c:if test="${isWorkflowDriven}">
                                                    <a href="${editWithCampaignManagerLink}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                                        <i class="icon icon-linkage-campaignmanager"></i>
                                                        <strong><bean:message key="campaign.manager.icon"/></strong>
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${isEnableTrackingVeto}">
	                            <div class="form-group">
	                                <div class="col-sm-4">
	                                    <label class="control-label" for="mailingContentTypeAdvertising">
	                                        <bean:message key="mailing.contentType.advertising"/>
	                                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/AdvertisingMsg.xml" tabindex="-1" type="button"></button>
	                                    </label>
	                                </div>
	                                <div class="col-sm-8">
		                				<html:hidden property="__STRUTS_CHECKBOX_mailingContentTypeAdvertising" value="false" />
		  								<label class="toggle">
		  									<html:checkbox property="mailingContentTypeAdvertising" styleId="mailingContentTypeAdvertising"/>
		                          			<div class="toggle-control"></div>
		  								</label>
	                                </div>
	                            </div>
                            </c:if>

                            <%@include file="./mailing-frequency-toggle.jspf" %>

                            <c:if test="${isMailingGrid}">
                                <jsp:include page="/WEB-INF/jsp/mailing/grid/mailing-grid-notes.jsp"/>
                            </c:if>

                        </div>

                    </div>

                    <c:if test="${mailingBaseForm.useMediaEmail}">
                        <jsp:include page="/WEB-INF/jsp/mailing/media/email.jsp"/>
                    </c:if>

                    <jsp:include page="/WEB-INF/jsp/mailing/view_base_settings.jsp"/>

                    <jsp:include page="/WEB-INF/jsp/mailing/mediatypes.jsp"/>

                    <emm:ShowByPermission token="mailing.parameter.show">
                        <jsp:include page="/WEB-INF/jsp/mailing/parameter/parameter.jsp"/>
                    </emm:ShowByPermission>

                    <%@include file="mailing-reference-content-tile.jspf" %>

                    <jsp:include page="/WEB-INF/jsp/mailing/interval.jsp"/>
                </agn:agnForm>
            </div>

            <c:if test="${not isPostMailing and not isMailingGrid and mailingBaseForm.mailingID ne 0}">
                <emm:ShowByPermission token="mailing.send.show">
                    <div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                        <div data-load="<html:rewrite page="/mailingsend.do?action=${ACTION_PREVIEW_SELECT}&mailingID=${mailingBaseForm.mailingID}&previewForm.pure=true"/>" data-load-target="#preview"></div>
                    </div>
                </emm:ShowByPermission>
            </c:if>

        </div>
    </tiles:put>
</tiles:insert>
