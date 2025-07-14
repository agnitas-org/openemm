<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>

<%@ page import="com.agnitas.util.AgnUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.corFe.components.form.UpdateMailingAttachmentsForm"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="attachments" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<tiles:insertTemplate template="/WEB-INF/jsp/mailing/template.jsp">
    <c:if test="${isMailingGrid}">
        <tiles:putAttribute name="header" type="string">
            <ul class="tile-header-nav">
                <!-- Tabs BEGIN -->
                <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                <!-- Tabs END -->
            </ul>
        </tiles:putAttribute>
    </c:if>

    <tiles:putAttribute name="content" type="string">
        <c:set var="uploadFormContent">
            <mvc:form servletRelativeAction="/mailing/${mailing.id}/attachment/upload.action"
                      id="mailing-upload-attachments-form" cssClass="form-vertical"
                      enctype="multipart/form-data" data-form="resource"
                      data-custom-loader=""
                      modelAttribute="uploadMailingAttachmentForm"
                      data-controller="mailing-upload-attachment"
                      data-initializer="mailing-upload-attachment">

                <div class="tile">
                    <div class="tile-header">
                        <a href="#" class="headline" data-toggle-tile="#tile-attachmentUpload">
                            <i class="tile-toggle icon icon-angle-up"></i>
                            <mvc:message code="New_Attachment"/>
                        </a>
                    </div>

                    <div id="tile-attachmentUpload" class="tile-content tile-content-forms" data-field="toggle-vis">
                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="attachment" class="form-label">
                                        <mvc:message code="mailing.Attachment"/>
                                    </label>
                                    <input type="file" name="attachment" id="attachment"
                                           class="form-control" ${isMailingEditable ? 'data-upload="" data-action="change-attachment-file"' : 'disabled'}>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="attachmentName" class="form-label">
                                        <mvc:message code="attachment.name"/>
                                    </label>
                                    <mvc:text path="attachmentName" id="attachmentName" cssClass="form-control" disabled="${not isMailingEditable}"/>
                                </div>
                            </div>
                            <%@include file="mailing-attachments-types.jsp" %>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="targetId" class="form-label">
                                        <mvc:message code="Target"/>
                                    </label>
                                    <mvc:select path="targetId" id="targetId" cssClass="form-control js-select" disabled="${not isMailingEditable}">
                                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                        <c:forEach items="${targetGroups}" var="target">
                                            <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                                        </c:forEach>
                                    </mvc:select>
                                </div>
                            </div>
                            <%@include file="mailing-attachments-uploaded-pdf.jspf" %>
                            <div class="col-xs-12">
                                <div class="form-group">
                                    <div class="btn-group">
                                        <button type="button" tabindex="-1" class="btn btn-regular btn-primary" ${isMailingEditable ? 'data-form-submit' : 'disabled'}>
                                            <i class="icon icon-cloud-upload"></i>
                                            <span class="text"><mvc:message code="button.Add"/></span>
                                        </button>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>

                </div>
            </mvc:form>
        </c:set>
        <c:set var="editFormContent">
            <mvc:form servletRelativeAction="/mailing/${mailing.id}/attachment/list.action"
                      id="save-attachments-form"
                      modelAttribute="form" data-resource-selector="#save-attachments-form"
                      data-form="resource">

                <script type="application/json" data-initializer="web-storage-persist">
                    {
                        "mailing-attachments-overview": {
                            "rows-count": ${form.numberOfRows}
                        }
                    }
                </script>

                <div class="tile">
                    <div class="tile-header">
                        <ul class="tile-header-actions">
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <i class="icon icon-eye"></i>
                                    <span class="text"><mvc:message code="button.Show"/></span>
                                    <i class="icon icon-caret-down"></i>
                                </a>
                                <ul class="dropdown-menu">
                                    <li class="dropdown-header"><mvc:message code="listSize"/></li>
                                    <li>
                                        <label class="label">
                                            <mvc:radiobutton path="numberOfRows" value="20"/>
                                            <span class="label-text">20</span>
                                        </label>
                                        <label class="label">
                                            <mvc:radiobutton path="numberOfRows" value="50"/>
                                            <span class="label-text">50</span>
                                        </label>
                                        <label class="label">
                                            <mvc:radiobutton path="numberOfRows" value="100"/>
                                            <span class="label-text">100</span>
                                        </label>
                                        <label class="label">
                                            <mvc:radiobutton path="numberOfRows" value="200"/>
                                            <span class="label-text">200</span>
                                        </label>
                                    </li>
                                    <li class="divider"></li>
                                    <li>
                                        <p>
                                            <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                                <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                            </button>
                                        </p>
                                    </li>
                                </ul>
                            </li>
                            <li>
                                <button type="button" tabindex="-1" class="btn btn-regular btn-primary" data-form-set="save: save" ${isMailingEditable ? 'data-form-submit' : 'disabled'}
                                        data-form-url="<c:url value="/mailing/${mailing.id}/attachment/save.action" />">
                                    <i class="icon icon-save"></i>
                                    <span class="text"><mvc:message code="button.Save"/></span>
                                </button>
                            </li>
                        </ul>
                    </div>

                    <div class="tile-content" data-form-content>
                        <div class="table-wrapper">
                            <display:table class="table table-bordered table-striped js-table" id="attachment" name="attachments"
                                           requestURI="/mailing/${mailingId}/attachment/list.action" excludedParams="*" pagesize="${form.numberOfRows}">

                                <%--@elvariable id="attachment" type="com.agnitas.emm.core.components.dto.MailingAttachmentDto"--%>

                                <display:column headerClass="js-table-sort" sortable="true" titleKey="mailing.Attachment" property="name" sortProperty="name" />
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

                                <display:column headerClass="js-table-sort" sortable="true" titleKey="Original_Size" sortProperty="originalSize">
                                    <span>${AgnUtils.bytesToKbStr(attachment.originalSize)} kB</span>
                                </display:column>

                                <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Size_Mail" sortProperty="emailSize">
                                    <span>${AgnUtils.bytesToKbStr(attachment.emailSize)} kB</span>
                                </display:column>

                                <display:column headerClass="js-table-sort" sortable="true" titleKey="Mime_Type" sortProperty="mimeType">
                                    <span class="badge">${attachment.mimeType}</span>
                                </display:column>

                                <c:if test="${fn:length(form.attachments) gt 0}">
                                    <display:column class="table-actions align-center">
                                        <c:if test="${isMailingEditable}">
                                            <c:url var="confirmDeleteLink" value="/mailing/${mailing.id}/attachment/${attachment.id}/confirmDelete.action"/>
                                            <a href="${confirmDeleteLink}"
                                               class="btn btn-regular btn-alert js-row-delete"
                                               data-tooltip="<mvc:message code="mailing.attachment.delete"/>">
                                                <i class="icon icon-trash-o"></i>
                                            </a>
                                        </c:if>

                                        <c:url var="imageLinkNoCache" value="/dc?compID=${attachment.id}"/>
                                        <a href="${imageLinkNoCache}" class="btn btn-regular btn-info" data-prevent-load="" download="${attachment.name}"
                                           data-tooltip="<mvc:message code='button.Download'/>">
                                            <i class="icon icon-cloud-download"></i>
                                        </a>
                                    </display:column>
                                </c:if>
                            </display:table>
                        </div>
                    </div>
                </div>
            </mvc:form>
        </c:set>

        <c:choose>
            <c:when test="${isMailingGrid}">
                <div class="tile-content-padded">
                    ${uploadFormContent}
                    ${editFormContent}
                </div>
            </c:when>

            <c:otherwise>
                ${uploadFormContent}
                ${editFormContent}
            </c:otherwise>
        </c:choose>

    </tiles:putAttribute>
</tiles:insertTemplate>
