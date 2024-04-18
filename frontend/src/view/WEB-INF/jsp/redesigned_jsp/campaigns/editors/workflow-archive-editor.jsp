<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="FORWARD_ARCHIVE_CREATE" value="<%= WorkflowController.FORWARD_ARCHIVE_CREATE%>" scope="page"/>

<div id="archive-editor" data-initializer="archive-editor-initializer">
    <mvc:form action="" id="archiveForm" name="archiveForm">
        <emm:ShowByPermission token="campaign.show">
            <label for="settings_general_campaign" class="form-label"><mvc:message code="mailing.archive"/></label>
            <div class="d-flex gap-1">
                <select id="settings_general_campaign" class="form-control js-select" name="campaignId">
                    <c:forEach var="campaign" items="${campaigns}">
                        <option value="${campaign.id}">${campaign.shortname}</option>
                    </c:forEach>
                </select>
                <a href="#" class="btn btn-icon-sm btn-primary disable-for-active" data-action="archive-editor-new">
                    <i class="icon icon-plus"></i>
                </a>
            </div>
        </emm:ShowByPermission>

        <div class="form-check form-switch mt-3">
            <input id="settings_general_in_archiv" type="checkbox" name="archived" value="true" class="form-check-input" role="switch"/>
            <label class="form-label form-check-label" for="settings_general_in_archiv">
                <mvc:message code="mailing.archived"/>
            </label>
        </div>
        
<%--                    <a href="#" class="btn btn-regular" data-action="editor-cancel">--%>
<%--                        <span><mvc:message code="button.Cancel"/></span>--%>
<%--                    </a>--%>
<%--                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="editor-save-current">--%>
<%--                        <span><mvc:message code="button.Apply"/></span>--%>
<%--                    </a>--%>
    </mvc:form>
</div>
