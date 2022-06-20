<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" %>
<%@ page import="com.agnitas.web.forms.ComMailingBaseForm" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>

<c:set var="ACTION_MOVE_MEDIA_DOWN" value="<%= MailingBaseAction.ACTION_MOVE_MEDIA_DOWN %>"/>
<c:set var="ACTION_MOVE_MEDIA_UP" value="<%= MailingBaseAction.ACTION_MOVE_MEDIA_UP %>"/>

<c:if test="${not mailingBaseForm.isMailingGrid}">
    <div class="tile" data-action="scroll-to">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-mediaTypes">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="mediatype.mediatypes"/>
            </a>
        </div>
        <div id="tile-mediaTypes" class="tile-content tile-content-forms">
            <div class="form-group">
                <html:hidden property="activeMedia"/>
                <div class="col-sm-4">
                    <label class="control-label">
                        <bean:message key="mediatype.mediatypes"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MediaTypesMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <ul class="list-group">
                        <c:forEach items="${mailingBaseForm.prioritizedMediatypes}" var="mediaType">

                            <c:set var="mediaTypeName" value="${fn:toLowerCase(mediaType.name())}"/>
                            <c:set var="mediaTypeCode" value="${mediaType.mediaCode}"/>
                            <c:choose>
                                <c:when test="${mailingBaseForm.isMailingGrid}">
                                    <c:if test="${mediaTypeName eq 'email'}">
                                        <emm:ShowByPermission token="${mediaType.requiredPermission}">
                                            <li class="list-group-item checkbox">
                                                <c:if test="${IS_MAILING_EDITABLE}">
                                                    <input type="hidden" name="__STRUTS_CHECKBOX_useMediaType[${mediaTypeCode}]" value="false"/>
                                                </c:if>
                                                <label>
                                                    <agn:agnCheckbox property="useMediaType[${mediaTypeCode}]" value="true" disabled="true"/>
                                                    <bean:message key="mailing.MediaType.${mediaTypeName}"/>
                                                </label>
                                            </li>
                                        </emm:ShowByPermission>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${mediaTypeName eq 'email'}">
                                        <emm:ShowByPermission token="mediatype.email">
                                            <li class="list-group-item checkbox">
                                                <c:if test="${IS_MAILING_EDITABLE}">
                                                    <input type="hidden" name="__STRUTS_CHECKBOX_useMediaType[${mediaTypeCode}]" value="false"/>
                                                </c:if>
                                                <label>
                                                    <agn:agnCheckbox property="useMediaType[${mediaTypeCode}]" value="true" disabled="${not IS_MAILING_EDITABLE}" data-action="change-media" />
                                                    <bean:message key="mailing.MediaType.email"/>
                                                </label>
                                                <c:if test="${mailingBaseForm.useMediaType[mediaTypeCode]}">
                                                    <div class="list-group-item-controls">
                                                        <a href="#" data-config="activeMedia: ${mediaTypeCode}, action: ${ACTION_MOVE_MEDIA_DOWN}" data-action="prioritise-media">
                                                            <i class="icon icon-chevron-circle-down"></i>
                                                        </a>
                                                        <a href="#" data-config="activeMedia: ${mediaTypeCode}, action: ${ACTION_MOVE_MEDIA_UP}" data-action="prioritise-media">
                                                            <i class="icon icon-chevron-circle-up"></i>
                                                        </a>
                                                    </div>
                                                </c:if>
                                            </li>
                                        </emm:ShowByPermission>
                                    </c:if>
                                    <c:if test="${mediaTypeName ne 'email'}">
                                        <%@include file="/WEB-INF/jsp/mailing/media/meditypes-list-item.jspf"%>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </ul>
                    
                    <%@include file="/WEB-INF/jsp/mailing/media/sms-mediatype-forbidden-message.jspf"%>

                </div>
            </div>

        </div>

    </div>
</c:if>

<%@include file="/WEB-INF/jsp/mailing/media/prioritized-mediatypes.jspf" %>
