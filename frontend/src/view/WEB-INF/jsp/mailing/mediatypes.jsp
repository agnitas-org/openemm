<%@ page import="com.agnitas.web.forms.ComMailingBaseForm" errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>

<c:set var="isMailingEditable" value="${not mailingBaseForm.worldMailingSend}"/>
<emm:ShowByPermission token="mailing.content.change.always">
    <c:set var="isMailingEditable" value="${true}"/>
</emm:ShowByPermission>

<c:if test="${not mailingBaseForm.isMailingGrid}">
    <div class="tile">
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
                        <c:forEach items="${mailingBaseForm.priorities}" var="mediaTypeCode">
                            <c:set var="mediaTypeName" value="${mailingBaseForm.mediaTypeLabelsLowerCase[mediaTypeCode]}"/>
                            <c:choose>
                                <c:when test="${mailingBaseForm.isMailingGrid}">
                                    <c:if test="${mediaTypeName eq 'email'}">
                                        <emm:ShowByPermission token="mediatype.${mediaTypeName}">
                                            <li class="list-group-item checkbox">
                                                <c:if test="${isMailingEditable}">
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
                                                <c:if test="${isMailingEditable}">
                                                    <input type="hidden" name="__STRUTS_CHECKBOX_useMediaType[${mediaTypeCode}]" value="false"/>
                                                </c:if>
                                                <label>
                                                    <agn:agnCheckbox property="useMediaType[${mediaTypeCode}]" value="true" disabled="${not isMailingEditable}" data-form-action="9" />
                                                    <bean:message key="mailing.MediaType.email"/>
                                                </label>
                                                <c:if test="${mailingBaseForm.useMediaType[mediaTypeCode]}">
                                                    <div class="list-group-item-controls">
                                                        <a href="#" data-form-set="activeMedia:${mediaTypeCode}" data-form-action="21">
                                                            <i class="icon icon-chevron-circle-down"></i>
                                                        </a>
                                                        <a href="#" data-form-set="activeMedia:${mediaTypeCode}" data-form-action="20">
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
