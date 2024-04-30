<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingExclusiveLockingAcquired" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="anotherLockingUserName" type="java.lang.String"--%>

<c:set var="PREVIEW_FORMAT_HTML" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>

<c:set var="isMailingGrid" value="${form.gridTemplateId > 0}" scope="request"/>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<emm:HideByPermission token="mailing.editor.hide">
    <c:choose>
        <c:when test="${emm:fullPackAllowed(pageContext.request)}">
            <c:set var="toolbarType" value="Full"/>
        </c:when>
        <c:when test="${emm:isCKEditorTrimmed(pageContext.request)}">
            <c:set var="toolbarType" value="Trimmed"/>
        </c:when>
        <c:otherwise>
            <c:set var="toolbarType" value="EMM"/>
        </c:otherwise>
    </c:choose>

    <jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp">
        <jsp:param name="toolbarType" value="${toolbarType}"/>
    </jsp:include>
</emm:HideByPermission>

<tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:put name="content" type="string">
        <c:if test="${isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>
        <c:choose>
            <c:when test="${not isPostMailing}">
                <div class="row" data-controller="mailing-content-controller">
                    <div class="col-xs-12" data-view-split="col-md-6 split-1-1" data-view-block="col-xs-12" data-view-hidden="col-xs-12">

                        <div class="tile">
                            <div class="tile-header">
                                <h2 class="headline"><mvc:message code="mailing.TextModules"/></h2>
                            </div>

                            <div class="tile-content" data-initializer="mailing-content-overview">
                                <div class="tile-content-forms">
                                    <mvc:form servletRelativeAction="/mailing/content/${form.mailingID}/import.action" data-form="resource">
                                        <%@include file="mailing-content-list-contentsource-list.jsp"  %>

                                        <%@include file="fragments/mailing-generate-text-from-html.jspf" %>

                                        <%@include file="mailing-content-list-contentsource-datelimit.jsp" %>
                                    </mvc:form>
                                </div>

                                <table class="table table-bordered table-striped table-hover js-table" id="contentList"
                                       data-controls-group="editing">
                                    <thead>
                                    <tr>
                                        <th><mvc:message code="Text_Module"/></th>
                                        <th><mvc:message code="Target"/></th>
                                        <th><mvc:message code="default.Content"/></th>
                                    </tr>
                                    </thead>
                                    <tbody id="table_body">
                                    </tbody>
                                </table>

                                <script data-initializer="mailing-content-initializer" type="application/json">
                                    {
                                        "targetGroupList": ${emm:toJson(form.availableTargetGroups)},
                                        "interestGroupList": ${emm:toJson(form.availableInterestGroups)},
                                        "dynTagNames": ${emm:toJson(form.dynTagNames)},
                                        "dynTagsMap": ${emm:toJson(form.tags)},
                                        "isMailingExclusiveLockingAcquired": ${isMailingExclusiveLockingAcquired},
                                        "isEditableMailing": ${isMailingEditable}
                                    }
                                </script>

                                <script id="config:mailing-content-overview" type="application/json">
                                    {
                                        "mailingId": ${form.mailingID},
                                        "isMailingExclusiveLockingAcquired": ${isMailingExclusiveLockingAcquired},
                                        "anotherLockingUserName": "${anotherLockingUserName}",
                                        "isEditableMailing": ${isMailingEditable}
                                    }
                                </script>
                            </div>
                            <!-- Tile Content END -->
                        </div>
                        <!-- Tile END -->
                    </div>
                    <!-- col END -->

                    <emm:ShowByPermission token="mailing.send.show">
                        <c:url var="previewLink" value="/mailing/preview/${form.mailingID}/view.action">
                            <c:param name="pure" value="true"/>
                            <c:param name="format" value="${form.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
                        </c:url>

                        <div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                            <div data-load="${previewLink}" data-load-target="#preview"></div>
                        </div>
                    </emm:ShowByPermission>
                </div>
            </c:when>
            <c:otherwise>
                <%@include file="fragments/mailing-type-post.jspf" %>
            </c:otherwise>
        </c:choose>
        <c:if test="${isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
</tiles:insert>

<c:set var="mailingId" value="${form.mailingID}"/>

<%@ include file="../fragments/content-editor-template.jspf"  %>
<%@ include file="../fragments/enlarged-content-editor-template.jspf"  %>
<%@ include file="fragments/mailing-content-table-entry-template.jspf"  %>
