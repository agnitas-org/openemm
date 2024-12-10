<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isContentGenerationAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingExclusiveLockingAcquired" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="anotherLockingUserName" type="java.lang.String"--%>
<%--@elvariable id="smsDynNames" type="java.util.Set<java.lang.String>"--%>
<%--@elvariable id="gsm7BitChars" type="java.util.Set<java.lang.Character>"--%>

<c:set var="PREVIEW_FORMAT_HTML" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>

<c:set var="isMailingGrid" value="${form.gridTemplateId > 0}" scope="request"/>
<c:set var="mailingId" value="${form.mailingID}"/>

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
        <jsp:param name="showAiTextGenerationBtn" value="${isContentGenerationAllowed}"/>
    </jsp:include>
</emm:HideByPermission>

<c:choose>
    <c:when test="${isPostMailing}">
        <%@include file="fragments/mailing-type-post.jspf" %>
    </c:when>
    <c:otherwise>
        <div id="mailing-content-blocks" class="tiles-container" data-controller="mailing-content-controller" data-editable-view="${agnEditViewKey}">
            <script data-initializer="mailing-content-initializer" type="application/json">
                {
                    "dynTags": ${emm:toJson(form.tags)},
                    "interestGroupList": ${emm:toJson(form.availableInterestGroups)},
                    "targetGroupList": ${emm:toJson(targets)},
                    "htmlDynTagNames": ${emm:toJson(form.dynTagNames)},
                    "mailingId": ${mailingId},
                    "isMailingExclusiveLockingAcquired": ${isMailingExclusiveLockingAcquired},
                    "isEditableMailing": ${isMailingEditable},
                    "isContentGenerationAllowed": ${isContentGenerationAllowed},
                    "mailFormat": ${mailFormat},
                    "isMailingGrid": ${isMailingGrid},
                    "isEmailMediaTypeActive": ${isEmailMediaTypeActive},
                    "isSmsMediaTypeActive": ${not empty isSmsMediaTypeActive and isSmsMediaTypeActive},
                    "smsDynNames": ${emm:toJson(smsDynNames)}
                }
            </script>
            
            <div id="content-blocks-tile" class="tile" data-editable-tile="main">
                <div class="tile-header">
                    <h1 class="tile-title text-truncate"><mvc:message code="webservice.permissionCategory.contentBlock"/></h1>
                </div>
                    <c:choose>
                        <c:when test="${empty form.tags}">
                            <div class="tile-body">
                                <div class="notification-simple">
                                    <i class="icon icon-info-circle"></i>
                                    <mvc:message code="mailing.content.empty"/>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="tile-body" style="display: grid; grid-template-columns: 1fr 3fr; gap: 10px">
                                <div id="dyn-tag-list">
                                    <c:forEach items="${form.tags}" var="dynTag" varStatus="status">
                                        <input type="radio" class="btn-check" name="view-dyn-tag" autocomplete="off" id="${dynTag.value.id}-dyn-tag-btn" ${status.index eq 0 ? 'checked' : ''}>
                                        <label class="dyn-tag" for="${dynTag.value.id}-dyn-tag-btn" data-dyn-tag-id="${dynTag.value.id}" data-action="switch-dyn-tag">
                                            <span class="d-inline-block text-truncate">${dynTag.key}</span>
                                            <span class="status-badge mailing.status.has-content"></span>
                                        </label>
                                    </c:forEach>
                                </div>
                                <div id="dyn-tag-settings">
                                    <%-- populated with js. see content-editor-template.jspf --%>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
            </div>
            <div class="tiles-block flex-column">
                <%@include file="fragments/mailing-content-settings.jspf" %>

                <emm:ShowByPermission token="mailing.send.show">
                    <c:url var="previewLink" value="/mailing/preview/${mailingId}/view.action">
                        <c:param name="pure" value="true"/>
                        <c:param name="format" value="${form.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
                    </c:url>
                    <div data-load="${previewLink}" data-load-replace data-load-target="#preview-form"></div>
                </emm:ShowByPermission>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="fragments/content-editor-template.jspf" %>

<script id="gsm-7-bit-chars" type="application/json">
    {
      "chars": ${emm:toJson(gsm7BitChars)}
    }
</script>
