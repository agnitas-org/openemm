<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>

<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="userFormEditForm" type="com.agnitas.web.forms.ComUserFormEditForm"--%>
<%--@elvariable id="emm_actions" type="java.util.List"--%>

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>

<agn:agnForm action="/userform"
             styleId="userFormEditForm"
             data-form="resource"
             data-autosave-scope="action-form/${userFormEditForm.formID}">
    <html:hidden property="formID"/>
    <html:hidden property="action"/>
    <html:hidden property="workflowId"/>
    <html:hidden property="forwardParams"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.form.edit"/></h2>
                <ul class="tile-header-actions">
                    <li>
                        <label class="btn btn-regular btn-ghost toggle">
                            <html:hidden property="__STRUTS_CHECKBOX_isActive" value="false"/>
                            <span class="text">
                                    <bean:message key="default.status.active"/>
                                </span>
                            <html:checkbox property="isActive"/>
                            <div class="toggle-control"></div>
                        </label>
                    </li>
                </ul>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_name" class="control-label">
                            <bean:message key="Name"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text styleClass="form-control" styleId="form_name" property="formName" maxlength="50" size="42"/>
                    </div>
                </div>

                <c:if test="${userFormEditForm.formID ne 0}">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="form_url" class="control-label">
                                <bean:message key="default.url"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <html:text styleClass="form-control" styleId="form_url" property="formUrl" maxlength="99" size="42" readonly="true"/>
                        </div>
                    </div>
                </c:if>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_description" class="control-label">
                            <bean:message key="default.description"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:textarea styleClass="form-control" styleId="form_description" property="description" rows="5" cols="32"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="settings.form.success"/>
            </h2>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_startAction" class="control-label">
                            <bean:message key="form.action.success"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <html:select property="startActionID" styleId="form_startAction" styleClass="form-control js-select" size="1">
                                    <html:option value="0">
                                        <bean:message key="settings.No_Action"/>
                                    </html:option>
                                    <c:forEach var="emm_action" items="${emm_actions}">
                                        <html:option value="${emm_action.id}">
                                            ${emm_action.shortname}
                                        </html:option>
                                    </c:forEach>
                                </html:select>
                            </div>

                            <div class="input-group-btn">
                                <emm:ShowByPermission token="actions.change">
                                    <logic:notEqual name="userFormEditForm" property="startActionID" value="0">
                                        <c:set var="actionSwitchTooltip">
                                            <bean:message key="form.action.switch"/>
                                        </c:set>
                                        <agn:agnLink page="/action.do?action=2&actionID=${userFormEditForm.startActionID}" class="btn btn-info btn-regular" data-tooltip="${actionSwitchTooltip}">
                                            <i class="icon icon-pencil"></i>
                                        </agn:agnLink>
                                    </logic:notEqual>
                                </emm:ShowByPermission>
                            </div>

                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_successTemplate" class="radio-inline control-label">
                            <html:radio styleId="form_successTemplate" property="successUseUrl" value="false"/>
                            <bean:message key="settings.form.success"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="inline-tile">
                            <div class="inline-tile-header">
                                <c:if test="${not userFormEditForm.successUseVelocity}">
                                    <ul class="inline-tile-header-nav">
                                        <li class="active">
                                            <a href="#" data-toggle-tab="#tab-successTemplateCodeEditor" onclick="removeAttrFromWYSIWYG('successTab')">
                                                <bean:message key="HTML"/>
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" id="successTab" data-clicked="false" onclick="checkVelocityScripts('successTemplate', 'successTab')">
                                                <bean:message key="mailingContentHTMLEditor"/>
                                            </a>
                                        </li>
                                    </ul>
                                </c:if>
                                <ul class="inline-tile-header-actions">
                                    <li>
                                        <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="settings.form.success"/>, target: successTemplate, id: htmlTemplateLarge, isUseVelocity: '${userFormEditForm.successUseVelocity}'" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                           <i class="icon icon-arrows-alt"></i>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <div class="inline-tile-content">
                                <!-- HTML CODE EDITOR -->
                                <div id="tab-successTemplateCodeEditor">
                                    <div class="row">
                                        <div id="successTemplateEditor" class="form-control"></div>
                                    </div>
                                </div>

                                <!-- WYSIWYG EDITOR -->
                                <div id="tab-successTemplateHtmlEditor" class="hidden" data-full-tags="true">
                                    <div class="row">
                                        <agn:agnTextarea styleClass="form-control js-editor js-wysiwyg" property="successTemplate" styleId="successTemplate" rows="14" cols="75" data-autosave="successForm"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_successUseUrl" class="radio-inline control-label">
                            <html:radio styleId="form_successUseUrl" property="successUseUrl" value="true"/>
                            <bean:message key="form.success_url"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text property="successUrl" styleClass="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_endAction" class="control-label">
                            <bean:message key="form.action.error"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <html:select property="endActionID" styleId="form_endAction" styleClass="form-control  js-select" size="1">
                                    <html:option value="0">
                                        <bean:message key="settings.No_Action"/>
                                    </html:option>
                                    <c:forEach var="emm_action" items="${emm_actions}">
                                        <html:option value="${emm_action.id}">
                                            ${emm_action.shortname}
                                        </html:option>
                                    </c:forEach>
                                </html:select>
                            </div>

                            <div class="input-group-btn">
                                <emm:ShowByPermission token="actions.change">
                                    <logic:notEqual name="userFormEditForm" property="endActionID" value="0">
                                        <c:set var="actionSwitchTooltip">
                                            <bean:message key="form.action.switch"/>
                                        </c:set>
                                        <agn:agnLink page="/action.do?action=2&actionID=${userFormEditForm.endActionID}" class="btn btn-info btn-regular" data-tooltip="${actionSwitchTooltip}">
                                            <i class="icon icon-pencil"></i>
                                        </agn:agnLink>
                                    </logic:notEqual>
                                </emm:ShowByPermission>
                            </div>

                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="settings.form.error"/>
            </h2>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_errorTemplate" class="radio-inline control-label">
                            <html:radio styleId="form_errorTemplate" property="errorUseUrl" value="false"/>
                            <bean:message key="settings.form.error"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="inline-tile">
                            <div class="inline-tile-header">
                                <c:if test="${not userFormEditForm.errorUseVelocity}">
                                    <ul class="inline-tile-header-nav">
                                        <li class="active">
                                            <a href="#" data-toggle-tab="#tab-errorTemplateCodeEditor" onclick="removeAttrFromWYSIWYG('errorTab')">
                                                <bean:message key="HTML"/>
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#" id="errorTab" data-clicked="false" onclick="checkVelocityScripts('errorTemplate', 'errorTab')">
                                                <bean:message key="mailingContentHTMLEditor"/>
                                            </a>
                                        </li>
                                    </ul>
                                </c:if>
                                <ul class="inline-tile-header-actions">
                                    <li>
                                        <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="settings.form.error"/>, target: errorTemplate, id: htmlTemplateLarge, isUseVelocity: '${userFormEditForm.errorUseVelocity}'" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                           <i class="icon icon-arrows-alt"></i>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <div class="inline-tile-content">
                                <!-- HTML CODE EDITOR -->
                                <div id="tab-errorTemplateCodeEditor">
                                    <div class="row">
                                        <div id="errorTemplateEditor" class="form-control"></div>
                                    </div>
                                </div>

                                <!-- WYSIWYG EDITOR -->
                                <div id="tab-errorTemplateHtmlEditor" class="hidden" data-full-tags="true">
                                    <div class="row">
                                        <agn:agnTextarea styleClass="form-control js-editor js-wysiwyg" property="errorTemplate" styleId="errorTemplate" rows="14" cols="75" data-autosave="errorForm"/>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="form_errorUseUrl" class="radio-inline control-label">
                            <html:radio styleId="form_errorUseUrl" property="errorUseUrl" value="true"/>
                            <bean:message key="form.error_url"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text property="errorUrl" styleClass="form-control"/>
                    </div>
                </div>
            </div>
        </div>

    </div>

</agn:agnForm>

<script id="modal-editor" type="text/x-mustache-template">
    {{ showHtmlEditor = (target == 'errorTemplate') ? $('#tab-errorTemplateHtmlEditor').is(':visible') : $('#tab-successTemplateHtmlEditor').is(':visible') }}
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                    <ul class="modal-header-nav" {{(isUseVelocity == 'true') ? print('hidden') : print('') }}>
                        <li class="active">
                            <a href="#" data-toggle-tab="#tab-modalTextAreaCodeEditor" onclick="removeAttrFromWYSIWYG('modalTab')">
                                <bean:message key="HTML"/>
                            </a>
                        </li>
                        <li>
                            <a href="#" id="modalTab" data-clicked="false" onclick="checkVelocityScripts('modalTextArea', 'modalTab')">
                                <bean:message key="mailingContentHTMLEditor"/>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="modal-body">
                    <div id="tab-modalTextAreaCodeEditor" {{ (showHtmlEditor) ? print('data-tab-hide') : print('data-tab-show') }}>
                        <div id="modalTextAreaEditor" class="form-control"></div>
                    </div>
                    <div id="tab-modalTextAreaHtmlEditor" data-full-tags="true" {{ (showHtmlEditor) ? print('data-tab-show') : print('data-tab-hide') }}>
                        <textarea id="modalTextArea" name="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor js-wysiwyg hidden" data-form-target="#userFormEditForm"></textarea>
                    </div>

                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <emm:ShowByPermission token="forms.change">
                            <c:set var="submitType" value="data-form-submit"/>
                            <c:if test="${workflowForwardParams != null && workflowForwardParams != ''}">
                                <c:set var="submitType" value="data-form-submit-static"/>
                            </c:if>
                            <button type="button" class="btn btn-primary btn-large" data-sync-from="#modalTextArea" data-sync-to="\#{{= target }}" data-dismiss="modal" data-form-target="#userFormEditForm" data-form-set="save:save" ${submitType}>
                                <i class="icon icon-save"></i>
                                <span class="text"><bean:message key="button.Save"/></span>
                            </button>
                        </emm:ShowByPermission>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
<script type="application/javascript">
    function checkVelocityScripts(textAreaId, tabId) {
        var text,
            $link = $('#' + tabId);
        // First if-else is used to avoid infinity 'click' event triggering;
        if (!$link.data('clicked')) {
            text = AGN.Lib.Editor.get($('#' + textAreaId)).val();
            if (text.match(/#(?:set|include|macro|parse|if|foreach)/gi)) {
                AGN.Lib.Messages('<bean:message key="Error"/>', '<bean:message key="userform.velocityNotAllowed"/>', 'alert');
            } else {
                $link.data('clicked', true);
                //Adds attribute to activate switching of editors.
                $link.attr('data-toggle-tab', '#tab-'+textAreaId+'HtmlEditor').trigger('click');
            }
        } else {
            $link.data('clicked', false);
        }
    }

    function removeAttrFromWYSIWYG(tabId) {
        //set timeout '0' is used to remove attribute in next js time measure right after editors were switched
        setTimeout(function() {
            $('#' + tabId).removeAttr('data-toggle-tab');
        }, 0);
    }
</script>
