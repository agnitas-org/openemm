<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>
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

<c:set var="ACTION_PREVIEW_HEADER" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_HEADER%>" scope="page"/>
<c:set var="ACTION_PREVIEW" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW%>" scope="page"/>
<c:set var="ACTION_PREVIEW_SELECT" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_SELECT%>" scope="page"/>
<c:set var="ACTION_PDF_PREVIEW" value="<%=ComMailingSendActionBasic.ACTION_PDF_PREVIEW%>" scope="page"/>

<c:set var="EMAIL_TYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>" scope="page"/>

<%-- If preview is requested as a separate tile (to be embedded) then it must look the same for both regular and grid mailings --%>
<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid and not (param.previewSelectPure eq true)}" scope="request"/>
<c:set var="storedFieldsScope" value="${mailingSendForm.mailingID}"/>

<agn:agnForm action="/mailingsend.do" id="preview"
             data-controller="mailing-preview"
             data-initializer="mailing-preview"
             data-form="resource"
             data-resource-selector="#container-preview">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>
    <html:hidden property="isMailingGrid" value="${isMailingGrid}"/>
    <html:hidden property="previewSelectPure" value="${param.previewSelectPure}"/>
    <html:hidden property="reloadPreview"/>

<div id="container-preview" data-load-target="#preview-container">
    <!--XYZ: ${mailingSendForm.previewFormat}-->
    <c:set var="tileHeaderActions" scope="page">
        <li>
            <c:set var="pdfTooltip"><bean:message key="mailing.preview.pdf"/></c:set>
            <c:url var="previewPDFLink" value="/mailingsend.do">
                <c:param name="action" value="${ACTION_PDF_PREVIEW}"/>
                <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                <c:param name="previewFormat" value="${mailingSendForm.previewFormat}"/>
                <c:param name="previewSize" value="${mailingSendForm.previewSize}"/>
                <c:param name="previewCustomerID" value="${mailingSendForm.previewCustomerID}"/>
                <c:param name="noImages" value="${mailingSendForm.noImages}"/>
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
                <c:param name="previewFormat" value="${mailingSendForm.previewFormat}"/>
                <c:param name="previewSize" value="${mailingSendForm.previewSize}"/>
                <c:param name="previewCustomerID" value="${mailingSendForm.previewCustomerID}"/>
                <c:param name="noImages" value="${mailingSendForm.noImages}"/>
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
                        <agn:agnCheckbox property="noImages" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="predelivery.without.images"/></span>
                    </label>
                </li>
                <li class="divider"></li>
                <li class="dropdown-header"><bean:message key="default.Size"/></li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="1" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="predelivery.desktop"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="2" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobilePortrait"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="3" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobileLandscape"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="4" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletPortrait"/></span>
                    </label>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="5" data-stored-field="${storedFieldsScope}"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletLandscape"/></span>
                    </label>
                </li>

                <c:set var="showMediaTypeRadioButtons" value="${not empty mailingSendForm.availablePreviewFormats}"/>

                <li class="divider"></li>
                <li class="dropdown-header"><bean:message key="action.Format"/></li>
                <li>
                    <c:forEach var="mediaTypeCode" items="${mailingSendForm.availablePreviewFormats}">
                       <c:choose>
                           <c:when test="${mediaTypeCode eq EMAIL_TYPE_CODE}">
                                <label class="label">
                                    <agn:agnRadio property="previewFormat" value="${MailingPreviewHelper.INPUT_TYPE_TEXT}" data-stored-field="${storedFieldsScope}"/>
                                    <span class="label-text"><bean:message key="Text"/></span>
                                </label>
                                <logic:greaterThan name="mailingSendForm" property="emailFormat" value="0">
                                    <label class="label">
                                        <agn:agnRadio property="previewFormat" value="${MailingPreviewHelper.INPUT_TYPE_HTML}" data-stored-field="${storedFieldsScope}"/>
                                        <span class="label-text"><bean:message key="HTML"/></span>
                                    </label>
                                </logic:greaterThan>
                           </c:when>
                           <c:otherwise>
                               <label class="label">
                                    <agn:agnRadio property="previewFormat" value="${mediaTypeCode + 1}" data-stored-field="${storedFieldsScope}"/>
                                   <span class="label-text"><bean:message key='mailing.MediaType.${mediaTypeCode}'/></span>
                                </label>
                           </c:otherwise>
                       </c:choose>
                    </c:forEach>
                </li>

                <li class="divider"></li>
                <li>
                    <p>
                        <button type="button" class="btn btn-block btn-primary btn-regular" data-action="refresh-preview"><i class="icon icon-refresh"></i> <bean:message key="button.Refresh"/></button>
                    </p>
                </li>
            </ul>
        </li>
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

                    <ul class="tile-header-actions">${tileHeaderActions}</ul>
            </tiles:put>
        </c:if>

        <tiles:put name="content" type="string">
            <c:if test="${not isMailingGrid}">
            <div class="tile">
                <div class="tile-header">

                    <h2 class="headline"><bean:message key="default.Preview"/></h2>
                    <ul class="tile-header-actions">${tileHeaderActions}</ul>

                </div>
                <div class="tile-content">
            </c:if>
                    <div id="preview-contents">
                        <c:if test="${mailingSendForm.previewFormatContainsHeader}">
                            <c:import url="/mailingsend.do?action=${ACTION_PREVIEW_HEADER}&previewCustomerID=${mailingSendForm.previewCustomerID}&mailingID=${mailingSendForm.mailingID}"/>
                        </c:if>
                        <div class="${isMailingGrid ? 'tile-content-padded' : 'mailing-preview-wrapper'}">
                            <c:if test="${not mailingSendForm.reloadPreview}">
                                <div class="progress loop" id="progress_bar" style="width: 100%"></div>
                            </c:if>
                            <c:if test="${mailingSendForm.reloadPreview}">
                                <div>
                                    <div class="mailing-preview-scroller center-block" id="preview-container">
                                        <c:url var="previewIFrame" value="/mailingsend.do">
                                            <c:param name="action" value="${ACTION_PREVIEW}"/>
                                            <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                                            <c:param name="previewFormat" value="${mailingSendForm.previewFormat}"/>
                                            <c:param name="previewSize" value="${mailingSendForm.previewSize}"/>
                                            <c:param name="previewCustomerID" value="${mailingSendForm.previewCustomerID}"/>
                                            <c:param name="noImages" value="${mailingSendForm.noImages}"/>
                                        </c:url>
                                        <iframe class="mailing-preview-frame js-simple-iframe" name="previewFrame"
                                                src="${previewIFrame}" border="0"
                                                data-max-width="${mailingSendForm.previewWidth}"
                                                data-media-query="${mailingSendForm.mediaQuery}"
                                                style="width: ${mailingSendForm.previewWidth}px;">
                                            Your Browser does not support IFRAMEs, please update!
                                        </iframe>
                                    </div>
                                </div>
                            </c:if>
                        </div>
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
