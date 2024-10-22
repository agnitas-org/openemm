<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ page import="org.agnitas.util.AgnUtils" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.corFe.components.form.UpdateMailingAttachmentsForm"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="attachments" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.components.dto.MailingAttachmentDto>"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<mvc:message var="deleteTooltipMsg"   code="mailing.attachment.delete" />
<mvc:message var="downloadTooltipMsg" code="button.Download" />

<c:url var="deleteUrl" value="/mailing/${mailing.id}/attachment/deleteRedesigned.action" />

<div class="tiles-container" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/${mailing.id}/attachment/list.action" modelAttribute="form"
              cssClass="tile" cssStyle="flex: 3" data-editable-tile="main">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mailing-attachments-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <div class="bulk-actions hidden">
                            <p class="bulk-actions__selected">
                                <span><%-- Updates by JS --%></span>
                                <mvc:message code="default.list.entry.select" />
                            </p>
                            <div class="bulk-actions__controls">
                                <c:url var="bulkDownloadUrl" value="/mailing/${mailing.id}/attachment/bulk/download.action"/>
                                <a href="#" class="icon-btn text-primary" data-tooltip="${downloadTooltipMsg}" data-form-url="${bulkDownloadUrl}" data-form-method="GET" data-prevent-load data-form-submit-static>
                                    <i class="icon icon-file-download"></i>
                                </a>
                                <c:if test="${isMailingEditable}">
                                    <a href="#" class="icon-btn text-danger" data-tooltip="${deleteTooltipMsg}" data-form-url="${deleteUrl}" data-form-method="GET" data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </c:if>
                            </div>
                        </div>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${attachments.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <agnDisplay:table class="table table--borderless js-table" id="attachment" name="attachments"
                                   requestURI="/mailing/${mailingId}/attachment/list.action" excludedParams="*" pagesize="${form.numberOfRows}">

                        <%--@elvariable id="attachment" type="com.agnitas.emm.core.components.dto.MailingAttachmentDto"--%>

                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                        </c:set>

                        <agnDisplay:column title="${checkboxSelectAll}" class="mobile-hidden" headerClass="mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${attachment.id}" data-bulk-checkbox />
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" sortProperty="name">
                            <span>${attachment.name}</span>
                        </agnDisplay:column>
                        <agnDisplay:column headerClass="js-table-sort" sortable="true" titleKey="Target" sortProperty="targetId">
                            <%-- NOTE: retrieves current row index from display tag lib --%>
                            <c:set var="rowIndex" value="${attachment_rowNum - 1}" />
                            <mvc:hidden path="attachments[${rowIndex}].id"/>
                            <mvc:hidden path="attachments[${rowIndex}].name"/>

                            <mvc:select path="attachments[${rowIndex}].targetId" cssClass="form-control js-select" disabled="${not isMailingEditable}">
                                <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                <c:forEach items="${targetGroups}" var="target">
                                    <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort fit-content" sortable="true" titleKey="Original_Size" sortProperty="originalSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.originalSize)} kB</span>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort fit-content" sortable="true" titleKey="default.Size_Mail" sortProperty="emailSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.emailSize)} kB</span>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort fit-content" sortable="true" titleKey="Mime_Type" sortProperty="mimeType">
                            <span class="text-uppercase table-badge">${attachment.mimeType}</span>
                        </agnDisplay:column>

                        <c:if test="${fn:length(form.attachments) gt 0}">
                            <agnDisplay:column headerClass="fit-content" class="table-actions">
                                <div>
                                    <c:if test="${isMailingEditable}">
                                        <a href="${deleteUrl}?bulkIds=${attachment.id}" class="icon-btn text-danger js-row-delete" data-tooltip="${deleteTooltipMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>

                                    <c:url var="imageLinkNoCache" value="/dc?compID=${attachment.id}"/>
                                    <a href="${imageLinkNoCache}" class="icon-btn text-primary" data-prevent-load="" download="${attachment.name}" data-tooltip="${downloadTooltipMsg}">
                                        <i class="icon icon-file-download"></i>
                                    </a>
                                </div>
                            </agnDisplay:column>
                        </c:if>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="new-attachment-tile" servletRelativeAction="/mailing/${mailing.id}/attachment/upload.action" enctype="multipart/form-data"
              cssClass="tile" modelAttribute="uploadMailingAttachmentForm" data-form="resource" data-custom-loader=""
              data-controller="mailing-upload-attachment" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="New_Attachment"/></h1>
        </div>

        <div class="tile-body">
            <div class="row g-3" data-field="toggle-vis">
                <%@include file="fragments/mailing-attachments-uploaded-pdf.jspf" %>

                <div class="col-12">
                    <label for="attachmentFile" class="form-label"><mvc:message code="mailing.Attachment"/></label>
                    <input id="attachmentFile" type="file" name="attachment" class="form-control" data-action="change-attachment-file" ${isMailingEditable ? '' : 'disabled'}>
                </div>

                <div class="col-12">
                    <label for="attachmentName" class="form-label"><mvc:message code="attachment.name"/></label>
                    <mvc:text path="attachmentName" id="attachmentName" cssClass="form-control" disabled="${not isMailingEditable}"/>
                </div>

                <%@include file="fragments/mailing-attachments-types.jspf" %>

                <div class="col-12">
                    <label for="targetId" class="form-label"><mvc:message code="Target"/></label>
                    <mvc:select path="targetId" id="targetId" cssClass="form-control js-select" disabled="${not isMailingEditable}">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <c:forEach items="${targetGroups}" var="target">
                            <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <button type="button" tabindex="-1" class="btn btn-primary w-100" ${isMailingEditable ? 'data-form-submit' : 'disabled'}>
                        <i class="icon icon-plus"></i>
                        <span class="text"><mvc:message code="button.Add"/></span>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
