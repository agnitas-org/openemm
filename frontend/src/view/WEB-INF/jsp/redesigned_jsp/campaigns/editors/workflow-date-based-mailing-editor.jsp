<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING_TYPE_DATEBASED" value="<%=MailingType.DATE_BASED.getCode()%>" scope="page"/>

<div id="datebased_mailing-editor" data-initializer="date-based-mailing-editor-initializer">

    <mvc:form action="" id="datebasedMailingForm" name="datebasedMailingForm">
        <jsp:include page="workflow-editor-mailing-select.jsp">
            <jsp:param name="containerId" value="datebased_mailing-editor"/>
            <jsp:param name="selectName" value="mailingId"/>
            <jsp:param name="statusName" value="mailings_status"/>
            <jsp:param name="baseMailingEditor" value="date-mailing-editor-base"/>
            <jsp:param name="status1" value="inactive"/>
            <jsp:param name="status2" value="active"/>
            <jsp:param name="showMailingLinks" value="true"/>
            <jsp:param name="message1" value="autoExport.statusNotActive"/>
            <jsp:param name="message2" value="default.status.active"/>
            <jsp:param name="disabledSelection" value="false"/>
        </jsp:include>

        <div class="security-notifications-settings flex-grow-1 mt-3">
            <div class="tile tile--sm">
                <div class="tile-header">
                    <div class="form-check form-switch">
                        <input type="checkbox" name="enableNotifications" id="datebased_mailing-editor_enable-notification" class="form-check-input" role="switch">
                        <label class="form-label form-check-label text-truncate" for="datebased_mailing-editor_enable-notification">
                            <mvc:message code="mailing.send.security.notification"/>
                        </label>
                    </div>
                </div>

                <div class="tile-body vstack gap-3 border-top p-3" data-show-by-checkbox="#datebased_mailing-editor_enable-notification">
                    <div>
                        <div class="form-check form-switch">
                            <input id="datebased_mailing-editor_enable-status-on-error" type="checkbox" class="form-check-input" name="enableNoSendCheckNotifications" role="switch">
                            <label class="form-label form-check-label text-truncate fw-normal" for="datebased_mailing-editor_enable-status-on-error">
                                <mvc:message code="mailing.SendStatusOnErrorOnly"/>
                                <a href="#" class="icon icon-question-circle" data-help="mailing/SendStatusOnErrorOnly.xml"></a>
                            </label>
                        </div>
                    </div>

                    <div>
                        <label for="datebased_mailing-editor_clearanceThreshold" class="form-label fw-normal">
                            <mvc:message code="mailing.autooptimization.threshold"/>
                            <a href="#" class="icon icon-question-circle" data-help="mailing/Threshold.xml"></a>
                        </label>

                        <input type="number" class="form-control" id="datebased_mailing-editor_clearanceThreshold" name="clearanceThreshold"
                               placeholder="<mvc:message code="mailing.send.threshold"/>">
                    </div>

                    <div>
                        <label for="datebased_mailing-editor_clearanceEmails" class="form-label fw-normal">
                            <mvc:message code="Recipients"/>
                            <a href="#" class="icon icon-question-circle" data-help="mailing/SendStatusEmail.xml"></a>
                        </label>

                        <select id="datebased_mailing-editor_clearanceEmails" name="clearanceEmails" class="form-control dynamic-tags" multiple=""
                                data-select-options="selectOnClose: true" data-placeholder="<mvc:message code='enterEmailAddresses'/>"></select>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>

    <script id="config:date-based-mailing-editor-initializer" type="application/json">
        {
            "form":"datebasedMailingForm",
            "container": "#datebased_mailing-editor",
            "mailingType": "${MAILING_TYPE_DATEBASED}",
            "selectName": "mailingId",
            "mailingStatus": "mailings_status",
            "showCreateEditLinks": ${not emm:permissionAllowed('mailing.content.readonly', pageContext.request)},
            "mailingTypesForLoading": ["${MAILING_TYPE_DATEBASED}"],
            "defaultMailingsSort": "active_sort_status asc, shortname",
            "defaultMailingsOrder": "asc"
        }
    </script>
</div>
