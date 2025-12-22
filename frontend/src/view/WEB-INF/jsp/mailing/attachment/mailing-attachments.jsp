<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.util.AgnUtils" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.corFe.components.form.UpdateMailingAttachmentsForm"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="attachments" type="com.agnitas.beans.PaginatedList<com.agnitas.emm.core.components.dto.MailingAttachmentDto>"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<mvc:message var="deleteTooltipMsg"   code="mailing.attachment.delete" />
<mvc:message var="downloadTooltipMsg" code="button.Download" />

<c:url var="deleteUrl" value="/mailing/${mailing.id}/attachment/delete.action" />

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
                                <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${downloadTooltipMsg}" data-form-url="${bulkDownloadUrl}" data-form-method="GET" data-prevent-load data-form-submit-static>
                                    <i class="icon icon-file-download"></i>
                                </a>
                                <c:if test="${isMailingEditable}">
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteTooltipMsg}" data-form-url="${deleteUrl}" data-form-method="GET" data-form-confirm>
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
                    <emm:table var="attachment" modelAttribute="attachments" cssClass="table table--borderless js-table">

                        <%--@elvariable id="attachment" type="com.agnitas.emm.core.components.dto.MailingAttachmentDto"--%>
                        <%--@elvariable id="attachment_index" type="java.lang.Integer"--%>

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                        </c:set>

                        <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${attachment.id}" autocomplete="off" data-bulk-checkbox />
                        </emm:column>

                        <emm:column sortable="true" titleKey="default.Name" property="name" />

                        <emm:column sortable="true" titleKey="Target" sortProperty="targetId">
                            <mvc:hidden path="attachments[${attachment_index}].id"/>
                            <mvc:hidden path="attachments[${attachment_index}].name"/>

                            <mvc:select path="attachments[${attachment_index}].targetId" cssClass="form-control" disabled="${not isMailingEditable}">
                                <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                <c:forEach items="${targetGroups}" var="target">
                                    <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </emm:column>

                        <emm:column headerClass="fit-content" sortable="true" titleKey="Original_Size" sortProperty="originalSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.originalSize)} kB</span>
                        </emm:column>

                        <emm:column headerClass="fit-content" sortable="true" titleKey="default.Size_Mail" sortProperty="emailSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.emailSize)} kB</span>
                        </emm:column>

                        <emm:column headerClass="fit-content" sortable="true" titleKey="Mime_Type" sortProperty="mimeType">
                            <span class="text-uppercase table-badge">${attachment.mimeType}</span>
                        </emm:column>

                        <c:if test="${fn:length(form.attachments) gt 0}">
                            <emm:column cssClass="table-actions">
                                <div>
                                    <c:if test="${isMailingEditable}">
                                        <a href="${deleteUrl}?bulkIds=${attachment.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteTooltipMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>

                                    <c:url var="imageLinkNoCache" value="/dc?compID=${attachment.id}"/>
                                    <a href="${imageLinkNoCache}" class="icon-btn icon-btn--primary" data-prevent-load="" download="${attachment.name}" data-tooltip="${downloadTooltipMsg}">
                                        <i class="icon icon-file-download"></i>
                                    </a>
                                </div>
                            </emm:column>
                        </c:if>
                    </emm:table>
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

        <div class="tile-body vstack gap-3 js-scrollable" data-field="toggle-vis">
            <%@include file="fragments/mailing-attachments-uploaded-pdf.jspf" %>

            <div>
                <label for="attachmentFile" class="form-label"><mvc:message code="mailing.Attachment"/></label>
                <input id="attachmentFile" type="file" name="attachment" class="form-control" data-action="change-attachment-file" ${isMailingEditable ? '' : 'disabled'}>
            </div>

            <div>
                <label for="attachmentName" class="form-label"><mvc:message code="attachment.name"/></label>
                <mvc:text path="attachmentName" id="attachmentName" cssClass="form-control" disabled="${not isMailingEditable}"/>
            </div>

            <%@include file="fragments/mailing-attachments-types.jspf" %>

            <div>
                <label for="targetId" class="form-label"><mvc:message code="Target"/></label>
                <mvc:select path="targetId" id="targetId" cssClass="form-control" disabled="${not isMailingEditable}">
                    <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                    <c:forEach items="${targetGroups}" var="target">
                        <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <button type="button" tabindex="-1" class="btn btn-primary w-100" ${isMailingEditable ? 'data-form-submit' : 'disabled'}>
                <i class="icon icon-plus"></i>
                <span class="text"><mvc:message code="button.Add"/></span>
            </button>
        </div>
    </mvc:form>
</div>
