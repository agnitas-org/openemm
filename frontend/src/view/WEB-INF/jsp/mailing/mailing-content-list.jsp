<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>
<%@ page import="com.agnitas.web.ComMailingSendAction" %>
<%@ page import="org.agnitas.beans.EmmLayoutBase" %>
<%@ page import="org.agnitas.beans.Mailing" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingContentForm" type="com.agnitas.web.ComMailingContentForm"--%>

<c:set var="ACTION_VIEW" value="<%= ComMailingContentAction.ACTION_VIEW %>"/>
<c:set var="ACTION_PREVIEW_SELECT" value="<%= ComMailingSendAction.ACTION_PREVIEW_SELECT %>"/>
<c:set var="ACTION_PREVIEW" value="<%= ComMailingSendAction.ACTION_PREVIEW %>"/>
<c:set var="ACTION_VIEW_CONTENT" value="<%= ComMailingContentAction.ACTION_VIEW_CONTENT %>"/>
<c:set var="ACTION_IMPORT_CONTENT" value="<%= ComMailingContentAction.ACTION_IMPORT_CONTENT %>"/>
<c:set var="ACTION_VIEW_TEXTBLOCK" value="<%= ComMailingContentAction.ACTION_VIEW_TEXTBLOCK %>"/>
<c:set var="ACTION_GENERATE_TEXT_FROM_HTML"	value="<%= ComMailingContentAction.ACTION_GENERATE_TEXT_FROM_HTML %>"/>

<c:set var="LIVEPREVIEW_POSITION_BOTTOM" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_BOTTOM %>"/>
<c:set var="LIVEPREVIEW_POSITION_DEACTIVATE" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_DEACTIVATE %>"/>
<c:set var="LIVEPREVIEW_POSITION_RIGHT" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_RIGHT %>"/>
<c:set var="MENU_POSITION_LEFT" value="<%= EmmLayoutBase.MENU_POSITION_LEFT %>"/>
<c:set var="MENU_POSITION_TOP" value="<%= EmmLayoutBase.MENU_POSITION_TOP %>"/>
<c:set var="PREVIEW_FORMAT_HTML" value="<%= Mailing.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= Mailing.INPUT_TYPE_TEXT %>"/>

<c:set var="isMailingGrid" value="${mailingContentForm.gridTemplateId > 0}" scope="request"/>
<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>
<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:put name="content" type="string">
        <c:if test="${isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>
            <div class="row">
                <div class="col-xs-12" data-view-split="col-md-6 split-1-1" data-view-block="col-xs-12" data-view-hidden="col-xs-12">
                    <agn:agnForm action="/mailingcontent"  data-form="resource" data-controller="mailing-content-controller">
                        <html:hidden property="mailingID"/>
                        <html:hidden property="showDateSettings"/>
                        <html:hidden property="action" value=""/>

                        <div class="tile">
                            <div class="tile-header">
                                <h2 class="headline"><bean:message key="mailing.TextModules"/></h2>
                            </div>

                            <div class="tile-content" data-initializer="mailing-content-overview">
                                <div class="tile-content-forms">
                                	<%@include file="mailing-content-list-contentsource-list.jsp"  %>

                                    <%-- TODO: remove permission check once GWUA-4016 successfully tested --%>
                                    <c:if test="${not isMailingGrid and mailingContentForm.enableTextGeneration and not mailingContentForm.worldMailingSend}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label">
                                                    <bean:message key="mailing.option"/>
                                                </label>
                                            </div>

                                            <div class="col-sm-8">
                                                <button type="button" tabindex="-1" class="btn btn-regular" data-form-action="${ACTION_GENERATE_TEXT_FROM_HTML}">
                                                    <i class="icon icon-exchange"></i>
                                                    <span class="text"><bean:message key="mailing.GenerateText"/></span>
                                                </button>
                                            </div>
                                        </div>
                                    </c:if>

									<%@include file="mailing-content-list-contentsource-datelimit.jsp" %>
                                </div>

                                <c:choose>
                                    <c:when test="${hasTempBetaPermission}">
                                        <table class="table table-bordered table-striped table-hover js-table" id="contentList">
                                            <thead>
                                            <tr>
                                                <th><bean:message key="Text_Module"/></th>
                                                <th><bean:message key="Target"/></th>
                                                <th><bean:message key="default.Content"/></th>
                                            </tr>
                                            </thead>
                                            <tbody id="table_body">
                                            </tbody>
                                        </table>

                                        <c:url var="saveUrl" value="/mailingcontent/save.action"/>

                                        <%--todo: mailingContentForm.tags contains extra information.--%>
                                        <%--todo: Please create DTO for that entity during migration --%>
                                        <script data-initializer="mailing-content-initializer" type="application/json">
                                            {
                                                "saveUrl": "${saveUrl}",
                                                "targetGroupList": ${emm:toJson(mailingContentForm.availableTargetGroups)},
                                                "interestGroupList": ${emm:toJson(mailingContentForm.availableInterestGroups)},
                                                "dynTagNames": ${emm:toJson(mailingContentForm.dynTagNames)},
                                                "dynTagsMap": ${emm:toJson(mailingContentForm.tags)}
                                            }
                                        </script>
                                    </c:when>
                                    <%-- TODO: remove the following block when GWUA-3758 will be successful tested --%>
                                    <c:otherwise>
                                        <table class="table table-bordered table-striped table-hover js-table" id="contentList">
                                            <thead>
                                            <tr>
                                                <th><bean:message key="Text_Module"/></th>
                                                <th><bean:message key="Target"/></th>
                                                <th><bean:message key="default.Content"/></th>
                                            </tr>
                                            </thead>

                                            <c:forEach var="dyntag" items="${mailingContentForm.tags}">
                                                <c:set var="dynTag" value="${dyntag.value}"/>

                                                <c:url var="viewLink" value="/mailingcontent.do">
                                                    <c:param name="action" value="${ACTION_VIEW_TEXTBLOCK}"/>
                                                    <c:param name="dynNameID" value="${dynTag.id}"/>
                                                    <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
                                                </c:url>

                                                <tr data-dyn-name-id="${dynTag.id}">
                                                    <td class="align-top">
                                                        <strong>${dynTag.dynName}</strong>
                                                        <a href="${viewLink}" class="hidden js-row-show"></a>
                                                    </td>
                                                    <td class="align-top">
                                                        <c:forEach var="dyncontent" items="${dyntag.value.dynContent}">
                                                            <c:set var="tagContent" value="${dyncontent.value}"/>

                                                            <a href="${viewLink}#content${tagContent.id}">
                                                                <c:choose>
                                                                    <c:when test="${tagContent.targetID eq 0}">
                                                                        <bean:message key="statistic.all_subscribers"/>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <c:forEach var="target" items="${mailingContentForm.availableTargetGroups}">
                                                                            <logic:equal name="target" property="id" value="${tagContent.targetID}">
                                                                                <c:choose>
                                                                                    <c:when test="${target.deleted == 0}">
                                                                                        <span class="multiline-sm-400">${target.targetName} (${target.id})</span>
                                                                                    </c:when>
                                                                                    <c:otherwise>
                                                                                        <span class="multiline-sm-400">${target.targetName} (<bean:message key="target.Deleted"/>)</span>
                                                                                    </c:otherwise>
                                                                                </c:choose>
                                                                            </logic:equal>
                                                                        </c:forEach>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </a>
                                                            <br>
                                                        </c:forEach>
                                                        <a href="${viewLink}#content0">
                                                            <bean:message key="New_Content"/>
                                                        </a>
                                                    </td>
                                                    <td class="align-top">
                                                        <logic:iterate id="dyncontent" name="dyntag" property="value.dynContent">
                                                            <span class="multiline-sm-300">${fn:escapeXml(emm:abbreviate(dyncontent.value.dynContent, 35))}</span>
                                                            <br>
                                                        </logic:iterate>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <!-- Tile Content END -->
                        </div>
                        <!-- Tile END -->
                    </agn:agnForm>
                </div>
                <!-- col END -->

                <emm:ShowByPermission token="mailing.send.show">
                    <c:url var="previewLink" value="/mailingsend.do">
                        <c:param name="action" value="${ACTION_PREVIEW_SELECT}"/>
                        <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
                        <c:param name="previewSelectPure" value="true"/>
                        <c:param name="previewFormat" value="${mailingContentForm.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
                    </c:url>

                    <div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                        <div data-load="${previewLink}" data-load-target="#preview"></div>
                    </div>
                </emm:ShowByPermission>
            </div>
        <c:if test="${isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
</tiles:insert>

<%-- TODO: remove this cheking when GWUA-3758 will be successful tested --%>
<c:if test="${hasTempBetaPermission}" >
<c:set var="mailingId" value="${mailingContentForm.mailingID}"/>

<%@ include file="fragments/content-editor-template.jspf"  %>

<%@ include file="fragments/enlarged-content-editor-template.jspf"  %>

<%@ include file="fragments/mailing-content-table-entry-template.jspf"  %>
</c:if>
