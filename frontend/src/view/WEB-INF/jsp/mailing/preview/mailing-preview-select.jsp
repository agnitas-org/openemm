<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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
<%--@elvariable id="recipientsAvailableForTestRun" type="java.util.List<com.agnitas.beans.impl.ComRecipientLiteImpl>"--%>

<c:set var="EMAIL_TYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>" scope="page"/>
<c:set var="RECIPIENT_MODE" value="<%= ModeType.RECIPIENT %>"/>
<c:set var="RECIPIENT_MODE_CODE" value="<%= ModeType.RECIPIENT.getCode() %>"/>
<c:set var="TARGET_GROUP_MODE" value="<%= ModeType.TARGET_GROUP %>"/>
<c:set var="TARGET_GROUP_MODE_CODE" value="<%= ModeType.TARGET_GROUP.getCode() %>"/>
<c:set var="MOBILE_PORTRAIT" value="<%= Preview.Size.MOBILE_PORTRAIT.getValue() %>"/>
<c:set var="MOBILE_LANDSCAPE" value="<%= Preview.Size.MOBILE_LANDSCAPE.getValue() %>"/>

<%-- If preview is requested as a separate tile (to be embedded) then it must look the same for both regular and grid mailings --%>
<c:set var="isMailingGrid" value="${form.isMailingGrid and not (form.pure eq true)}" scope="request"/>
<c:set var="storedFieldsScope" value="${form.mailingId}"/>

<mvc:form id="preview" servletRelativeAction="/mailing/preview/${form.mailingId}/view.action" modelAttribute="form" data-controller="mailing-preview"
          data-initializer="mailing-preview" data-form="resource" data-resource-selector="#container-preview">

    <mvc:hidden path="mailingId" />
    <mvc:hidden path="pure" />
    <mvc:hidden path="reload" />

    <script id="config:mailing-preview" type="application/json">
        {
            "MAILING_ID": "${form.mailingId}",
            "RECIPIENT_MODE": "${RECIPIENT_MODE_CODE}",
            "TARGET_MODE": "${TARGET_GROUP_MODE_CODE}"
        }
    </script>

    <c:url var="previewLink" value="/mailing/preview/view-content.action">
        <c:param name="mailingId" value="${form.mailingId}"/>
        <c:param name="format" value="${form.format}"/>
        <c:param name="modeTypeId" value="${form.modeTypeId}"/>
        <c:param name="size" value="${form.size}"/>
        <c:param name="customerID" value="${form.customerID}"/>
        <c:param name="targetGroupId" value="${form.targetGroupId}"/>
        <c:param name="noImages" value="${form.noImages}"/>
    </c:url>

    <div id="container-preview" data-load-target="#preview-container">
    <c:set var="tileHeaderActions" scope="page">
        <c:if test="${emm:contains(availablePreviewFormats, EMAIL_TYPE_CODE)}">
            <li>
                <c:url var="downloadHtmlLink" value="/mailing/preview/html.action">
                    <c:param name="mailingId" value="${form.mailingId}"/>
                    <c:param name="format" value="${form.format}"/>
                    <c:param name="modeTypeId" value="${form.modeTypeId}"/>
                    <c:param name="size" value="${form.size}"/>
                    <c:param name="customerID" value="${form.customerID}"/>
                    <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                    <c:param name="noImages" value="${form.noImages}"/>
                </c:url>
                <a href="${downloadHtmlLink}" class="link" data-tooltip="<mvc:message code="default.HTML.code"/>" data-prevent-load="">
                    <i class="icon icon-file-code-o"></i>
                </a>
            </li>
        </c:if>
        <li>
            <c:set var="pdfTooltip"><mvc:message code="mailing.preview.pdf"/></c:set>
            <c:url var="previewPDFLink" value="/mailing/preview/pdf.action">
                <c:param name="mailingId" value="${form.mailingId}"/>
                <c:param name="format" value="${form.format}"/>
                <c:param name="modeTypeId" value="${form.modeTypeId}"/>
                <c:param name="size" value="${form.size}"/>
                <c:param name="customerID" value="${form.customerID}"/>
                <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                <c:param name="noImages" value="${form.noImages}"/>
            </c:url>

            <a href="${previewPDFLink}" class="link" data-tooltip="${pdfTooltip}" data-prevent-load="">
                <i class="icon icon-file-pdf-o"></i>
            </a>
        </li>
        <li>
            <c:set var="previewTooltip"><mvc:message code="mailing.open_preview"/></c:set>
            <a href="${previewLink}" class="link" target="_blank" data-tooltip="${previewTooltip}">
                <i class="icon icon-share-square-o"></i>
            </a>
        </li>
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <i class="icon icon-eye"></i>
                <span class="text"><mvc:message code="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </a>
            <ul class="dropdown-menu">
                <li class="dropdown-header"><mvc:message code="mailing.Graphics_Components"/></li>
                <li>
                    <label class="label">
                        <mvc:checkbox path="noImages" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><mvc:message code="predelivery.without.images"/></span>
                    </label>
                </li>
                <li class="divider"></li>
                <li class="dropdown-header"><mvc:message code="default.Size"/></li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="size" value="1" data-stored-field="${storedFieldsScope}" />
                        <span class="label-text"><mvc:message code="predelivery.desktop"/></span>
                    </label>
                    <label class="label">
                        <mvc:radiobutton path="size" value="2" data-stored-field="${storedFieldsScope}" />
                        <span class="label-text"><mvc:message code="mailing.PreviewSize.MobilePortrait"/></span>
                    </label>
                    <label class="label">
                        <mvc:radiobutton path="size" value="3" data-stored-field="${storedFieldsScope}" />
                        <span class="label-text"><mvc:message code="mailing.PreviewSize.MobileLandscape"/></span>
                    </label>
                    <label class="label">
                        <mvc:radiobutton path="size" value="4" data-stored-field="${storedFieldsScope}" />
                        <span class="label-text"><mvc:message code="mailing.PreviewSize.TabletPortrait"/></span>
                    </label>
                    <label class="label">
                        <mvc:radiobutton path="size" value="5" data-stored-field="${storedFieldsScope}" />
                        <span class="label-text"><mvc:message code="mailing.PreviewSize.TabletLandscape"/></span>
                    </label>
                </li>

                <c:if test="${not empty availablePreviewFormats}">
                    <li class="divider"></li>
                    <li class="dropdown-header"><mvc:message code="action.Format"/></li>
                    <li>
                        <c:forEach var="mediaTypeCode" items="${availablePreviewFormats}">
                            <c:choose>
                                <c:when test="${mediaTypeCode eq EMAIL_TYPE_CODE}">
                                    <label class="label">
                                        <mvc:radiobutton path="format" value="${MailingPreviewHelper.INPUT_TYPE_TEXT}" data-stored-field="${storedFieldsScope}" />
                                        <span class="label-text"><mvc:message code="Text"/></span>
                                    </label>
                                    <c:if test="${form.emailFormat gt 0}">
                                        <label class="label">
                                            <mvc:radiobutton path="format" value="${MailingPreviewHelper.INPUT_TYPE_HTML}" data-stored-field="${storedFieldsScope}" />
                                            <span class="label-text"><mvc:message code="HTML"/></span>
                                        </label>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <label class="label">
                                        <mvc:radiobutton path="format" value="${mediaTypeCode + 1}" data-stored-field="${storedFieldsScope}" />
                                        <span class="label-text"><mvc:message code='mailing.MediaType.${mediaTypeCode}'/></span>
                                    </label>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </li>
                </c:if>

                <li class="divider"></li>
                <li>
                    <p>
                        <button type="button" class="btn btn-block btn-primary btn-regular" data-action="refresh-preview">
                            <i class="icon icon-refresh"></i>
                            <mvc:message code="button.Refresh"/>
                        </button>
                    </p>
                </li>
            </ul>
        </li>
    </c:set>

    <c:set var="tileHeaderNav" scope="page">
        <li class="${form.modeType == RECIPIENT_MODE ? 'active' : ''}">
            <a href="#" data-toggle-tab="#preview-recipientModeContent" data-action="toggle-tab-recipientMode">
                <mvc:message code="mailing.preview.mode.recipient"/>
            </a>
        </li>
        <li class="${form.modeType == TARGET_GROUP_MODE ? 'active' : ''}">
            <a href="#" data-toggle-tab="#preview-targetModeContent" data-action="toggle-tab-targetGroupMode">
                <mvc:message code="target.Target"/>
            </a>
        </li>
    </c:set>

    <c:set var="previewHeader" scope="page">
        <c:if test="${form.containsHeader}">
            <%-- trick to restore preview mode type and reload preview if necessary --%>
            <mvc:text path="modeTypeId" cssClass="hidden" data-action="change-stored-header-data" data-stored-field="${storedFieldsScope}"/>

            <c:if test="${isMailingGrid}">
                <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <mvc:message code="mailing.preview.mode"/>
                    </h2>

                    <ul class="tile-header-nav">${tileHeaderNav}</ul>
                </div>
            </c:if>
            <div class="${isMailingGrid ? 'tile-content tile-content-forms' : 'mailing-preview-header'}">

                <div id="preview-recipientModeContent" class="row" data-field="toggle-vis">
                    <div class="col-sm-6 preview-settings-block">
                        <div class="preview-settings-header">
                            <h3><mvc:message code="recipient.select"/></h3>
                        </div>
                        <div class="preview-settings-content">
                            <div class="preview-settings-group">
                                <div class="col-xs-2 col-sm-3 col-md-3">
                                    <label for="useCustomerATID" class="radio-inline">
                                        <mvc:radiobutton path="useCustomerEmail" value="false"
                                                      id="useCustomerATID"
                                                      data-stored-field="${storedFieldsScope}"
                                                      data-action="change-preview-customer-options"
                                                      data-field-vis="" data-field-vis-hide="#recipient-manual-input"
                                                      data-field-vis-show="#recipient-select,#personalized-test-run-container"/>

                                        <mvc:message code="recipient.TestSubscriber"/>
                                    </label>
                                </div>
                                <div id="recipient-select" class="col-xs-10 col-sm-9 col-md-9 col-lg-9">
                                    <div class="input-group">
                                        <div class="input-group-controls">
                                            <select name="customerATID" class="js-select form-control"
                                                    data-action="change-header-data" data-sort="alphabetic">
                                                <option value="0" selected="${form.customerATID == 0}" data-fix-position>
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
                                        <div class="input-group-btn">
                                            <%@include file="fragments/preview-add-to-test-run-btn.jspf" %>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="preview-settings-group">
                                <div class="col-xs-2 col-sm-3 col-md-3">
                                    <label for="useCustomEmail" class="radio-inline">
                                        <mvc:radiobutton name="mailingSendForm" path="useCustomerEmail" value="true"
                                                      id="useCustomEmail"
                                                      data-stored-field="${storedFieldsScope}"
                                                      data-action="change-preview-customer-options"
                                                      data-field-vis="" data-field-vis-hide="#recipient-select,#personalized-test-run-container"
                                                      data-field-vis-show="#recipient-manual-input"/>

                                        <mvc:message code="mailing.preview.input"/>
                                    </label>
                                </div>

                                <div id="recipient-manual-input" class="col-xs-10 col-sm-9 col-md-9 col-lg-9">
                                    <div class="inline-block" style="width: 100%;">
                                        <div class="input-group-controls">
                                            <mvc:text path="customerEmail" cssClass="form-control" data-stored-field="${storedFieldsScope}" />
                                        </div>
                                        <div class="input-group-btn">
                                            <button type="button" id="btnCustomEmailRefresh" class="btn btn-regular" data-action="refresh-preview">
                                                <mvc:message code="default.enter.email"/>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 preview-settings-block hidden" id="personalized-test-run-container" style="display: flex">
                        <%@include file="fragments/preview-test-run-container.jspf" %>
                    </div>
                </div>

                <div id="preview-targetModeContent">
                    <div class="preview-settings-block" data-field="toggle-vis">
                        <div class="preview-settings-header">
                            <h3><mvc:message code="recipient.select"/></h3>
                        </div>
                        <div class="preview-settings-content">
                            <div class="preview-settings-group">
                                <mvc:select path="targetGroupId" style="max-width: 350px;" data-action="change-stored-header-data"
                                            cssClass="js-select form-control" data-stored-field="${storedFieldsScope}">
                                    <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                    <mvc:options items="${availableTargetGroups}" itemLabel="targetName" itemValue="id"/>
                                </mvc:select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <c:if test="${isMailingGrid}">
                </div>
            </c:if>

            <c:if test="${empty mailingListExist or mailingListExist}">
                <%@ include file="fragments/preview-header-mailing-info.jspf" %>
            </c:if>
        </c:if>
    </c:set>

    <tiles:insertTemplate template="/WEB-INF/jsp/mailing/template.jsp">
        <%-- There're no footer items --%>

        <c:if test="${isMailingGrid}">
            <tiles:putAttribute name="header" type="string">
                <ul class="tile-header-nav">
                    <!-- Tabs BEGIN -->
                    <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    <!-- Tabs END -->
                </ul>

                <c:if test="${empty mailingListExist or mailingListExist}">
                    <ul class="tile-header-actions">${tileHeaderActions}</ul>
                </c:if>
            </tiles:putAttribute>
        </c:if>

        <tiles:putAttribute name="content" type="string">
            <c:if test="${not isMailingGrid}">
                <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <c:choose>
                            <c:when test="${form.containsHeader}">
                                <mvc:message code="mailing.preview.mode"/>
                            </c:when>
                            <c:otherwise>
                                <mvc:message code="default.Preview"/>
                            </c:otherwise>
                        </c:choose>
                    </h2>

                    <c:if test="${empty mailingListExist or mailingListExist}">
                        <ul class="tile-header-nav">${tileHeaderNav}</ul>
                        <ul class="tile-header-actions">${tileHeaderActions}</ul>
                    </c:if>

                </div>
                <div class="tile-content">
            </c:if>
            <div id="preview-contents">
                <c:choose>
                    <c:when test="${empty mailingListExist or mailingListExist}">
                        ${previewHeader}

                        <div class="${isMailingGrid ? 'tile-content-padded' : 'mailing-preview-wrapper'}">
                            <c:if test="${not form.reload}">
                                <div class="progress loop" id="progress_bar" style="width: 100%"></div>
                            </c:if>
                            <c:if test="${form.reload}">
                                <div>
                                    <div class="mailing-preview-scroller center-block" id="preview-container">
                                        <iframe class="mailing-preview-frame js-simple-iframe" name="previewFrame"
                                                src="${previewLink}" border="0"
                                                data-max-width="${form.width}"
                                                data-media-query="${form.mediaQuery}"
                                                style="width: ${form.width}">
                                            Your Browser does not support IFRAMEs, please update!
                                        </iframe>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="tile-content-padded">
                            <h3><mvc:message code="error.mailing.mailinglist.deleted"/></h3>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:if test="${not isMailingGrid}">
                </div>
                </div>
            </c:if>
        </tiles:putAttribute>
    </tiles:insertTemplate>
</mvc:form>

<c:if test="${not isMailingGrid}">
    <div class="clearfix"></div>
</c:if>
</div>
