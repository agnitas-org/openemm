<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.MailingComponent" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.web.ComMailingAttachmentsAction" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_CONFIRM_DELETE" value="<%=ComMailingAttachmentsAction.ACTION_CONFIRM_DELETE%>"/>

<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <!-- Tabs BEGIN -->
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
            <!-- Tabs END -->
        </ul>

        <c:if test="${isMailingGrid}">
            <ul class="tile-header-actions">${tileHeaderActions}</ul>
        </c:if>
    </tiles:put>

    <tiles:put name="content" type="string">
        <c:if test="${isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>
            <agn:agnForm action="/mailingattachments" enctype="multipart/form-data" data-form="static" class="form-vertical" data-controller="mailing-attachments">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <c:if test="${isMailingGrid}">
                    <html:hidden property="isMailingGrid" value="${isMailingGrid}"/>
                    <html:hidden property="templateId" value="${templateId}"/>
                </c:if>

                <div class="tile">
                    <div class="tile-header">
                        <a href="#" class="headline" data-toggle-tile="#tile-attachmentUpload">
                            <i class="tile-toggle icon icon-angle-up"></i>
                            <bean:message key="New_Attachment"/>
                        </a>
                    </div>
                    <div id="tile-attachmentUpload" class="tile-content tile-content-forms" data-field="toggle-vis">
                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="newAttachment" class="form-label">
                                        <bean:message key="mailing.Attachment"/>
                                    </label>
                                    <input type="file" name="newAttachment" id="newAttachment" class="form-control" data-action="update-filename">
                                        <%--  <html:file property="newAttachment" styleId="newAttachment" onchange="getFilename()" styleClass="form-control"/> --%>
                                </div>
                            </div>

                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="newAttachmentName" class="form-label">
                                        <bean:message key="attachment.name"/>
                                    </label>
                                    <html:text property="newAttachmentName" styleId="newAttachmentName" styleClass="form-control"/>
                                </div>
                            </div>

							<%@include file="/WEB-INF/jsp/mailing/mailing-attachments-types.jsp" %>

                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="attachmentTargetID" class="form-label">
                                        <bean:message key="Target"/>
                                    </label>
                                    <html:select property="attachmentTargetID" styleClass="form-control js-select" styleId="attachmentTargetID">
                                        <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                                        <c:forEach var="target" items="${targets}">
                                            <html:option value="${target.id}">${target.targetName}</html:option>
                                        </c:forEach>
                                    </html:select>
                                </div>
                            </div>

							<%@include file="mailing-attachments-uploaded-pdf.jspf" %>

                            <div class="col-xs-12">
                                <div class="form-group">
                                    <div class="btn-group">

                                        <logic:equal name="mailingAttachmentsForm" property="worldMailingSend" value="false">
                                            <button type="button" tabindex="-1" class="btn btn-regular btn-primary" data-form-set="add: add" data-form-submit>
                                                <i class="icon icon-cloud-upload"></i>
                                                <span class="text"><bean:message key="button.Add"/></span>
                                            </button>
                                        </logic:equal>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <!-- Row END -->

                    </div>
                    <!-- Tile Content END -->

                </div>
                <!-- Tile END -->



            </agn:agnForm>

            <agn:agnForm action="/mailingattachments" enctype="multipart/form-data" data-form="search">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <c:if test="${isMailingGrid}">
                    <html:hidden property="isMailingGrid" value="${isMailingGrid}"/>
                    <html:hidden property="templateId" value="${templateId}"/>
                </c:if>

                <div class="tile">
                    <div class="tile-header">
                        <h2 class="headline"><bean:message key="mailing.Attachments"/></h2>
                        <ul class="tile-header-actions">
                            <logic:equal name="mailingAttachmentsForm" property="worldMailingSend" value="false">
                                <li>
                                    <button type="button" tabindex="-1" class="btn btn-regular btn-primary" data-form-set="save: save" data-form-submit>
                                        <i class="icon icon-save"></i>
                                        <span class="text"><bean:message key="button.Save"/></span>
                                    </button>
                                </li>
                            </logic:equal>
                        </ul>
                    </div>

                    <div class="tile-content" data-form-content>
                        <div class="table-responsive">

                            <table class="table table-bordered table-striped js-table">
                                <thead>
                                <th><bean:message key="mailing.Attachment"/></th>
                                <th><bean:message key="Target"/></th>
                                <th><bean:message key="Original_Size"/></th>
                                <th><bean:message key="default.Size_Mail"/></th>
                                <th><bean:message key="Mime_Type"/></th>
                                <th></th>
                                </thead>

                                <%--@elvariable id="attachments" type="java.util.List<org.agnitas.beans.MailingComponent>"--%>
                                <tbody>
                                <logic:iterate id="attachment" collection="${attachments}" scope="request" type="org.agnitas.beans.MailingComponent">
                                    <tr>
                                        <td>${attachment.componentName}</td>
                                        <td>
                                            <html:select property="target${attachment.id}"
                                                         value="${attachment.targetID}"
                                                         styleId="target${attachment.id}" styleClass="form-control js-select">
                                                <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                                                <c:forEach var="target" items="${targets}">
                                                    <html:option value="${target.id}">${target.targetName}</html:option>
                                                </c:forEach>
                                            </html:select>
                                        </td>
                                        <td><%= AgnUtils.bytesToKbStr(ArrayUtils.getLength(attachment.getBinaryBlock())) %> <bean:message key="default.KByte"/></td>
                                        <td><%= AgnUtils.bytesToKbStr(StringUtils.length(AgnUtils.encodeBase64(attachment.getBinaryBlock()))) %> <bean:message key="default.KByte"/></td>
                                        <td><span class="badge">${attachment.mimeType}</span></td>
                                        <td class="table-actions">
                                            <logic:equal name="mailingAttachmentsForm" property="worldMailingSend" value="false">
                                                <c:set var="messageDelete" scope="page">
                                                    <bean:message key='mailing.attachment.delete'/>
                                                </c:set>
                                                <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete"
                                                             data-tooltip="${messageDelete}"
                                                             page="/mailingattachments.do?action=${ACTION_CONFIRM_DELETE}&attachmentId=${attachment.id}&mailingID=${mailingAttachmentsForm.mailingID}">
                                                    <i class="icon icon-trash-o"></i>
                                                </agn:agnLink>
                                            </logic:equal>

                                            <c:set var="messageDownload" scope="page">
                                                <bean:message key='button.Download'/>
                                            </c:set>
                                            <agn:agnLink styleClass="btn btn-regular btn-primary" data-tooltip="${messageDownload}" data-prevent-load="" page="/dc?compID=${attachment.id}">
                                                <i class="icon icon-cloud-download"></i>
                                            </agn:agnLink>
                                        </td>
                                    </tr>
                                </logic:iterate>

                                </tbody>
                            </table>

                        </div>

                    </div>
                    <!-- Tile Content END -->

                </div>
                <!-- Tile END -->

            </agn:agnForm>
        <c:if test="${isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
</tiles:insert>
