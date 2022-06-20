<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ page import="org.agnitas.preview.ModeType" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="availablePreviewFormats" type="java.util.List"--%>
<%--@elvariable id="previewRecipients" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>

<c:set var="ACTION_PREVIEW_HEADER" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_HEADER%>" scope="page"/>
<c:set var="ACTION_PREVIEW" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW%>" scope="page"/>
<c:set var="ACTION_PREVIEW_SELECT" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_SELECT%>" scope="page"/>
<c:set var="ACTION_PDF_PREVIEW" value="<%=ComMailingSendActionBasic.ACTION_PDF_PREVIEW%>" scope="page"/>

<c:set var="EMAIL_TYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>" scope="page"/>
<c:set var="RECIPIENT_MODE" value="<%= ModeType.RECIPIENT %>"/>
<c:set var="RECIPIENT_MODE_CODE" value="<%= ModeType.RECIPIENT.getCode() %>"/>
<c:set var="TARGET_GROUP_MODE" value="<%= ModeType.TARGET_GROUP %>"/>
<c:set var="TARGET_GROUP_MODE_CODE" value="<%= ModeType.TARGET_GROUP.getCode() %>"/>

<%-- If preview is requested as a separate tile (to be embedded) then it must look the same for both regular and grid mailings --%>
<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid and not (mailingSendForm.previewForm.pure eq true)}" scope="request"/>
<c:set var="storedFieldsScope" value="${mailingSendForm.mailingID}"/>

<agn:agnForm action="/mailingsend.do" id="preview"
             data-controller="mailing-preview"
             data-initializer="mailing-preview"
             data-form="resource"
             data-resource-selector="#container-preview">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>
    <html:hidden property="isMailingGrid" value="${isMailingGrid}"/>
    <html:hidden property="previewForm.pure" value="${mailingSendForm.previewForm.pure}"/>
    <html:hidden property="previewForm.reload"/>

    <script id="config:mailing-preview" type="application/json">
        {
            "RECIPIENT_MODE": "${RECIPIENT_MODE_CODE}",
            "TARGET_MODE": "${TARGET_GROUP_MODE_CODE}"
        }
    </script>


<div id="container-preview" data-load-target="#preview-container">
    <c:set var="tileHeaderActions" scope="page">
        <li>
            <c:set var="pdfTooltip"><bean:message key="mailing.preview.pdf"/></c:set>
            <c:url var="previewPDFLink" value="/mailingsend.do">
                <c:param name="action" value="${ACTION_PDF_PREVIEW}"/>
                <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                <c:param name="previewForm.format" value="${mailingSendForm.previewForm.format}"/>
                <c:param name="previewForm.modeTypeId" value="${mailingSendForm.previewForm.modeTypeId}"/>
                <c:param name="previewForm.size" value="${mailingSendForm.previewForm.size}"/>
                <c:param name="previewForm.customerID" value="${mailingSendForm.previewForm.customerID}"/>
                <c:param name="previewForm.targetGroupId" value="${mailingSendForm.previewForm.targetGroupId}"/>
                <c:param name="previewForm.noImages" value="${mailingSendForm.previewForm.noImages}"/>
            </c:url>
            <agn:agnLink styleClass="link" href="${previewPDFLink}" data-tooltip="${pdfTooltip}" data-prevent-load="">
                <i class="icon icon-file-pdf-o"></i>
            </agn:agnLink>
        </li>
        <li>
            <c:set var="previewTooltip"><bean:message key="mailing.open_preview"/></c:set>
            <c:url var="previewLink" value="/mailingsend.do">
                <c:param name="action" value="${ACTION_PREVIEW}"/>
                <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                <c:param name="previewForm.format" value="${mailingSendForm.previewForm.format}"/>
                <c:param name="previewForm.modeTypeId" value="${mailingSendForm.previewForm.modeTypeId}"/>
                <c:param name="previewForm.size" value="${mailingSendForm.previewForm.size}"/>
                <c:param name="previewForm.customerID" value="${mailingSendForm.previewForm.customerID}"/>
                <c:param name="previewForm.targetGroupId" value="${mailingSendForm.previewForm.targetGroupId}"/>
                <c:param name="previewForm.noImages" value="${mailingSendForm.previewForm.noImages}"/>
            </c:url>
            <agn:agnLink styleClass="link" target="_blank" href="${previewLink}" data-tooltip="${previewTooltip}">
                <i class="icon icon-share-square-o"></i>
            </agn:agnLink>
        </li>
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <i class="icon icon-eye"></i>
                <span class="text"><bean:message key="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </a>
            <ul class="dropdown-menu">
                <li class="dropdown-header"><bean:message key="mailing.Graphics_Components"/></li>
                <li>
                    <label class="label">
                        <agn:agnCheckbox property="previewForm.noImages" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="predelivery.without.images"/></span>
                    </label>
                </li>
                <li class="divider"></li>
                <li class="dropdown-header"><bean:message key="default.Size"/></li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewForm.size" value="1" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="predelivery.desktop"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewForm.size" value="2" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobilePortrait"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewForm.size" value="3" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobileLandscape"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewForm.size" value="4" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletPortrait"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewForm.size" value="5" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletLandscape"/></span>
                    </label>
                </li>

                <c:if test="${not empty availablePreviewFormats}">
                <li class="divider"></li>
                <li class="dropdown-header"><bean:message key="action.Format"/></li>
                <li>
                    <c:forEach var="mediaTypeCode" items="${availablePreviewFormats}">
                       <c:choose>
                           <c:when test="${mediaTypeCode eq EMAIL_TYPE_CODE}">
                                <label class="label">
                                    <agn:agnRadio property="previewForm.format" value="${MailingPreviewHelper.INPUT_TYPE_TEXT}" data-stored-field="${storedFieldsScope}"/>
                                    <span class="label-text"><bean:message key="Text"/></span>
                                </label>
                                <logic:greaterThan name="mailingSendForm" property="emailFormat" value="0">
                                    <label class="label">
                                        <agn:agnRadio property="previewForm.format" value="${MailingPreviewHelper.INPUT_TYPE_HTML}" data-stored-field="${storedFieldsScope}"/>
                                        <span class="label-text"><bean:message key="HTML"/></span>
                                    </label>
                                </logic:greaterThan>
                           </c:when>
                           <c:otherwise>
                               <label class="label">
                                    <agn:agnRadio property="previewForm.format" value="${mediaTypeCode + 1}" data-stored-field="${storedFieldsScope}"/>
                                   <span class="label-text"><bean:message key='mailing.MediaType.${mediaTypeCode}'/></span>
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
                            <bean:message key="button.Refresh"/>
                        </button>
                    </p>
                </li>
            </ul>
        </li>
    </c:set>

    <c:set var="tileHeaderNav" scope="page">
        <li class="${mailingSendForm.previewForm.modeType == RECIPIENT_MODE ? 'active' : ''}">
            <a href="#" data-toggle-tab="#preview-recipientModeContent" data-action="toggle-tab-recipientMode">
                <bean:message key="mailing.preview.mode.recipient"/>
            </a>
        </li>
        <li class="${mailingSendForm.previewForm.modeType == TARGET_GROUP_MODE ? 'active' : ''}">
            <a href="#" data-toggle-tab="#preview-targetModeContent" data-action="toggle-tab-targetGroupMode">
                <bean:message key="target.Target"/>
            </a>
        </li>
    </c:set>

    <c:set var="previewHeader" scope="page">
        <c:if test="${mailingSendForm.previewForm.containsHeader}">
            <%-- trick to restore preview mode type and reload preview if necessary --%>
            <agn:agnText property="previewForm.modeTypeId" styleClass="hidden" data-action="change-stored-header-data"
                         data-stored-field="${storedFieldsScope}"/>

            <c:if test="${isMailingGrid}">
                <div class="tile">
                    <div class="tile-header">
                        <h2 class="headline">
                            <bean:message key="mailing.preview.mode"/>
                        </h2>

                        <ul class="tile-header-nav">${tileHeaderNav}</ul>
                    </div>
            </c:if>
                <div class="${isMailingGrid ? 'tile-content tile-content-forms' : 'mailing-preview-header'}">

                    <div id="preview-recipientModeContent" data-field="toggle-vis">
                        <div class="preview-settings-block">
                            <div class="preview-settings-header">
                                <h3><bean:message key="recipient.select"/></h3>
                            </div>
                            <div class="preview-settings-content">
                                <div class="preview-settings-group">
                                    <div class="col-xs-5 col-sm-4 col-md-4">
                                        <label for="useCustomerATID" class="radio-inline">
                                            <agn:agnRadio name="mailingSendForm" property="previewForm.useCustomerEmail" value="false"
                                                          styleId="useCustomerATID"
                                                          data-stored-field="${storedFieldsScope}"
                                                          data-action="change-preview-customer-options"
                                                          data-field-vis="" data-field-vis-hide="#recipient-manual-input" data-field-vis-show="#recipient-select"/>
                                            <bean:message key="recipient.TestSubscriber"/>
                                        </label>
                                    </div>
                                    <div id="recipient-select" class="col-xs-7 col-sm-7 col-md-7 col-lg-8">
                                        <select name="previewForm.customerATID"
                                                class="js-select form-control" style="width: 100%;"
                                                data-action="change-header-data">
                                            <option value="0" selected="${mailingSendForm.previewForm.customerATID == 0}">
                                                <bean:message key="default.select.email"/>
                                            </option>
                                            <c:forEach var="customer" items="${previewRecipients}">
                                                <c:set var="customerId" value="${customer.key}"/>
                                                <c:set var="customerName" value="${customer.value}"/>
                                                <c:set var="isSelected"
                                                       value="${mailingSendForm.previewForm.customerATID == customerId
                                                    or mailingSendForm.previewForm.customerID == customerId}"/>

                                                <option value="${customerId}" ${isSelected ? 'selected="selected"' : ''}>${customerName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="preview-settings-group">
                                    <div class="col-xs-5 col-sm-4 col-md-4">
                                        <label for="useCustomEmail" class="radio-inline">
                                            <agn:agnRadio name="mailingSendForm" property="previewForm.useCustomerEmail" value="true"
                                                          styleId="useCustomEmail"
                                                          data-stored-field="${storedFieldsScope}"
                                                          data-action="change-preview-customer-options"
                                                          data-field-vis="" data-field-vis-hide="#recipient-select" data-field-vis-show="#recipient-manual-input"/>
                                            <bean:message key="mailing.preview.input"/>
                                        </label>
                                    </div>

                                    <div  id="recipient-manual-input" class="col-xs-7 col-sm-7 col-md-7 col-lg-8">
                                        <div class="inline-block" style="width: 100%;">
                                            <div class="input-group-controls">
                                                <agn:agnText name="mailingSendForm" property="previewForm.customerEmail"
                                                             styleClass="form-control" data-stored-field="${storedFieldsScope}"/>
                                            </div>
                                            <div class="input-group-btn">
                                                <button type="button" id="btnCustomEmailRefresh" class="btn btn-regular"
                                                        data-action="refresh-preview"><bean:message key="default.enter.email"/></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div id="preview-targetModeContent">
                        <div class="preview-settings-block" data-field="toggle-vis">
                            <div class="preview-settings-header">
                                <h3><bean:message key="recipient.select"/></h3>
                            </div>
                            <div class="preview-settings-content">
                                <div class="preview-settings-group">
                                    <agn:agnSelect name="mailingSendForm" property="previewForm.targetGroupId" style="max-width: 350px;"
                                       data-action="change-stored-header-data"
                                       styleClass="js-select form-control"
                                       data-stored-field="${storedFieldsScope}">
                                        <agn:agnOption value="0"><bean:message key="statistic.all_subscribers"/></agn:agnOption>
                                        <c:forEach items="${availableTargetGroups}" var="targetGroup">
                                            <agn:agnOption value="${targetGroup.id}">${targetGroup.targetName}</agn:agnOption>
                                        </c:forEach>
                                    </agn:agnSelect>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            <c:if test="${isMailingGrid}">
                </div>
            </c:if>
            
            <c:if test="${empty mailingListExist or mailingListExist}">
                <c:import url="/mailingsend.do">
                    <c:param name="action" value="${ACTION_PREVIEW_HEADER}"/>
                    <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                    <c:param name="previewForm.modeTypeId" value="${mailingSendForm.previewForm.modeTypeId}"/>
                    <c:param name="previewForm.customerID" value="${mailingSendForm.previewForm.customerID}"/>
                    <c:param name="previewForm.targetGroupId" value="${mailingSendForm.previewForm.targetGroupId}"/>
                </c:import>
            </c:if>
        </c:if>
    </c:set>

    <tiles:insert page="template.jsp">
        <%-- There're no footer items --%>

        <c:if test="${isMailingGrid}">
            <tiles:put name="header" type="string">
                <ul class="tile-header-nav">
                    <%--<div class="headline">
                        <i class="icon icon-th-list"></i>
                    </div>--%>

                    <!-- Tabs BEGIN -->
                    <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    <!-- Tabs END -->
                </ul>

                    <c:if test="${empty mailingListExist or mailingListExist}">
                        <ul class="tile-header-actions">${tileHeaderActions}</ul>
                    </c:if>
            </tiles:put>
        </c:if>

        <tiles:put name="content" type="string">
            <c:if test="${not isMailingGrid}">
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <c:choose>
                            <c:when test="${mailingSendForm.previewForm.containsHeader}">
                                <bean:message key="mailing.preview.mode"/>
                            </c:when>
                            <c:otherwise>
                                <bean:message key="default.Preview"/>
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
                            <c:if test="${not mailingSendForm.previewForm.reload}">
                                <div class="progress loop" id="progress_bar" style="width: 100%"></div>
                            </c:if>
                            <c:if test="${mailingSendForm.previewForm.reload}">
                                <div>
                                    <div class="mailing-preview-scroller center-block" id="preview-container">
                                        <c:url var="previewIFrame" value="/mailingsend.do">
                                            <c:param name="action" value="${ACTION_PREVIEW}"/>
                                            <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                                            <c:param name="previewForm.format" value="${mailingSendForm.previewForm.format}"/>
                                            <c:param name="previewForm.modeTypeId" value="${mailingSendForm.previewForm.modeTypeId}"/>
                                            <c:param name="previewForm.size" value="${mailingSendForm.previewForm.size}"/>
                                            <c:param name="previewForm.customerID" value="${mailingSendForm.previewForm.customerID}"/>
                                            <c:param name="previewForm.targetGroupId" value="${mailingSendForm.previewForm.targetGroupId}"/>
                                            <c:param name="previewForm.noImages" value="${mailingSendForm.previewForm.noImages}"/>
                                        </c:url>
                                        <iframe class="mailing-preview-frame js-simple-iframe" name="previewFrame"
                                                src="${previewIFrame}" border="0"
                                                data-max-width="${mailingSendForm.previewForm.width}"
                                                data-media-query="${mailingSendForm.previewForm.mediaQuery}"
                                                style="width: ${mailingSendForm.previewForm.width}px;">
                                            Your Browser does not support IFRAMEs, please update!
                                        </iframe>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                        </c:when>
                        <c:otherwise>
                            <div class="tile-content-padded">
                                <h3><bean:message key="error.mailing.mailinglist.deleted"/></h3>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    </div>
            <c:if test="${not isMailingGrid}">
                </div>
            </div>
            </c:if>
        </tiles:put>
    </tiles:insert>
</agn:agnForm>

<c:if test="${not isMailingGrid}">
    <div class="clearfix"></div>
</c:if>
</div>
