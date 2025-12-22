<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isSmsMediaTypeActive" type="java.lang.Boolean"--%>
<%--@elvariable id="isEmailMediaTypeActive" type="java.lang.Boolean"--%>
<%--@elvariable id="isContentGenerationAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="mailFormat" type="java.lang.Integer"--%>
<%--@elvariable id="anotherLockingUserName" type="java.lang.String"--%>
<%--@elvariable id="smsDynNames" type="java.util.Set<java.lang.String>"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>

<c:set var="PREVIEW_FORMAT_HTML" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= MailingPreviewHelper.INPUT_TYPE_TEXT %>"/>

<c:set var="isMailingGrid" value="${form.gridTemplateId > 0}" scope="request"/>
<c:set var="mailingId" value="${form.mailingID}"/>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<emm:HideByPermission token="mailing.editor.hide">
    <c:if test="${not emm:isJoditEditorUsageAllowed(pageContext.request)}">
        <jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp">
            <jsp:param name="toolbarType" value="${emm:getWysiwygToolbarType(pageContext.request, 'EMM')}"/>
        </jsp:include>
    </c:if>
</emm:HideByPermission>

<c:choose>
    <c:when test="${isPostMailing}">
        <%@include file="fragments/mailing-type-post.jspf" %>
    </c:when>
    <c:otherwise>
        <div id="mailing-content-blocks" class="tiles-container"
             data-controller="mailing-content-controller"
             data-editable-view="${agnEditViewKey}">
            <script data-initializer="mailing-content-initializer" type="application/json">
                {
                    "dynTags": ${emm:toJson(form.tags)},
                    "interestGroupList": ${emm:toJson(form.availableInterestGroups)},
                    "targetGroupList": ${emm:toJson(targets)},
                    "htmlDynTagNames": ${emm:toJson(form.dynTagNames)},
                    "mailingId": ${mailingId},
                    "isEditableMailing": ${isMailingEditable},
                    "isContentGenerationAllowed": ${isContentGenerationAllowed},
                    "mailFormat": ${mailFormat},
                    "isMailingGrid": ${isMailingGrid},
                    "isEmailMediaTypeActive": ${isEmailMediaTypeActive},
                    "isSmsMediaTypeActive": ${not empty isSmsMediaTypeActive and isSmsMediaTypeActive},
                    "smsDynNames": ${emm:toJson(smsDynNames)}
                }
            </script>

            <div id="content-blocks-tile" class="tile" data-editable-tile="main" style="flex: 1 1 55%">
                <nav class="tile-header navbar navbar-expand-lg border-bottom">
                    <a class="btn btn-header-tab active" href="#"><span class="text text-truncate"></span></a>
                    <button class="navbar-toggler btn-icon" type="button" data-bs-toggle="offcanvas" data-bs-target="#mailing-settings-nav" aria-controls="mailing-settings-nav" aria-expanded="false">
                        <i class="icon icon-bars"></i>
                    </button>
                    <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="mailing-settings-nav">
                        <ul class="navbar-nav offcanvas-body">
                            <li class="nav-item">
                                <a class="btn btn-outline-primary active" href="#" data-toggle-tab="#content-blocks-tab" data-bs-dismiss="offcanvas">
                                    <span class="text-truncate"><mvc:message code="webservice.permissionCategory.contentBlock"/></span>
                                </a>
                            </li>

                            <li class="nav-item">
                                <a class="btn btn-outline-primary" href="#" data-toggle-tab="#frame-content-tab" data-bs-dismiss="offcanvas">
                                    <span class="text-truncate"><mvc:message code="mailing.frame"/></span>
                                </a>
                            </li>

                            <%@ include file="fragments/mailing-content-source-tab-btn.jspf" %>
                        </ul>
                    </div>
                </nav>

                <%@ include file="fragments/content-blocks-tab.jspf" %>

                <mvc:form id="frame-content-tab" cssClass="tile-body hidden" servletRelativeAction="/mailing/${mailingId}/frame.action" modelAttribute="frameContentForm"
                          data-initializer="frame-content-tab"
                          data-form="resource"
                          data-resource-selector="#mailing-content-blocks">
                    <c:set var="showText" value="${isEmailMediaTypeActive or isMailingGrid}"/>
                    <jsp:include page="mailing-frame-content-tab.jsp">
                        <jsp:param name="fromContentTab" value="true"/>
                        <jsp:param name="showText" value="${showText}"/>
                        <jsp:param name="showHtml" value="${showText and mailFormat ne 0 and not isMailingGrid}"/>
                        <jsp:param name="showSms" value="${isSmsMediaTypeActive}"/>
                    </jsp:include>
                </mvc:form>

                <%@ include file="fragments/mailing-content-settings-tab.jspf" %>
            </div>

            <div class="tiles-block flex-column" style="flex: 1 1 45%">
                <emm:ShowByPermission token="mailing.send.show">
                    <c:url var="previewLink" value="/mailing/preview/${mailingId}/view.action">
                        <c:param name="pure" value="true"/>
                        <c:param name="format" value="${form.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
                        <c:param name="forEmcTextModules" value="${isMailingGrid}"/>
                    </c:url>
                    <div data-load="${previewLink}" data-load-replace data-load-target="#preview-form"></div>
                </emm:ShowByPermission>
            </div>
        </div>
    </c:otherwise>
</c:choose>
