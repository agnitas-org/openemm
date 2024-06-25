<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>

<%@ page import="org.agnitas.util.AgnUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.corFe.components.form.UpdateMailingAttachmentsForm"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="attachments" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<mvc:message var="deleteTooltipMsg" code="mailing.attachment.delete"/>
<mvc:message var="downloadTooltipMsg" code="button.Download" />

<div class="tiles-container d-flex hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/${mailing.id}/attachment/list.action" modelAttribute="form"
              cssClass="tile" cssStyle="flex: 3" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-rounded js-table" id="attachment" list="${form.attachments}"
                                   requestURI="/mailing/${mailingId}/attachment/list.action" excludedParams="*" length="${fn:length(form.attachments)}" sort="list">

                        <c:set var="noNumberOfRowsSelect" value="true" />
                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" property="name"/>
                        <display:column headerClass="js-table-sort" sortable="true" titleKey="Target" sortProperty="targetId">
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
                        </display:column>

                        <display:column headerClass="js-table-sort fit-content" sortable="true" titleKey="Original_Size" sortProperty="originalSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.originalSize)} kB</span>
                        </display:column>

                        <display:column headerClass="js-table-sort fit-content" sortable="true" titleKey="default.Size_Mail" sortProperty="emailSize">
                            <span>${AgnUtils.bytesToKbStr(attachment.emailSize)} kB</span>
                        </display:column>

                        <display:column headerClass="js-table-sort fit-content" sortable="true" titleKey="Mime_Type" sortProperty="mimeType">
                            <span class="text-uppercase pill-badge">${attachment.mimeType}</span>
                        </display:column>

                        <c:if test="${fn:length(form.attachments) gt 0}">
                            <display:column headerClass="fit-content" class="table-actions">
                                <div>
                                    <c:if test="${isMailingEditable}">
                                        <c:url var="confirmDeleteLink" value="/mailing/${mailing.id}/attachment/${attachment.id}/deleteRedesigned.action"/>
                                        <a href="${confirmDeleteLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deleteTooltipMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>

                                    <c:url var="imageLinkNoCache" value="/dc?compID=${attachment.id}"/>
                                    <a href="${imageLinkNoCache}" class="btn btn-icon-sm btn-primary" data-prevent-load="" download="${attachment.name}" data-tooltip="${downloadTooltipMsg}">
                                        <i class="icon icon-file-download"></i>
                                    </a>
                                </div>
                            </display:column>
                        </c:if>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="new-attachment-tile" servletRelativeAction="/mailing/${mailing.id}/attachment/upload.action" enctype="multipart/form-data"
              cssClass="tile" cssStyle="flex: 1" modelAttribute="uploadMailingAttachmentForm" data-form="resource" data-custom-loader=""
              data-controller="mailing-upload-attachment" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="New_Attachment"/></h1>
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
