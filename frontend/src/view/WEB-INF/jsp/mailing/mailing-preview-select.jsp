<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.*, org.agnitas.web.*" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>

<c:set var="ACTION_PREVIEW_HEADER" value="<%= MailingSendAction.ACTION_PREVIEW_HEADER %>" scope="page"/>
<c:set var="ACTION_PREVIEW" value="<%= MailingSendAction.ACTION_PREVIEW %>" scope="page"/>

<%-- If preview is requested as a separate tile (to be embedded) then it must look the same for both regular and grid mailings --%>
<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid and not (param.previewSelectPure eq true)}" scope="request"/>
<c:set var="storedFieldsScope" value="${mailingSendForm.mailingID}"/>

<%
    int prevX = 800 + 2;
    int prevY = 600;
    String mediaQuery = "true";
    ComMailingSendForm aForm = null;

    aForm = (ComMailingSendForm) request.getAttribute("mailingSendForm");

    switch (aForm.getPreviewSize()) {
        case 1:
            prevX = 1022 + 2;
            prevY = 600;
            mediaQuery = "false";
            break;
        case 2:
            prevX = 320 + 2;
            prevY = 356;
            mediaQuery = "true";
            break;
        case 3:
            prevX = 356 + 2;
            prevY = 320;
            mediaQuery = "true";
            break;
        case 4:
            prevX = 768 + 2;
            prevY = 946;
            mediaQuery = "true";
            break;
        case 5:
            prevX = 1024 + 2;
            prevY = 690;
            mediaQuery = "false";
            break;
        default:
            aForm.setPreviewSize(1);
            prevX = 800 + 2;
            prevY = 600;
            mediaQuery = "false";
            break;
    }
%>


<agn:agnForm action="/mailingsend" id="preview" data-form="" data-controller="mailing-preview">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>
    <html:hidden property="isMailingGrid" value="${isMailingGrid}"/>
    <html:hidden property="previewSelectPure" value="${param.previewSelectPure}"/>

    <c:set var="tileHeaderActions" scope="page">
        <li>
            <c:set var="pdfTooltip">
                <bean:message key="mailing.preview.pdf"/>
            </c:set>
            <agn:agnLink styleClass="link" page='<%= "/mailingsend.do?action=" + ComMailingSendAction.ACTION_PDF_PREVIEW +
                            "&mailingID=" + aForm.getMailingID() + "&previewFormat=" + aForm.getPreviewFormat()  +
                            "&previewSize=" + aForm.getPreviewSize()+ "&previewCustomerID=" + aForm.getPreviewCustomerID() +
                            "&noImages=" + aForm.isNoImages() %>' data-tooltip="${pdfTooltip}" data-prevent-load="">
                <i class="icon icon-file-pdf-o"></i>
            </agn:agnLink>
        </li>
        <li>
            <c:set var="previewTooltip">
                <bean:message key="mailing.open_preview"/>
            </c:set>
            <agn:agnLink styleClass="link" target="_blank" page='<%= "/mailingsend.do?action=" + MailingSendAction.ACTION_PREVIEW +
                             "&mailingID=" + aForm.getMailingID() + "&previewFormat=" + aForm.getPreviewFormat()
                             + "&previewSize=" + aForm.getPreviewSize()+ "&previewCustomerID=" + aForm.getPreviewCustomerID() +
                             "&noImages=" + aForm.isNoImages() %>' data-tooltip="${previewTooltip}">
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

                <c:set var="showMediaTypeRadioButtons" value="false"/>
                <c:forEach var="mediaTypeCode" items="${mailingSendForm.availablePreviewFormats}">
                    <c:if test="${mediaTypeCode >= 0 and mediaTypeCode <= 4}">
                        <c:set var="showMediaTypeRadioButtons" value="true"/>
                    </c:if>
                </c:forEach>

                <c:if test="${showMediaTypeRadioButtons}">
                    <li class="divider"></li>
                    <li class="dropdown-header"><bean:message key="action.Format"/></li>
                    <li>
                        <c:forEach var="mediaTypeCode" items="${mailingSendForm.availablePreviewFormats}">
                           <c:if test="${mediaTypeCode >= 0 and mediaTypeCode <= 4}">
                               <c:choose>
                                   <c:when test="${mediaTypeCode == 0}">
                                        <label class="label">
                                            <agn:agnRadio property="previewFormat" value="0" data-stored-field="${storedFieldsScope}"/>
                                            <span class="label-text"><bean:message key="Text"/></span>
                                        </label>
                                        <logic:greaterThan name="mailingSendForm" property="emailFormat" value="0">
                                            <label class="label">
                                                <agn:agnRadio property="previewFormat" value="1" data-stored-field="${storedFieldsScope}"/>
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
                           </c:if>
                        </c:forEach>
                    </li>
                </c:if>

                <li class="divider"></li>
                <li>
                    <p>
                        <button type="button" class="btn btn-block btn-primary btn-regular" data-form-submit><i class="icon icon-refresh"></i> <bean:message key="button.Refresh"/></button>
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
                    <c:if test="${mailingSendForm.previewFormat eq 0 or mailingSendForm.previewFormat eq 1 or mailingSendForm.previewFormat eq 2 or mailingSendForm.previewFormat eq 6}">
                        <c:import url="/mailingsend.do?action=${ACTION_PREVIEW_HEADER}&previewCustomerID=${mailingSendForm.previewCustomerID}&mailingID=${mailingSendForm.mailingID}"/>
                    </c:if>
                    <div class="${isMailingGrid ? 'tile-content-padded' : 'mailing-preview-wrapper'}">
                        <div class="progress loop" id="progress_bar" style="width: 100%"></div>
                        <div>
                            <div class="mailing-preview-scroller center-block hidden" id="preview-container">
                                <iframe class="mailing-preview-frame js-simple-iframe" name="previewFrame"
                                        src="<html:rewrite page='/mailingsend.do?action=${ACTION_PREVIEW}&mailingID=${mailingSendForm.mailingID}&previewFormat=${mailingSendForm.previewFormat}&previewSize=${mailingSendForm.previewSize}&previewCustomerID=${mailingSendForm.previewCustomerID}&noImages=${mailingSendForm.noImages}'/>"
                                        border="0"
                                        data-max-width="<%= prevX %>"
                                        data-media-query="<%= mediaQuery %>"
                                        style="width: <%= prevX %>px;"
                                        onLoad="
                                            var $progress = $('#progress_bar');
                                            var $preview = $('#preview-container');
                                            $progress.show();
                                            $preview.addClass('hidden');
                                            $preview.find('iframe') .ready(function() {
                                              $progress.hide();
                                              $preview.removeClass('hidden');
                                            })">
                                    Your Browser does not support IFRAMEs, please update!
                                </iframe>
                            </div>
                        </div>
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
