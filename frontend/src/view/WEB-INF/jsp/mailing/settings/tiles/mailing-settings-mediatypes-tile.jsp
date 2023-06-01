<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="prioritizedMediatypes" type="java.util.List<com.agnitas.emm.core.mediatypes.common.MediaTypes>"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:if test="${gridTemplateId <= 0}">
    <div id="mediatypes-tile" class="tile" data-action="scroll-to">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-mediaTypes">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="mediatype.mediatypes"/>
            </a>
        </div>
        <div id="tile-mediaTypes" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="mediatype.mediatypes"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MediaTypesMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <ul class="list-group">
                        <c:forEach var="mediaType" items="${prioritizedMediatypes}">
                            <c:set var="mediaTypeName" value="${fn:toLowerCase(mediaType.name())}"/>
                            <c:set var="mediaTypeCode" value="${mediaType.mediaCode}"/>

                            <c:set var="mtFormName" value="${mediaTypeName}Mediatype"/>
                            <c:set var="mt" value="${mailingSettingsForm[mtFormName]}" />
                            <c:set var="mediatypeItem">
                                <li class="list-group-item checkbox" data-priority="${mt.priority}" data-mediatype="${mediaTypeName}" data-action="change-mediatype">
                                    <label>
                                        <mvc:checkbox path="${mediaTypeName}Mediatype.active" value="true" disabled="${not MAILING_EDITABLE}" />
                                        <mvc:message code="mailing.MediaType.${mediaTypeName}" />
                                    </label>
                                    <c:if test="${not empty mt and mt.active}">
                                        <div class="list-group-item-controls">
                                            <a href="#" data-config="mediatypeCode: ${mediaTypeCode}" data-action="prioritise-mediatype-down">
                                                <i class="icon icon-chevron-circle-down"></i>
                                            </a>
                                            <a href="#" data-config="mediatypeCode: ${mediaTypeCode}" data-action="prioritise-mediatype-up">
                                                <i class="icon icon-chevron-circle-up"></i>
                                            </a>
                                        </div>
                                    </c:if>
                                </li>
                            </c:set>
                            <c:if test="${mediaTypeName eq 'email'}">
                                <emm:ShowByPermission token="mediatype.email">
                                    ${mediatypeItem}
                                </emm:ShowByPermission>
                            </c:if>
                            <c:if test="${mediaTypeName ne 'email'}">
                                <%@include file="../fragments/mailing-not-email-mediatypes.jspf"%>
                            </c:if>
                        </c:forEach>
                    </ul>
                    <%@include file="../fragments/sms-mediatype-forbidden-msg.jspf"%>
                </div>
            </div>
        </div>
    </div>
</c:if>

<%@include file="../fragments/mailing-settings-prioritized-mediatypes.jspf" %>
