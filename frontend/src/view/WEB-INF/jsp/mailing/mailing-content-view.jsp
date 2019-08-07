<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>
<%@ page import="com.agnitas.web.ComMailingContentForm" %>
<%@ page import="com.agnitas.web.ComMailingSendAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingContentForm" type="com.agnitas.web.ComMailingContentForm"--%>

<c:set var="ACTION_VIEW_TEXTBLOCK" value="<%= ComMailingContentAction.ACTION_VIEW_TEXTBLOCK %>"/>
<c:set var="ACTION_SAVE_TEXTBLOCK" value="<%= ComMailingContentAction.ACTION_SAVE_TEXTBLOCK %>"/>
<c:set var="ACTION_SAVE_TEXTBLOCK_AND_BACK" value="<%= ComMailingContentAction.ACTION_SAVE_TEXTBLOCK_AND_BACK %>"/>
<c:set var="ACTION_DELETE_TEXTBLOCK" value="<%= ComMailingContentAction.ACTION_DELETE_TEXTBLOCK %>"/>
<c:set var="ACTION_CHANGE_ORDER_TOP" value="<%= ComMailingContentAction.ACTION_CHANGE_ORDER_TOP %>"/>
<c:set var="ACTION_CHANGE_ORDER_UP" value="<%= ComMailingContentAction.ACTION_CHANGE_ORDER_UP %>"/>
<c:set var="ACTION_CHANGE_ORDER_BOTTOM" value="<%= ComMailingContentAction.ACTION_CHANGE_ORDER_BOTTOM %>"/>
<c:set var="ACTION_CHANGE_ORDER_DOWN" value="<%= ComMailingContentAction.ACTION_CHANGE_ORDER_DOWN %>"/>
<c:set var="ACTION_PREVIEW_SELECT" value="<%= ComMailingSendAction.ACTION_PREVIEW_SELECT %>"/>
<c:set var="ACTION_ADD_TEXTBLOCK" value="<%= ComMailingContentAction.ACTION_ADD_TEXTBLOCK %>"/>
<c:set var="ACTION_ADD_TEXTBLOCK_AND_BACK" value="<%= ComMailingContentAction.ACTION_ADD_TEXTBLOCK_AND_BACK %>"/>
<c:set var="ACTION_VIEW_CONTENT" value="<%= ComMailingContentAction.ACTION_VIEW_CONTENT %>"/>

<c:set var="MAILING_CONTENT_HTML_CODE" value="<%= ComMailingContentForm.MAILING_CONTENT_HTML_CODE %>"/>
<c:set var="MAILING_CONTENT_HTML_EDITOR" value="<%= ComMailingContentForm.MAILING_CONTENT_HTML_EDITOR %>"/>

<c:set var="PREVIEW_FORMAT_HTML" value="<%= Mailing.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= Mailing.INPUT_TYPE_TEXT %>"/>

<c:set var="isEditable" value="${not mailingContentForm.worldMailingSend}" scope="page"/>
<c:set var="isHTMLVersion" value="${mailingContentForm.dynName eq 'HTML-Version'}" scope="page"/>
<c:set var="isMailingGrid" value="${mailingContentForm.gridTemplateId > 0}" scope="request"/>

<emm:ShowByPermission token="mailing.content.change.always">
    <c:set var="isEditable" value="true" scope="page"/>
</emm:ShowByPermission>


<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:put name="content" type="string">
        <c:if test="${isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>
            <div class="row" data-autosave-scope='mailing-components/${mailingContentForm.mailingID}/${mailingContentForm.dynNameID}'>
                <div class="col-xs-12" data-view-split="col-md-6 split-1-1" data-view-block="col-xs-12" data-view-hidden="col-xs-12" data-initializer="mailing-content-view" data-dyn-name-id="${mailingContentForm.dynNameID}">

                    <agn:agnForm action="/mailingcontent" id="mailingContentForm" data-form="resource" data-disable-controls="save">
                        <html:hidden property="action" value="${ACTION_SAVE_TEXTBLOCK}"/>
                        <html:hidden property="dynNameID"/>
                        <html:hidden property="mailingID"/>
                        <html:hidden property="contentID"/>

                        <div class="tile">
                            <div class="tile-header">
                                <h2 class="headline">${mailingContentForm.dynName}</h2>
                            </div>

                            <div class="tile-content">
                                <div class="tile-content-forms">
                                    <c:if test="${not isMailingGrid and mailingContentForm.enableTextGeneration and not mailingContentForm.worldMailingSend}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label">
                                                    <bean:message key="mailing.option"/>
                                                </label>
                                            </div>

                                            <div class="col-sm-8">
                                                <button type="button" tabindex="-1" class="btn btn-regular" data-form-action="${ACTION_GENERATE_TEXT_FROM_HTML}">
                                                    <i class="icon icon-exchange"></i>
                                                    <span class="text"><bean:message key="mailing.GenerateText"/></span>
                                                </button>
                                            </div>
                                        </div>

                                        <div class="tile-separator"></div>
                                    </c:if>

                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label class="control-label" for="dynInterestGroup">
                                                <bean:message key="interestgroup"/>
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <html:select property="dynInterestGroup" styleId="dynInterestGroup" styleClass="form-control js-select"
                                                         disabled="${not isEditable}">
                                                <html:option value=""><bean:message key="nointerestgroup"/></html:option>
                                                <c:forEach var="interestField" items="${mailingContentForm.availableInterestGroups}">
                                                    <html:option value="${interestField.column}">${interestField.shortname}</html:option>
                                                </c:forEach>
                                            </html:select>
                                        </div>
                                    </div>
                                    
                                    <%@ include file="mailing-content-view-disableLinkExtension.jspf" %>
									
                                    <div class="tile-separator"></div>

                                    <c:set var="contentHTMLCodeTitle" value="mailingContentText" scope="request"/>
                                    <c:set var="modalEditor" value="modal-editor-text" scope="request"/>
                                    <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                        <c:set var="contentHTMLCodeTitle" value="HTML" scope="request"/>
                                        <c:set var="modalEditor" value="modal-editor" scope="request"/>
                                    </logic:equal>

                                    <c:forEach var="dyncontent" items="${mailingContentForm.content}" varStatus="contentLoop">
                                        <input type="hidden" name="dynContentId(${dyncontent.key})" value="${dyncontent.value.id}"/>
                                        <input type="hidden" name="dynNameId(${dyncontent.key})" value="${dyncontent.value.dynNameID}"/>

                                        <div class="inline-tile" id="content${dyncontent.value.id}">
                                            <div class="inline-tile-header">
                                                <c:choose>
                                                    <c:when test="${dyncontent.value.targetID eq 0}">
                                                        <c:set var="currentTargetName" scope="request"><bean:message key="statistic.all_subscribers"/></c:set>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="target" items="${mailingContentForm.availableTargetGroups}">
                                                            <c:if test="${target.id == dyncontent.value.targetID}">
                                                                <c:set var="currentTargetName" value="${target.targetName}" scope="request"/>
                                                            </c:if>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>

                                                <h2 class="headline-static">
                                                        ${currentTargetName}
                                                </h2>

                                                <c:set var="targetGroupDeleted" value="0" scope="page"/>
                                                <agn:agnSelect property='targetID(${dyncontent.key})' disabled="${not isEditable}" class="form-control js-select">
                                                    <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                                                    <c:forEach var="target" items="${mailingContentForm.availableTargetGroups}">
                                                        <c:if test="${(target.deleted == 0) || (target.id == dyncontent.value.targetID)}">
                                                            <html:option value="${target.id}">${target.targetName} (${target.id})
                                                                <c:if test="${target.deleted != 0}">
                                                                    &nbsp;(<bean:message key="target.Deleted"/>)
                                                                    <c:set var="targetGroupDeleted" value="1" scope="page"/>
                                                                </c:if>
                                                            </html:option>
                                                        </c:if>
                                                    </c:forEach>
                                                </agn:agnSelect>

                                                <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                    <ul class="inline-tile-header-nav">
                                                        <li class="active">
                                                            <a href="#" data-toggle-tab="#tab-mailingContentViewCode${dyncontent.value.id}" data-tab-group="mailingContentViewCode">
                                                                <bean:message key="${contentHTMLCodeTitle}"/>
                                                            </a>
                                                        </li>
                                                        <li>
                                                            <a href="#" data-toggle-tab="#tab-mailingContentViewHTML${dyncontent.value.id}" data-tab-group="mailingContentHtml">
                                                                <bean:message key="mailingContentHTMLEditor"/>
                                                            </a>
                                                        </li>
                                                    </ul>
                                                </logic:equal>

                                                <ul class="inline-tile-header-actions">
                                                    <c:if test="${isEditable}">
                                                        <li>
                                                            <c:set var="MessageToDelete"><bean:message key="button.Delete" /></c:set>
                                                            <a href="#" data-form-set="contentID: ${dyncontent.value.id}" data-form-action="${ACTION_DELETE_TEXTBLOCK}" data-tooltip="${MessageToDelete}" data-controls-group="save">
                                                                <i class="icon icon-trash-o"></i>
                                                            </a>
                                                        </li>
                                                    </c:if>

                                                    <c:if test="${fn:length(mailingContentForm.content) > 1}">
                                                        <c:if test="${not contentLoop.first}">
                                                            <li>
                                                                <c:set var="MessageToTop"><bean:message key="mailing.content.toTop" /></c:set>
                                                                <a href="#" data-form-set="contentID: ${dyncontent.value.id}" data-form-action="${ACTION_CHANGE_ORDER_TOP}" data-tooltip="${MessageToTop}">
                                                                    <i class="icon icon-angle-double-up"></i>
                                                                </a>
                                                            </li>
                                                            <li>
                                                                <c:set var="MessageMoveUp"><bean:message key="mailing.content.moveUp" /></c:set>
                                                                <a href="#" data-form-set="contentID: ${dyncontent.value.id}" data-form-action="${ACTION_CHANGE_ORDER_UP}" data-tooltip="${MessageMoveUp}">
                                                                    <i class="icon icon-angle-up"></i>
                                                                </a>
                                                            </li>
                                                        </c:if>

                                                        <c:if test="${not contentLoop.last}">
                                                            <li>
                                                                <c:set var="MessageToBottom"><bean:message key="mailing.content.toBottom" /></c:set>
                                                                <a href="#" data-form-set="contentID: ${dyncontent.value.id}" data-form-action="${ACTION_CHANGE_ORDER_BOTTOM}" data-tooltip="${MessageToBottom}">
                                                                    <i class="icon icon-angle-double-down"></i>
                                                                </a>
                                                            </li>
                                                            <li>
                                                                <c:set var="MessageMoveDown"><bean:message key="mailing.content.moveDown" /></c:set>
                                                                <a href="#" data-form-set="contentID: ${dyncontent.value.id}" data-form-action="${ACTION_CHANGE_ORDER_DOWN}" data-tooltip="${MessageMoveDown}">
                                                                    <i class="icon icon-angle-down"></i>
                                                                </a>
                                                            </li>
                                                        </c:if>
                                                    </c:if>

                                                    <li>
                                                        <c:set var="quote" value="'" />
                                                        <c:set var="quoteReplace" value="\\'" />
                                                        <c:set var="currentTargetNameEscaped" scope="request"><c:out value="${fn:replace(currentTargetName, quote, quoteReplace)}"/></c:set>
                                                        <a href="#" data-modal="${modalEditor}" data-modal-set="title: '${currentTargetNameEscaped}', target: 'content_${dyncontent.key}_dynContent', id: ${dyncontent.value.id}" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                                            <i class="icon icon-arrows-alt"></i>
                                                        </a>
                                                    </li>
                                                </ul>
                                            </div>
                                            <div class="inline-tile-content">
                                                <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                    <div id="tab-mailingContentViewCode${dyncontent.value.id}" >
                                                        <div class="row">
                                                            <div class="col-sm-12">
                                                                <div id="content_${dyncontent.key}_dynContentEditor" class="form-control"></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </logic:equal>

                                                <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                <div id="tab-mailingContentViewHTML${dyncontent.value.id}" class="hidden" data-full-tags="${isHTMLVersion}">
                                                    </logic:equal>
                                                    <div class="row">
                                                        <div class="col-sm-12">
                                                            <html:hidden property='dynOrder(${dyncontent.key})'/>
                                                            <agn:agnTextarea property='dynContent(${dyncontent.key})'
                                                                             styleId="content_${dyncontent.key}_dynContent" rows="14" cols="20"
                                                                             styleClass="form-control js-editor js-wysiwyg"
                                                                             readonly="${not isEditable}" data-autosave="${dyncontent.value.id}"
                                                                             data-browse-mailing-id="${mailingContentForm.mailingID}"/>
                                                        </div>
                                                    </div>
                                                    <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                </div>
                                                </logic:equal>

                                                <c:set var="dyncontent" value="${dyncontent}" scope="request"/>
                                                <tiles:insert attribute="mailing-content-view-additional-properties" flush="false"/>


                                            </div>
                                        </div>

                                        <div class="tile-separator"></div>
                                    </c:forEach>

                                    <div class="inline-tile" id="content0">
                                        <div class="inline-tile-header">
                                            <h2 class="headline-static">
                                                <bean:message key="New_Content" />
                                            </h2>

                                            <agn:agnSelect property="newTargetID" disabled="${not isEditable}" class="js-select form-control">
                                                <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                                                <c:forEach var="target" items="${mailingContentForm.availableTargetGroups}">
                                                    <c:if test="${target.deleted == 0}">
                                                        <html:option value="${target.id}">${target.targetName} (${target.id})</html:option>
                                                    </c:if>
                                                </c:forEach>
                                            </agn:agnSelect>

                                            <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                <ul class="inline-tile-header-nav">
                                                    <li class="active">
                                                        <a href="#" data-toggle-tab="#tab-mailingContentViewCode0" data-tab-group="mailingContentViewCode">
                                                            <bean:message key="${contentHTMLCodeTitle}"/>
                                                        </a>
                                                    </li>
                                                    <li>
                                                        <a href="#" data-toggle-tab="#tab-mailingContentViewHTML0" data-tab-group="mailingContentHtml">
                                                            <bean:message key="mailingContentHTMLEditor"/>
                                                        </a>
                                                    </li>
                                                </ul>
                                            </logic:equal>
                                            <ul class="inline-tile-header-actions">
                                                <li>
                                                    <a href="#" data-modal="${modalEditor}" data-modal-set="title: '${newTargetName}', target: 'newContentText', id: 0" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                                        <i class="icon icon-arrows-alt"></i>
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>
                                        <div class="inline-tile-content">
                                            <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                                <div id="tab-mailingContentViewCode0">
                                                    <div class="row">
                                                        <div class="col-sm-12">
                                                            <div id="newContentTextEditor" class="form-control"></div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </logic:equal>

                                            <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                            <div id="tab-mailingContentViewHTML0" class="hidden" data-full-tags="${isHTMLVersion}">
                                                </logic:equal>
                                                <div class="row">
                                                    <div class="col-sm-12">
                                                        <agn:agnTextarea property="newContent" styleId="newContentText" rows="14" cols="20"
                                                                         styleClass="form-control js-editor js-wysiwyg" readonly="${not isEditable}"
                                                                         data-browse-mailing-id="${mailingContentForm.mailingID}"
                                                                         data-autosave="new"/>
                                                    </div>
                                                </div>
                                                <logic:equal name="mailingContentForm" property="showHTMLEditor" value="true">
                                            </div>
                                            </logic:equal>
                                        </div>

                                        <div class="inline-tile-footer">
                                            <div class="btn-group">
                                                <html:link page="/mailingcontent.do?action=${ACTION_VIEW_CONTENT}&mailingID=${mailingContentForm.mailingID}" styleClass="btn btn-regular pull-left">
                                                    <i class="icon icon-angle-left"></i>
                                                    <span class="text"> <bean:message key="button.Back"/></span>
                                                </html:link>

                                                <c:if test="${isEditable}">
                                                	<%@include file="mailing-content-view-form-readonly.jspf" %>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>

                                </div>

                            </div>
                            <!-- Tile Content END -->

                        </div>
                        <!-- Tile END -->

                    </agn:agnForm>

                </div>
                <!-- Col END -->

                <c:url var="previewLink" value="/mailingsend.do">
                    <c:param name="action" value="${ACTION_PREVIEW_SELECT}"/>
                    <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
                    <c:param name="previewSelectPure" value="true"/>
                    <c:param name="previewFormat" value="${mailingContentForm.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
                </c:url>

                <div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                    <div data-load="${previewLink}" data-load-target="#preview"></div>
                </div>
            </div>
        <c:if test="${isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>

    <tiles:putList name="footerItems">
        <tiles:add>
            <html:link styleClass="btn btn-large btn-regular" page="/mailingcontent.do?action=${ACTION_VIEW_CONTENT}&mailingID=${mailingContentForm.mailingID}">
                <span class="text"><bean:message key="button.Back"/></span>
            </html:link>
        </tiles:add>

        <c:if test="${(not isMailingGrid) or mailingContentForm.mailingEditable}">
            <tiles:add>
                <div class="pull-right">
                    <button type="button" class="btn btn-large btn-regular" data-form-target='#mailingContentForm' data-form-set='action:${ACTION_SAVE_TEXTBLOCK_AND_BACK}' data-form-submit="" data-controls-group="save">
                        <span class="text"><bean:message key="button.SaveAndBack"/></span>
                    </button>

                    <button type="button" class="btn btn-large btn-primary" data-form-target='#mailingContentForm' data-form-set='action:${ACTION_SAVE_TEXTBLOCK}' data-form-submit="" data-controls-group="save">
                        <span class="text"><bean:message key="button.Save"/></span>
                    </button>
                </div>
            </tiles:add>
        </c:if>
    </tiles:putList>
</tiles:insert>


<script id="modal-editor-text" type="text/x-mustache-template">
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                </div>
                <div class="modal-body">
                    <textarea id="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor-text" data-form-target="#mailingContentForm"></textarea>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <c:if test="${isEditable}">
							<%@include file="mailing-content-view-js1-readonly.jspf" %>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>


<script id="modal-editor" type="text/x-mustache-template">
    {{ showHTMLEditor = $('#tab-mailingContentViewHTML' + id).is(':visible') }}
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                    <ul class="modal-header-nav">
                        <li class="active">
                            <a href="#" data-toggle-tab="#tab-mailingContentViewCodeModal" data-tab-group="mailingContentViewCode">
                                <bean:message key="${contentHTMLCodeTitle}"/>
                            </a>
                        </li>
                        <li>
                            <a href="#" data-toggle-tab="#tab-mailingContentViewHTMLModal" data-tab-group="mailingContentHtml">
                                <bean:message key="mailingContentHTMLEditor"/>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="modal-body">
                    <div id="tab-mailingContentViewCodeModal" {{ (showHTMLEditor) ? print('data-tab-hide') : print('data-tab-show') }}>
                        <div id="modalTextAreaEditor" class="form-control"></div>
                    </div>

                    <div id="tab-mailingContentViewHTMLModal" {{ (showHTMLEditor) ? print('data-tab-show') : print('data-tab-hide') }} data-full-tags="${isHTMLVersion}">
                        <textarea id="modalTextArea" name="modalTextArea" data-sync="\#{{= target }}"
                                  class="form-control js-editor js-wysiwyg"
                                  data-form-target="#mailingContentForm"
                                  data-browse-mailing-id="${mailingID}"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <c:if test="${isEditable}">
							<%@include file="mailing-content-view-js2-readonly.jspf" %>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
