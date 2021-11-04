<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>

<%--@elvariable id="form" type="com.agnitas.emm.core.userform.form.UserFormForm"--%>
<%--@elvariable id="userFormURLPattern" type="java.lang.String"--%>
<%--@elvariable id="userFormFullURLPattern" type="java.lang.String"--%>
<%--@elvariable id="emmActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="workflowParameters" type="org.agnitas.web.forms.WorkflowParameters"--%>
<%--@elvariable id="companyId" type="java.lang.Integer"--%>

<c:url var="actionEditUrlPattern" value="/action/{action-ID}/view.action"/>

<mvc:form servletRelativeAction="/webform/save.action" id="userFormForm" modelAttribute="form" data-form="resource"
          data-autosave-scope="action-form/${form.formId}"
          data-controller="userform-view" data-initializer="userform-view" data-validator="userform-edit/form">

    <script id="config:userform-view" type="application/json">
        {
            "actionURLPattern": "${actionEditUrlPattern}"
        }
    </script>

    <emm:ShowByPermission token="forms.creator">
        <script id="config:formbuilderCommon" type="application/json">
            {
                "companyId": ${companyId},
                "confirmationModal": "warning-html-generation-modal",
                "namesJson": ${emm:toJson(names)},
                "mediapoolImages": ${emm:toJson(mediapoolImages)},
                "textProfileFields": ${emm:toJson(textProfileFields)},
                "dateProfileFields": ${emm:toJson(dateProfileFields)},
                "numberProfileFields": ${emm:toJson(numberProfileFields)},
                "cssUrl": "${formCssLocation}/css/bootstrap.min.css"
            }
        </script>
    </emm:ShowByPermission>

    <mvc:hidden path="formId"/>
    <emm:workflowParameters />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.form.edit"/></h2>
                <ul class="tile-header-actions">
                    <li>
                        <label class="btn btn-regular btn-ghost toggle">
                            <span class="text">
                                <mvc:message code="default.status.active"/>
                            </span>
                            <mvc:checkbox path="active"/>
                            <div class="toggle-control"></div>
                        </label>
                    </li>
                </ul>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="formName" class="control-label">
                            <mvc:message var="nameMsg" code="default.Name"/>
                            ${nameMsg}
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="formName" maxlength="50" size="42" cssClass="form-control" id="formName" placeholder="${nameMsg}"/>
                    </div>
                </div>

                <c:if test="${form.formId ne 0}">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="formURL" class="control-label">
                                <mvc:message code="default.url"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <input type="text" id="formURL"
                                           class="form-control" maxlength="99" size="42" readonly="readonly"
                                           value="${userFormURLPattern}">
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="formURL" class="control-label">
                                <mvc:message code="userform.test"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                	<select id="formTestRecipient" class="form-control js-select" data-action="formTestRecipient">
                                		<option value="${userFormFullURLPatternNoUid}"><mvc:message code="userform.test.withoutRecipient"/></option>
                                		<c:forEach items="${userFormFullURLPatterns}" var="testURL">
                                			<option value="${testURL.userFormUrl}">${testURL.firstname} ${testURL.lastname} (${testURL.email})</option>
                                		</c:forEach>
                                	</select>
                                </div>
                                <div class="input-group-btn">
                                  <a href="${userFormFullURLPatternNoUid}"
                                  	 id="formTestLink"
                                     data-tooltip="<mvc:message code='userform.test'/>"
                                     class="btn btn-regular btn-primary" target="_blank">
                                      <i class="icon icon-arrow-right"></i>
                                  </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="description" class="control-label">
                            <mvc:message var="descriptionMsg" code="default.description"/>
                            ${descriptionMsg}
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="description" rows="5" cols="32" cssClass="form-control" id="description" placeholder="${descriptionMsg}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

<%--    Success settings--%>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="settings.form.success"/>
            </h2>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="startAction" class="control-label">
                            <mvc:message code="form.action.success"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:select path="successSettings.startActionId" size="1" cssClass="form-control js-select" id="startAction"
                                    data-action="change-intro-action">
                                    <mvc:option value="0"><mvc:message code="settings.No_Action"/></mvc:option>
                                    <mvc:options items="${emmActions}" itemValue="id" itemLabel="shortname"/>
                                </mvc:select>
                            </div>

                            <div class="input-group-btn">
                                <emm:ShowByPermission token="actions.show">
                                    <mvc:message var="actionSwitchTooltip" code="form.action.switch"/>
                                    <a id="startActionLink" class="btn btn-info btn-regular ${form.successSettings.startActionId > 0 ? '' : 'hidden'}" data-tooltip="${actionSwitchTooltip}"
                                       href="${fn:replace(actionEditUrlPattern, '{action-ID}', form.successSettings.startActionId)}">
                                        <i class="icon icon-pencil"></i>
                                    </a>
                                </emm:ShowByPermission>
                            </div>

                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="successUseTemplate" class="radio-inline control-label">
                            <mvc:radiobutton path="successSettings.useUrl" value="false" id="successUseTemplate"/>
                            <mvc:message code="settings.form.success"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="inline-tile">
                            <div class="inline-tile-header">
                                <ul class="inline-tile-header-nav">
                                    <li class="active">
                                        <a href="#" data-toggle-tab="#tab-success-template-html">
                                            <mvc:message code="HTML"/>
                                        </a>
                                    </li>
                                    <c:if test="${not form.successSettings.useVelocity}">
                                        <li>
                                            <a href="#" data-toggle-tab="#tab-success-template-wysiwyg"
                                               data-action="check-velocity-script"
                                               data-action-options="type: success">
                                                <mvc:message code="mailingContentHTMLEditor"/>
                                            </a>
                                        </li>
                                    </c:if>
                                    <emm:ShowByPermission token="forms.creator">
                                        <li>
                                            <a href="#" data-toggle-tab="#tab-success-template-form-builder">
                                                <mvc:message code="userform.builder"/>
                                            </a>
                                        </li>
                                    </emm:ShowByPermission>
                                </ul>
                                <ul class="inline-tile-header-actions">
                                    <li>
                                        <a href="#" data-modal="modal-editor"
                                           data-modal-set="title: <mvc:message code="settings.form.success"/>,
                                                target: '#successTemplate', type: success,
                                                useVelocity: '${form.successSettings.useVelocity}',
                                                targetFormBuilder: '#successFormBuilder'"
                                           data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                                           <i class="icon icon-arrows-alt"></i>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <div class="inline-tile-content">
                                <!-- HTML CODE EDITOR -->
                                <div id="tab-success-template-html">
                                    <div class="row">
                                        <div id="successTemplateEditor" class="form-control"></div>
                                    </div>
                                </div>

                                <!-- WYSIWYG EDITOR -->
                                <div id="tab-success-template-wysiwyg" class="hidden">
                                    <div class="row">
                                        <mvc:textarea path="successSettings.template" cssClass="form-control js-editor js-wysiwyg"
                                                      rows="14" cols="75" data-autosave="successForm"
                                                      data-editor-options="allowExternalScript: true, isFullHtml: true"
                                                      id="successTemplate"/>
                                    </div>
                                </div>

                                <emm:ShowByPermission token="forms.creator">
                                    <!-- FORM BUILDER -->
                                    <div id="tab-success-template-form-builder" class="hidden" data-initializer="formbuilder">
                                        <script id="config:formbuilder" type="application/json">
                                            {
                                                "formName": "${empty form.formName ? "new form" : form.formName}${'(success)'}",
                                                "data": ${empty form.successSettings.formBuilderJson ? "\"\"" : form.successSettings.formBuilderJson}
                                            }
                                        </script>
                                        <div class="row">
                                            <div id="successFormBuilder" class="js-form-builder" data-target="#successTemplate"></div>
                                        </div>
                                    </div>
                                </emm:ShowByPermission>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="successUseURL" class="radio-inline control-label">
                            <mvc:radiobutton path="successSettings.useUrl" value="true" id="successUseURL"/>
                            <mvc:message code="form.success_url"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="successSettings.url" cssClass="form-control" id="successURL"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="finalAction" class="control-label">
                            <mvc:message code="form.action.error"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:select path="successSettings.finalActionId" size="1" cssClass="form-control js-select" id="finalAction"
                                    data-action="change-final-action">
                                    <mvc:option value="0"><mvc:message code="settings.No_Action"/></mvc:option>
                                    <mvc:options items="${emmActions}" itemValue="id" itemLabel="shortname"/>
                                </mvc:select>
                            </div>

                            <div class="input-group-btn">
                                <emm:ShowByPermission token="actions.show">
                                    <mvc:message var="actionSwitchTooltip" code="form.action.switch"/>
                                    <a id="finalActionLink" class="btn btn-info btn-regular ${form.successSettings.finalActionId > 0 ? '' : 'hidden'}" data-tooltip="${actionSwitchTooltip}"
                                       href="${fn:replace(actionEditUrlPattern, '{action-ID}', form.successSettings.finalActionId)}">
                                        <i class="icon icon-pencil"></i>
                                    </a>
                                </emm:ShowByPermission>
                            </div>

                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

<%--    Error settings--%>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="settings.form.error"/>
            </h2>
        </div>

        <div class="tile-content">
            <div class="tile-content-forms">

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="errorUseTemplate" class="radio-inline control-label">
                            <mvc:radiobutton path="errorSettings.useUrl" value="false" id="errorUseTemplate"/>
                            <mvc:message code="settings.form.error"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="inline-tile">
                            <div class="inline-tile-header">
                                <ul class="inline-tile-header-nav">
                                    <li class="active">
                                        <a href="#" data-toggle-tab="#tab-error-template-html">
                                            <mvc:message code="HTML"/>
                                        </a>
                                    </li>
                                    <c:if test="${not form.errorSettings.useVelocity}">
                                        <li>
                                            <a href="#" data-toggle-tab="#tab-error-template-wysiwyg"
                                               data-action="check-velocity-script"
                                               data-action-options="type: error">
                                                <mvc:message code="mailingContentHTMLEditor"/>
                                            </a>
                                        </li>
                                    </c:if>
                                    <emm:ShowByPermission token="forms.creator">
                                        <li>
                                            <a href="#" data-toggle-tab="#tab-error-template-form-builder">
                                                <mvc:message code="userform.builder"/>
                                            </a>
                                        </li>
                                    </emm:ShowByPermission>
                                </ul>
                                <ul class="inline-tile-header-actions">
                                    <li>
                                        <a href="#" data-modal="modal-editor"
                                           data-modal-set="title: <mvc:message code="settings.form.error"/>,
                                           target: '#errorTemplate', type: error,
                                           useVelocity: '${form.errorSettings.useVelocity}',
                                           targetFormBuilder: '#errorFormBuilder'"
                                           data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                                           <i class="icon icon-arrows-alt"></i>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <div class="inline-tile-content">
                                <!-- HTML CODE EDITOR -->
                                <div id="tab-error-template-html">
                                    <div class="row">
                                        <div id="errorTemplateEditor" class="form-control"></div>
                                    </div>
                                </div>

                                <!-- WYSIWYG EDITOR -->
                                <div id="tab-error-template-wysiwyg" class="hidden">
                                    <div class="row">
                                        <mvc:textarea path="errorSettings.template" cssClass="form-control js-editor js-wysiwyg"
                                                      data-editor-options="allowExternalScript: true, isFullHtml: true"
                                                      rows="14" cols="75" data-autosave="errorForm"
                                                      id="errorTemplate"/>
                                    </div>
                                </div>

                                <emm:ShowByPermission token="forms.creator">
                                    <!-- FORM BUILDER -->
                                    <div id="tab-error-template-form-builder" class="hidden" data-initializer="formbuilder">
                                        <script id="config:formbuilder" type="application/json">
                                            {
                                                "formName": "${empty form.formName ? "new form" : form.formName}${'(error)'}",
                                                "data": ${empty form.errorSettings.formBuilderJson ? "\"\"" : form.errorSettings.formBuilderJson}
                                            }
                                        </script>
                                        <div class="row">
                                            <div id="errorFormBuilder" class="js-form-builder" data-target="#errorTemplate"></div>
                                        </div>
                                    </div>
                                </emm:ShowByPermission>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="errorUseURL" class="radio-inline control-label">
                            <mvc:radiobutton path="errorSettings.useUrl" value="true" id="errorUseURL"/>
                            <mvc:message code="form.error_url"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="errorSettings.url" cssClass="form-control" id="errorURL"/>
                    </div>
                </div>
            </div>
        </div>

    </div>
</mvc:form>

<script id="modal-editor" type="text/x-mustache-template">
    {{ showHtmlEditor = $('#tab-' + type + '-template-html').is(':visible') }}
    {{ showWysisygEditor = $('#tab-' + type + '-template-wysiwyg').is(':visible') }}
    {{ showFormBuilder = $('#tab-' + type + '-template-form-builder').is(':visible') }}
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                    <ul class="modal-header-nav" {{(useVelocity == 'true') ? print('hidden') : print('') }}>
                        <li class="active">
                            <a href="#" data-toggle-tab="#tab-modal-template-html">
                                <mvc:message code="HTML"/>
                            </a>
                        </li>
                        <li>
                            <a href="#" id="modalTab" data-toggle-tab="#tab-modal-template-wysiwyg"
                               data-action="check-velocity-script" data-action-options="type: {{- type}}">
                                <mvc:message code="mailingContentHTMLEditor"/>
                            </a>
                        </li>
                        <emm:ShowByPermission token="forms.creator">
                            <li>
                                <a href="#" data-toggle-tab="#tab-modal-template-formbuilder">
                                    <mvc:message code="userform.builder"/>
                                </a>
                            </li>
                        </emm:ShowByPermission>
                    </ul>
                </div>
                <div class="modal-body">
                    <div id="tab-modal-template-html" {{ (showHtmlEditor) ? print('data-tab-show') : print('data-tab-hide') }}>
                        <div id="modalTextAreaEditor" class="form-control"></div>
                    </div>
                    <div id="tab-modal-template-wysiwyg" {{ (showWysisygEditor) ? print('data-tab-show') : print('data-tab-hide') }}>
                        <textarea id="modalTextArea" name="modalTextArea"
                                  data-editor-options="allowExternalScript: true, isFullHtml: true"
                                  data-sync="{{= target }}" class="form-control js-editor js-wysiwyg hidden"></textarea>
                    </div>
                    <emm:ShowByPermission token="forms.creator">
                        <div id="tab-modal-template-formbuilder" {{ (showFormBuilder) ? print('data-tab-show') : print('data-tab-hide') }}>
                            <!-- FORM BUILDER -->
                            <div class="row" data-initializer="formbuilder">
                                <div id="modalFormBuilder" class="modal-js-form-builder" data-target-formbuilder="{{= targetFormBuilder }}"></div>
                            </div>
                        </div>
                    </emm:ShowByPermission>

                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <emm:ShowByPermission token="forms.change">
                            <button type="button" class="btn btn-primary btn-large"
                                    data-sync-from="#modalTextArea, #modalFormBuilder" data-sync-to="{{= target }}, {{= targetFormBuilder }}"
                                    data-dismiss="modal" data-action="saveUserForm">
                                <i class="icon icon-save"></i>
                                <span class="text"><mvc:message code="button.Save"/></span>
                            </button>
                        </emm:ShowByPermission>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="warning-html-generation-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                    <h4 class="modal-title text-state-warning">
                        <i class="icon icon-state-warning"></i>
                        <mvc:message code="warning" />
                    </h4>
                </div>

                <div class="modal-body">
                    <p><mvc:message code="userform.builder.generateHtml.question" /></p>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <mvc:message code="button.Cancel" />
                            </span>
                        </button>

                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                <mvc:message code="button.Proceed" />
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="warning-save-different-tabs" type="text/x-mustache-template">
    <div class="modal modal-wide" data-controller="userform-view">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                    <h4 class="modal-title text-state-warning">
                        <i class="icon icon-state-warning"></i>
                        <mvc:message code="warning" />
                    </h4>
                </div>

                <div class="modal-body">
                    <form id="choose-code-format-modal-form">

                        <div class="form-group">
                            <div class="row">
                                <div class="col-sm-8 col-sm-offset-2">
                                    <mvc:message code="GWUA.warning.userform.severalContentTabsChanged"/>
                                </div>
                            </div>
                            <div class="row"></div>
                        </div>

                        {{ if(needSuccessCodeChoose) { }}
                        <div class="form-group" data-field="toggle-vis">
                            <div class="row">
                                <div class="col-sm-4">
                                    <label class="control-label">
                                        <mvc:message code="settings.form.success"/>:
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <label class="radio-inline">
                                        <input type="radio" value="HTML" checked="checked" name="success_template_mode" data-field-vis="" data-field-vis-hide="#success-code-replacement-warning"/>
                                        <mvc:message code="HTML"/>
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" value="FORM_BUILDER" name="success_template_mode" data-field-vis="" data-field-vis-show="#success-code-replacement-warning"/>
                                        <mvc:message code="userform.builder"/>
                                    </label>
                                </div>
                            </div>
                            <div class="row col-sm-offset-4 col-sm-8 text-state-warning" id="success-code-replacement-warning">
                                <i class="icon icon-state-warning"></i>
                                <mvc:message code="userform.builder.generateHtml.question" />
                            </div>
                        </div>
                        {{ } }}

                        {{ if(needErrorCodeChoose) { }}
                        <div class="form-group" data-field="toggle-vis">
                            <div class="row">
                                <div class="col-sm-4">
                                    <label class="control-label">
                                        <mvc:message code="settings.form.error"/>:
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <label class="radio-inline">
                                        <input type="radio" value="HTML" checked="checked" name="error_template_mode" data-field-vis="" data-field-vis-hide="#error-code-replacement-warning"/>
                                        <mvc:message code="HTML"/>
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" value="FORM_BUILDER" name="error_template_mode" data-field-vis="" data-field-vis-show="#error-code-replacement-warning"/>
                                        <mvc:message code="userform.builder"/>
                                    </label>
                                </div>
                            </div>
                            <div class="row col-sm-offset-4 col-sm-8 text-state-warning" id="error-code-replacement-warning">
                                <i class="icon icon-state-warning"></i>
                                <mvc:message code="userform.builder.generateHtml.question" />
                            </div>
                        </div>

                        {{ } }}
                    </form>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <mvc:message code="button.Cancel" />
                            </span>
                        </button>

                        <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-action="save-specific-code-mode">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                <mvc:message code="button.Proceed" />
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
