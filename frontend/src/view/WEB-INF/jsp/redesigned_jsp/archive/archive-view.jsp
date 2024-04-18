<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.archive.forms.MailingArchiveForm"--%>

<c:set var="isWorkflowDriven" value="${not empty workflowId and workflowId > 0}" />
<c:set var="isNewArchive" value="${form.id eq 0}" />
<mvc:message var="mailingDeleteMessage" scope="page" code="mailing.MailingDelete"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered ${isNewArchive ? '' : 'modal-dialog-full-height'}">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <mvc:message code="${isNewArchive ? 'campaign.NewCampaign' : 'campaign.Edit'}"/>
                </h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="d-flex flex-column gap-3 h-100">
                    <mvc:form id="archiveForm" cssClass="row g-3" servletRelativeAction="/mailing/archive/save.action" modelAttribute="form"
                              data-form="${isWorkflowDriven ? 'static' : 'resource'}" data-disable-controls="save">
                        <mvc:hidden path="id"/>

                        <div class="col-12">
                            <label for="archive-name" class="form-label">
                                <mvc:message var="nameMsg" code="default.Name"/>
                                ${nameMsg} *
                            </label>
                            <mvc:text path="shortname" cssClass="form-control" id="archive-name" maxlength="99" placeholder="${nameMsg}" data-field="required"/>
                        </div>

                        <div class="col-12">
                            <label for="archive-description" class="form-label">
                                <mvc:message var="descriptionMsg" code="Description"/>
                                ${descriptionMsg}
                            </label>

                            <mvc:textarea path="description" id="archive-description" cssClass="form-control v-resizable" rows="3" placeholder="${descriptionMsg}"/>
                        </div>
                    </mvc:form>

                    <c:if test="${not isNewArchive}">
                        <mvc:form cssClass="table-box flex-grow-1" servletRelativeAction="/mailing/archive/${form.id}/view.action" method="GET" modelAttribute="form">
                            <div class="table-scrollable">
                                <script type="application/json" data-initializer="web-storage-persist">
                                    {
                                        "archive-mailings-overview": {
                                            "rows-count": ${form.numberOfRows}
                                        }
                                    }
                                 </script>

                                <display:table id="archive_mailing" class="table table-rounded table-hover js-table"
                                               pagesize="${mailingsList.pageSize}" sort="external" name="mailingsList" requestURI="/mailing/archive/${form.id}/view.action"
                                               partialList="true" size="${mailingsList.fullListSize}" excludedParams="*">

                                    <%@ include file="../displaytag/displaytag-properties.jspf" %>

                                    <display:column headerClass="js-table-sort" titleKey="Mailing" sortable="true"
                                                    property="shortname" sortProperty="mailing_name"/>
                                    <display:column headerClass="js-table-sort" titleKey="Description" sortable="true"
                                                    property="description" sortProperty="mailing_description"/>
                                    <display:column headerClass="js-table-sort" titleKey="Mailinglist" sortable="true"
                                                    property="mailinglist.shortname" sortProperty="listname"/>

                                    <display:column headerClass="js-table-sort" titleKey="mailing.senddate" sortable="true"
                                                    format="{0,date,${localeTablePattern}}" property="senddate" sortProperty="senddate"/>

                                    <display:column headerClass="fit-content">
                                        <a href="<c:url value="/mailing/${archive_mailing.id}/settings.action"/>" class="hidden" data-view-row="page"></a>

                                        <emm:ShowByPermission token="mailing.delete">
                                            <c:url var="deletionLink" value="/mailing/deleteMailings.action">
                                                <c:param name="bulkIds" value="${archive_mailing.id}"/>
                                                <c:param name="toPage" value="/mailing/archive/${form.id}/view.action"/>
                                            </c:url>

                                            <a href="${deletionLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${mailingDeleteMessage}">
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </emm:ShowByPermission>
                                    </display:column>
                                </display:table>
                            </div>
                        </mvc:form>
                    </c:if>
                </div>
            </div>
            <emm:ShowByPermission token="campaign.change">
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary flex-grow-1" data-controls-group="save" data-form-target="#archiveForm"
                        ${not empty workflowForwardParams ? 'data-form-submit-static' : 'data-form-submit'}>
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </emm:ShowByPermission>
        </div>
    </div>
</div>
