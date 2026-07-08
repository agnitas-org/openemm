<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.archive.forms.MailingArchiveForm"--%>

<c:set var="isWorkflowDriven" value="${(not empty workflowId and workflowId gt 0) or not empty workflowForwardParams}" />
<c:set var="isNewArchive" value="${form.id eq 0}" />

<mvc:message var="mailingDeleteMsg" scope="page" code="mailing.MailingDelete"/>
<c:set var="mailingDeleteAllowed" value="${emm:permissionAllowed('mailing.delete', pageContext.request)}" />
<c:url var="mailingDeleteLink" value="/mailing/deleteMailings.action">
    <c:param name="toPage" value="/mailing/archive/${form.id}/view.action"/>
</c:url>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg ${isNewArchive ? '' : 'modal-dialog-full-height'}">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <mvc:message code="${isNewArchive ? 'campaign.NewCampaign' : 'campaign.Edit'}"/>
                </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body vstack gap-3">
                <mvc:form id="archiveForm" cssClass="vstack gap-3" servletRelativeAction="/mailing/archive/save.action" modelAttribute="form"
                          data-form="resource" data-disable-controls="save">
                    <mvc:hidden path="id"/>

                    <div>
                        <mvc:message var="nameMsg" code="default.Name"/>
                        <label for="archive-name" class="form-label">${nameMsg} *</label>
                        <mvc:text path="shortname" cssClass="form-control" id="archive-name" maxlength="99" placeholder="${nameMsg}" data-field="required"/>
                    </div>

                    <div>
                        <mvc:message var="descriptionMsg" code="Description"/>
                        <label for="archive-description" class="form-label">${descriptionMsg}</label>
                        <mvc:textarea path="description" id="archive-description" cssClass="form-control" rows="1" placeholder="${descriptionMsg}"/>
                    </div>
                </mvc:form>

                <c:if test="${not isNewArchive}">
                    <mvc:form cssClass="table-wrapper flex-grow-1" servletRelativeAction="/mailing/archive/${form.id}/view.action" method="GET" modelAttribute="form">
                        <div class="table-wrapper__header justify-content-end">
                            <div class="table-wrapper__controls">
                                <c:if test="${mailingDeleteAllowed}">
                                    <div class="bulk-actions hidden">
                                        <p class="bulk-actions__selected">
                                            <span><%-- Updates by JS --%></span>
                                            <mvc:message code="default.list.entry.select" />
                                        </p>
                                        <div class="bulk-actions__controls">
                                            <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${mailingDeleteMsg}" data-form-url="${mailingDeleteLink}" data-form-confirm>
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </div>
                                    </div>
                                </c:if>

                                <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                                <jsp:include page="../common/table/entries-label.jsp">
                                    <jsp:param name="totalEntries" value="${mailingsList.fullListSize}"/>
                                </jsp:include>
                            </div>
                        </div>

                        <div class="table-wrapper__body">
                            <script type="application/json" data-initializer="web-storage-persist">
                                {
                                    "archive-mailings-overview": {
                                        "rows-count": ${form.numberOfRows}
                                    }
                                }
                             </script>

                            <emm:table var="archive_mailing" modelAttribute="mailingsList" cssClass="table table--borderless table-hover js-table">

                                <c:if test="${mailingDeleteAllowed}">
                                    <c:set var="checkboxSelectAll">
                                        <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                                    </c:set>

                                    <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                        <input class="form-check-input" type="checkbox" name="bulkIds" value="${archive_mailing.id}" autocomplete="off" data-bulk-checkbox />
                                    </emm:column>
                                </c:if>

                                <emm:column titleKey="Mailing"     sortable="true" sortProperty="mailing_name"        property="shortname" />
                                <emm:column titleKey="Description" sortable="true" sortProperty="mailing_description" property="description" />

                                <emm:column titleKey="Mailinglist" sortable="true" sortProperty="listname">
                                    <span>${archive_mailing.mailinglist.shortname}</span>
                                    <a href="<c:url value="/mailing/${archive_mailing.id}/settings.action"/>" class="hidden" data-view-row="page"></a>
                                </emm:column>

                                <emm:column titleKey="mailing.senddate" property="senddate" sortable="true" />

                                <c:if test="${mailingDeleteAllowed and mailingsList.fullListSize gt 0}">
                                    <emm:column>
                                        <a href="${mailingDeleteLink}&bulkIds=${archive_mailing.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${mailingDeleteMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </emm:column>
                                </c:if>
                            </emm:table>
                        </div>
                    </mvc:form>
                </c:if>
            </div>
            <emm:ShowByPermission token="campaign.change">
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary"
                            data-controls-group="save"
                            data-form-target="#archiveForm"
                            ${isWorkflowDriven ? 'data-confirm-positive' : 'data-form-submit'}>
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </emm:ShowByPermission>
        </div>
    </div>
</div>
