<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="actions" type="java.util.List<java.util.Map>"--%>
<%--@elvariable id="action" type="java.util.Map"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">${mailingShortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <div class="table-wrapper">
                    <div class="table-wrapper__header justify-content-end">
                        <div class="table-wrapper__controls">
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="totalEntries" value="${fn:length(actions)}"/>
                            </jsp:include>
                        </div>
                    </div>
                    <div class="table-wrapper__body">
                        <agnDisplay:table class="table table--borderless table-hover js-table" id="action" name="${actions}">
                            <c:set var="noNumberOfRowsSelect" value="true" />
                            <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                            <agnDisplay:column titleKey="action.Action" headerClass="js-table-sort">
                                <span>${action["action_name"]}</span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="mailing.URL" headerClass="js-table-sort">
                                <span>${action["url"]}</span>
                                <a href="<c:url value="/action/${action['action_id']}/view.action"/>" class="hidden" data-view-row="page"></a>
                            </agnDisplay:column>
                        </agnDisplay:table>
                    </div>
                    <div class="table-wrapper__footer"></div>
                </div>
            </div>
        </div>
    </div>
</div>
