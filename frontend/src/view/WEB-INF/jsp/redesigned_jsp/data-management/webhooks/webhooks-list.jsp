<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>

<mvc:form id="webhooks-form" cssClass="tiles-container" servletRelativeAction="/webhooks/enableInterface.action" modelAttribute="enableInterfaceForm" method="POST">
    <div class="tile">
        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <emm:ShowByPermission token="webhooks.enable">
                            <div class="form-check form-switch">
                                <mvc:checkbox cssClass="form-check-input" role="switch" id="enableWebhooksInterface" path="enable" value="true" />
                                <label class="form-label form-check-label" for="enableWebhooksInterface"><mvc:message code="webhooks.interface.enable"/></label>
                            </div>
                        </emm:ShowByPermission>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${fn:length(WEBHOOKS)}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="webhook" modelAttribute="WEBHOOKS" cssClass="table table-hover table--borderless js-table">
                        <emm:column titleKey="webhooks.event">
                            <span><mvc:message code="webhooks.event.${webhook.eventType}.label" /></span>
                            <a href="<c:url value="/webhooks/${webhook.eventType}/view.action" />" class="hidden" data-view-row></a>
                        </emm:column>

                        <emm:column titleKey="webhooks.url"           property="url" />
                        <emm:column titleKey="webhooks.profilefields" property="profilefieldsAsString" />
                    </emm:table>
                </div>
                <div class="table-wrapper__footer"></div>
            </div>
        </div>
    </div>
</mvc:form>
