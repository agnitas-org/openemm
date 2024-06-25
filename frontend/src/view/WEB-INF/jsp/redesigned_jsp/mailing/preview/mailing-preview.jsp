<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="org.agnitas.preview.ModeType" %>
<%@ page import="org.agnitas.preview.Preview" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="availablePreviewFormats" type="java.util.List"--%>
<%--@elvariable id="previewRecipients" type="java.util.List<com.agnitas.beans.impl.ComRecipientLiteImpl>"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>
<%--@elvariable id="mailingListExist" type="java.lang.Boolean"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<c:set var="EMAIL_TYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>" scope="page"/>
<c:set var="RECIPIENT_MODE" value="<%= ModeType.RECIPIENT %>" scope="request" />
<c:set var="TARGET_GROUP_MODE" value="<%= ModeType.TARGET_GROUP %>" scope="request" />
<c:set var="MANUAL_MODE" value="<%= ModeType.MANUAL %>" scope="request" />

<%-- If preview is requested as a separate tile (to be embedded) then it must look the same for both regular and grid mailings --%>
<c:set var="isMailingGrid" value="${form.isMailingGrid and not (form.pure eq true)}" scope="request"/>
<c:set var="storedFieldsScope" value="${form.mailingId}"/>

<c:url var="previewLink" value="/mailing/preview/view-content.action">
    <c:param name="mailingId" value="${form.mailingId}"/>
    <c:param name="format" value="${form.format}"/>
    <c:param name="modeTypeId" value="${form.modeTypeId}"/>
    <c:param name="size" value="${form.size}"/>
    <c:param name="customerID" value="${form.customerID}"/>
    <c:param name="targetGroupId" value="${form.targetGroupId}"/>
    <c:param name="noImages" value="${form.noImages}"/>
</c:url>

<div class="tiles-container">
    <mvc:form cssClass="tile" id="preview-form" servletRelativeAction="/mailing/preview/${form.mailingId}/view.action" modelAttribute="form"
          data-controller="mailing-preview" data-initializer="mailing-preview" data-editable-tile="">
        <mvc:hidden path="mailingId"/>
        <mvc:hidden path="pure"/>
        <mvc:hidden path="reload"/>

        <script id="config:mailing-preview" type="application/json">
            {
                "MAILING_ID": "${form.mailingId}"
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="Preview" /></h1>

            <c:if test="${empty mailingListExist or mailingListExist}">
                <div class="tile-controls">
                    <c:if test="${emm:contains(availablePreviewFormats, EMAIL_TYPE_CODE)}">
                        <c:url var="downloadHtmlLink" value="/mailing/preview/html.action">
                            <c:param name="mailingId" value="${form.mailingId}"/>
                            <c:param name="format" value="${form.format}"/>
                            <c:param name="modeTypeId" value="${form.modeTypeId}"/>
                            <c:param name="size" value="${form.size}"/>
                            <c:param name="customerID" value="${form.customerID}"/>
                            <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                            <c:param name="noImages" value="${form.noImages}"/>
                        </c:url>
                        <a href="${downloadHtmlLink}" class="btn btn-icon-sm btn-inverse" data-tooltip="<mvc:message code="default.HTML.code"/>" data-prevent-load="">
                            <i class="icon icon-file-code"></i>
                        </a>
                    </c:if>

                    <c:url var="previewPDFLink" value="/mailing/preview/pdf.action">
                        <c:param name="mailingId" value="${form.mailingId}"/>
                        <c:param name="format" value="${form.format}"/>
                        <c:param name="modeTypeId" value="${form.modeTypeId}"/>
                        <c:param name="size" value="${form.size}"/>
                        <c:param name="customerID" value="${form.customerID}"/>
                        <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                        <c:param name="noImages" value="${form.noImages}"/>
                    </c:url>

                    <a href="${previewPDFLink}" class="btn btn-icon-sm btn-inverse" data-tooltip="<mvc:message code="mailing.preview.pdf" />" data-prevent-load="">
                        <i class="icon icon-file-pdf"></i>
                    </a>

                    <a href="${previewLink}" class="btn btn-icon-sm btn-inverse" target="_blank" data-tooltip="<mvc:message code="mailing.open_preview"/>">
                        <i class="icon icon-external-link-alt"></i>
                    </a>

                    <div class="dropdown">
                        <button class="btn btn-sm-horizontal btn-inverse dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                            <i class="icon icon-eye"></i>
                            <mvc:message code="default.View"/>
                        </button>

                        <div class="dropdown-menu" data-action="refresh-preview">
                            <div class="row g-2">
                                <div class="col-12">
                                    <label for="preview-size" class="form-label"><mvc:message code="default.Size"/></label>

                                    <mvc:select id="preview-size" path="size" cssClass="form-control js-select" data-stored-field="${storedFieldsScope}">
                                        <mvc:option value="1"><mvc:message code="predelivery.desktop"/></mvc:option>
                                        <mvc:option value="2"><mvc:message code="mailing.PreviewSize.MobilePortrait"/></mvc:option>
                                        <mvc:option value="3"><mvc:message code="mailing.PreviewSize.MobileLandscape"/></mvc:option>
                                        <mvc:option value="4"><mvc:message code="mailing.PreviewSize.TabletPortrait"/></mvc:option>
                                        <mvc:option value="5"><mvc:message code="mailing.PreviewSize.TabletLandscape"/></mvc:option>
                                    </mvc:select>
                                </div>

                                <c:if test="${not empty availablePreviewFormats}">
                                    <div class="col-12">
                                        <label for="preview-format" class="form-label"><mvc:message code="action.Format"/></label>
                                        <mvc:select id="preview-format" path="format" cssClass="form-control js-select" data-stored-field="${storedFieldsScope}">
                                            <c:forEach var="mediaTypeCode" items="${availablePreviewFormats}">
                                                <c:choose>
                                                    <c:when test="${mediaTypeCode eq EMAIL_TYPE_CODE}">
                                                        <mvc:option value="${MailingPreviewHelper.INPUT_TYPE_TEXT}"><mvc:message code="Text"/></mvc:option>
                                                        <c:if test="${form.emailFormat gt 0}">
                                                            <mvc:option value="${MailingPreviewHelper.INPUT_TYPE_HTML}"><mvc:message code="HTML"/></mvc:option>
                                                        </c:if>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <mvc:option value="${mediaTypeCode + 1}"><mvc:message code='mailing.MediaType.${mediaTypeCode}'/></mvc:option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </mvc:select>
                                    </div>
                                </c:if>

                                <div class="col-12">
                                    <div class="form-check form-switch">
                                        <mvc:checkbox id="preview-no-images" path="noImages" cssClass="form-check-input" role="switch" data-stored-field="${storedFieldsScope}"/>
                                        <label class="form-label form-check-label fw-medium" for="preview-no-images"><mvc:message code="predelivery.without.images"/></label>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </c:if>
        </div>

        <div class="tile-body">
            <div class="row g-0">
                <c:choose>
                    <c:when test="${empty mailingListExist or mailingListExist}">
                        <c:if test="${form.containsHeader}">
                            <div class="col-12 tile-body__block">
                                <div class="row g-1" data-field="toggle-vis">
                                    <div class="col">
                                        <label for="preview-mode" class="form-label"><mvc:message code="mailing.preview.source" /></label>

                                        <mvc:select id="preview-mode" path="modeType" cssClass="form-control js-select" data-field-vis="" data-action="change-stored-header-data" data-stored-field="${storedFieldsScope}">
                                            <mvc:option value="${RECIPIENT_MODE}" data-field-vis-hide="#recipient-manual-input-block, #target-groups-block"
                                                        data-field-vis-show="#recipient-select-block, #test-recipient-btn-block">
                                                <mvc:message code="recipient.TestSubscriber"/>
                                            </mvc:option>
                                            <mvc:option value="${MANUAL_MODE}" data-field-vis-hide="#recipient-select-block, #target-groups-block"
                                                        data-field-vis-show="#recipient-manual-input-block, #test-recipient-btn-block">
                                                <mvc:message code="mailing.preview.input"/>
                                            </mvc:option>

                                            <mvc:option value="${TARGET_GROUP_MODE}" data-field-vis-show="#target-groups-block"
                                                        data-field-vis-hide="#recipient-select-block, #test-recipient-btn-block, #recipient-manual-input-block">
                                                <mvc:message code="target.Target"/>
                                            </mvc:option>
                                        </mvc:select>
                                    </div>

                                    <div id="recipient-select-block" class="col">
                                        <div class="d-flex align-items-end h-100">
                                            <select name="customerATID" class="js-select form-control" data-action="change-header-data" data-sort="alphabetic">
                                                <option value="0" selected="${form.customerATID == 0}" data-no-sort>
                                                    <mvc:message code="default.select.email"/>
                                                </option>
                                                <c:forEach var="recipient" items="${previewRecipients}">
                                                    <c:set var="customerId" value="${recipient.id}"/>
                                                    <c:set var="customerName" value="${recipient.firstname} ${recipient.lastname} &lt;${recipient.email}&gt;"/>
                                                    <c:set var="isSelected" value="${form.customerATID == customerId or form.customerID == customerId}"/>

                                                    <option value="${customerId}" data-email="${recipient.email}" ${isSelected ? 'selected="selected"' : ''}>${customerName}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div id="recipient-manual-input-block" class="col">
                                        <div class="d-flex align-items-end gap-1 h-100">
                                            <mvc:text path="customerEmail" cssClass="form-control" data-action="change-personalized-test-recipients" data-stored-field="${storedFieldsScope}" />
                                            <button type="button" id="btnCustomEmailRefresh" class="btn btn-inverse" data-action="update-preview">
                                                <span class="text text-truncate"><mvc:message code="default.enter.email"/></span>
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <div id="target-groups-block" class="col">
                                        <div class="d-flex align-items-end h-100">
                                            <mvc:select path="targetGroupId" data-action="change-stored-header-data"
                                                        cssClass="js-select form-control" data-stored-field="${storedFieldsScope}">
                                                <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                                <mvc:options items="${availableTargetGroups}" itemLabel="targetName" itemValue="id"/>
                                            </mvc:select>
                                        </div>
                                    </div>

                                    <%@include file="fragments/preview-add-to-test-run-btn.jspf" %>
                                </div>
                            </div>
                            
                            <%@include file="fragments/preview-test-run-container.jspf" %>

                            <%@include file="fragments/preview-header-mailing-info.jspf" %>
                        </c:if>

                        <c:choose>
                            <c:when test="${form.reload}">
                                <div class="col-12 tile-body__block">
                                    <div class="flex-center">
                                        <div class="flex-center flex-grow-1">
                                            <iframe class="mailing-preview-frame js-simple-iframe" name="previewFrame" src="${previewLink}" border="0"
                                                    data-max-width="${form.width}" data-media-query="${form.mediaQuery}" style="width: ${form.width}">
                                                Your Browser does not support IFRAMEs, please update!
                                            </iframe>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="progress loop"></div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="col-12">
                            <div class="notification-simple notification-simple--lg notification-simple--info">
                                <p><mvc:message code="error.mailing.mailinglist.deleted"/></p>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </mvc:form>
</div>
