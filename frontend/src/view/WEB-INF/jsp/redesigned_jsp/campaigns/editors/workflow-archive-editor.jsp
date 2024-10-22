<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="archive-editor" data-initializer="archive-editor-initializer">
    <mvc:form action="" id="archiveNodeForm" name="archiveNodeForm">
        <emm:ShowByPermission token="campaign.show">
        <div class="d-flex gap-1">
            <div class="w-100">
                <label for="settings_general_campaign" class="form-label"><mvc:message code="mailing.archive"/></label>
                <select id="settings_general_campaign" class="form-control js-select" name="campaignId" data-field="required">
                    <option value=""><mvc:message code="mailing.NoCampaign"/></option>
                    <c:forEach var="campaign" items="${campaigns}">
                        <option value="${campaign.id}">${campaign.shortname}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label class="form-label">&nbsp;</label>
                <button type="button" class="btn btn-icon btn-primary disable-for-active" data-action="archive-editor-new">
                    <i class="icon icon-plus"></i>
                </button>
            </div>
        </div>
        </emm:ShowByPermission>

        <div class="form-check form-switch mt-3">
            <input id="settings_general_in_archiv" type="checkbox" name="archived" value="true" class="form-check-input" role="switch"/>
            <label class="form-label form-check-label" for="settings_general_in_archiv">
                <mvc:message code="mailing.archived"/>
            </label>
        </div>
    </mvc:form>
</div>
