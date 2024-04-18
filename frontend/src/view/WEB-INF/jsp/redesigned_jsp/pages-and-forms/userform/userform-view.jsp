<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>
<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>

<%--@elvariable id="form" type="com.agnitas.emm.core.userform.form.UserFormForm"--%>
<%--@elvariable id="userFormURLPattern" type="java.lang.String"--%>
<%--@elvariable id="userFormFullURLPattern" type="java.lang.String"--%>
<%--@elvariable id="emmActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="workflowParameters" type="org.agnitas.web.forms.WorkflowParameters"--%>
<%--@elvariable id="companyToken" type="java.lang.String"--%>

<c:url var="actionEditUrlPattern" value="/action/:action-ID:/view.action"/>
<mvc:message var="labMsq" code="lab.message"/>

<mvc:form cssClass="tiles-container hidden d-flex flex-column" servletRelativeAction="/webform/save.action" id="userFormForm"
          modelAttribute="form" data-form="resource"
          data-autosave-scope="action-form/${form.formId}"
          data-controller="userform-view" data-initializer="userform-view" data-validator="userform-edit/form"
          cssStyle="grid-template-columns: 1fr 1fr; grid-template-rows: auto 1fr"
          data-action="saveUserForm" data-editable-view="${agnEditViewKey}">

    <script id="config:userform-view" type="application/json">
        {
            "actionURLPattern": "${actionEditUrlPattern}",
            "formId": ${form.formId}
        }
    </script>

    <script id="config:formbuilderCommon" type="application/json">
   		{
			"companyToken": "${companyToken}",
            "confirmationModal": "warning-html-generation-modal",
            "namesJson": ${emm:toJson(names)},
            "mediapoolImages": ${emm:toJson(mediapoolImages)},
            "profileFieldsForSelect": ${emm:toJson(profileFieldsForSelect)},
            "textProfileFields": ${emm:toJson(textProfileFields)},
            "dateProfileFields": ${emm:toJson(dateProfileFields)},
            "numberProfileFields": ${emm:toJson(numberProfileFields)},
            "cssUrl": "${formCssLocation}/css/bootstrap.min.css"
		}
	</script>

    <mvc:hidden path="formId"/>
    <emm:workflowParameters />

    <div id="settings-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="settings.form.edit"/></h1>
            <div class="tile-controls">
                <div class="form-check form-switch">
                    <mvc:checkbox cssClass="form-check-input" role="switch" id="is-active-switch" path="active"/>
                    <label class="form-label form-check-label" for="is-active-switch"><mvc:message code="default.status.active"/></label>
                </div>
            </div>
        </div>
        <div class="tile-body grid" style="--bs-columns:2">
            <div>
                <label for="formName" class="form-label"><mvc:message var="nameMsg" code="default.Name"/>${nameMsg}&nbsp;*</label>
                <mvc:text path="formName" maxlength="50" size="42" cssClass="form-control" id="formName" placeholder="${nameMsg}"/>
            </div>

            <div>
                <label for="description" class="form-label"><mvc:message var="descriptionMsg" code="Description"/>${descriptionMsg}</label>
                <mvc:text path="description" rows="5" cols="32" cssClass="form-control" id="description" placeholder="${descriptionMsg}"/>
            </div>

            <c:if test="${form.formId ne 0}">
                <div>
                    <label for="formURL" class="form-label"><mvc:message code="default.url"/></label>
                    <div class="d-flex gap-2">
                        <input type="text" id="formURL" class="form-control" maxlength="99" size="42" readonly="readonly" value="${userFormURLPattern}">
                        <button type="button" class="btn btn-info btn-icon btn-icon-sm" data-copyable="" data-copyable-value="${userFormURLPattern}" data-tooltip='<mvc:message code="button.Copy"/>'>
                            <i class="icon icon-copy"></i>
                        </button>
                    </div>
                </div>

                <div>
                    <label for="formTestRecipient" class="form-label"><mvc:message code="userform.test"/></label>
                    <div class="d-flex gap-2">
                        <select id="formTestRecipient" class="form-control js-select" data-action="formTestRecipient">
                            <option value="${userFormFullURLPatternNoUid}"><mvc:message code="userform.test.withoutRecipient"/></option>
                            <c:forEach items="${userFormFullURLPatterns}" var="testURL">
                                <option value="${testURL.userFormUrl}">${testURL.firstname} ${testURL.lastname} (${testURL.email})</option>
                            </c:forEach>
                        </select>
                        <a href="${userFormFullURLPatternNoUid}"
                           id="formTestLink"
                           data-action="${not form.active ? 'activate-and-test' : ''}"
                           data-tooltip="<mvc:message code='userform.test'/>"
                           class="btn btn-icon-sm btn-primary" target="_blank">
                            <i class="icon icon-play-circle"></i>
                        </a>
                    </div>
                </div>
            </c:if>
        </div>
    </div>

    <div class="tiles-block flex-grow-1">
        <%-- Success settings--%>
        <div id="success-form-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="settings.form.success"/></h1>
                <div class="tile-controls">
                    <input id="success-type" name="successSettings.useUrl" ${form.successSettings.useUrl ? 'checked' : ''} type="checkbox" class="icon-switch">
                    <label for="success-type" class="text-switch__label">
                        <span><mvc:message code="Form"/></span>
                        <span><mvc:message code="URL"/></span>
                    </label>
                </div>
            </div>
            <div class="tile-body d-flex flex-column gap-3">
                <div>
                    <label for="startAction" class="form-label"><mvc:message code="form.action.success"/></label>
                    <div class="d-flex gap-2">
                        <mvc:select path="successSettings.startActionId" size="1" cssClass="form-control js-select" id="startAction"
                            data-action="change-intro-action">
                            <mvc:option value="0"><mvc:message code="settings.No_Action"/></mvc:option>
                            <mvc:options items="${emmActions}" itemValue="id" itemLabel="shortname"/>
                        </mvc:select>
                        <emm:ShowByPermission token="actions.show">
                            <mvc:message var="actionSwitchTooltip" code="form.action.switch"/>
                            <a id="startActionLink" class="btn btn-icon btn-icon-sm btn-primary ${form.successSettings.startActionId > 0 ? '' : 'hidden'}" data-tooltip="${actionSwitchTooltip}"
                               href="${fn:replace(actionEditUrlPattern, ':action-ID:', form.successSettings.startActionId)}">
                                <i class="icon icon-pen"></i>
                            </a>
                        </emm:ShowByPermission>
                    </div>
                </div>

                <div class="overflow-hidden flex-grow-1 d-flex flex-column" data-hide-by-checkbox="#success-type">
                    <label class="form-label" for="success-form-editor"><mvc:message code="userform.content"/></label>
                    <div class="tile flex-grow-1" id="success-form-editor" data-multi-editor>
                        <div class="tile-header">
                            <ul class="tile-title-controls gap-1">
                                <li>
                                    <a href="#" class="btn btn-icon-sm btn-inverse active" data-toggle-tab="#successTemplateEditor">
                                        <i class="icon icon-code"></i>
                                    </a>
                                </li>
                                <c:if test="${not form.successSettings.useVelocity}">
                                    <li>
                                        <a href="#" class="btn btn-icon-sm btn-inverse" data-multi-editor-option="wysiwyg" data-toggle-tab="#success-wysiwyg-editor"
                                           data-action="check-velocity-script"
                                           data-action-options="type: success">
                                            <i class="icon icon-font"></i>
                                        </a>
                                    </li>
                                </c:if>
                                <li>
                                    <a href="#" class="btn btn-sm btn-icon-sm w-auto btn-inverse" data-toggle-tab="#success-form-builder-tab">
                                        <i class="icon icon-table"></i>
                                        <i class="icon icon-flask text-secondary" data-tooltip="${labMsq}"></i>
                                    </a>
                                </li>
                            </ul>
                            <div class="tile-controls">
                                <a href="#" class="btn-enlarge" data-enlarged-modal data-modal-set="title: <mvc:message code="settings.form.error"/>">
                                   <i class="icon icon-expand-arrows-alt"></i>
                                </a>
                            </div>
                        </div>
                        <div class="tile-body p-0 border-0">
                            <!-- HTML CODE EDITOR -->
                            <div id="successTemplateEditor" class="h-100"></div>

                            <!-- WYSIWYG EDITOR -->
                            <div id="success-wysiwyg-editor" class="full-height-ck-editor h-100">
                                <mvc:textarea path="successSettings.template" cssClass="form-control js-editor js-wysiwyg"
                                              data-autosave="successForm"
                                              data-editor-options="allowExternalScript: true, isFullHtml: true"
                                              id="successTemplate"/>
                            </div>

                            <!-- FORM BUILDER -->
                            <div data-initializer="formbuilder" id="success-form-builder-tab">
                                <div id="successFormBuilder" class="js-form-builder" data-target="#successTemplate">
                                    <script id="config:formbuilder" type="application/json">
                                        {
                                            "formName": "${empty form.formName ? "new form" : form.formName}${'(success)'}",
                                            "data": ${empty form.successSettings.formBuilderJson ? "\"\"" : form.successSettings.formBuilderJson}
                                        }
                                    </script>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div data-show-by-checkbox="#success-type">
                    <label for="successURL" class="form-label"><mvc:message code="URL"/></label>
                    <mvc:text path="successSettings.url" cssClass="form-control" id="successURL" placeholder="https://www.yourdomain.com/"/>
                </div>

                <div>
                    <label for="finalAction" class="form-label"><mvc:message code="form.action.error"/></label>
                    <div class="d-flex gap-2">
                        <mvc:select path="successSettings.finalActionId" size="1" cssClass="form-control js-select" id="finalAction" data-action="change-final-action">
                            <mvc:option value="0"><mvc:message code="settings.No_Action"/></mvc:option>
                            <mvc:options items="${emmActions}" itemValue="id" itemLabel="shortname"/>
                        </mvc:select>
                        <emm:ShowByPermission token="actions.show">
                            <mvc:message var="actionSwitchTooltip" code="form.action.switch"/>
                            <a id="finalActionLink" class="btn btn-primary btn-icon-sm ${form.successSettings.finalActionId > 0 ? '' : 'hidden'}" data-tooltip="${actionSwitchTooltip}"
                               href="${fn:replace(actionEditUrlPattern, ':action-ID:', form.successSettings.finalActionId)}">
                                <i class="icon icon-pen"></i>
                            </a>
                        </emm:ShowByPermission>
                    </div>
                </div>
            </div>
        </div>

        <%-- Error settings--%>
        <div id="error-form-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="settings.form.error"/></h1>
                <div class="tile-controls">
                    <input id="error-type" name="errorSettings.useUrl" ${form.errorSettings.useUrl ? 'checked' : ''} type="checkbox" class="icon-switch">
                    <label for="error-type" class="text-switch__label">
                        <span><mvc:message code="Form"/></span>
                        <span><mvc:message code="URL"/></span>
                    </label>
                </div>
            </div>
            <div class="tile-body d-flex flex-column">
                <div class="overflow-hidden flex-grow-1 d-flex flex-column" data-hide-by-checkbox="#error-type">
                    <label class="form-label" for="error-form-editor"><mvc:message code="userform.content"/></label>
                    <div class="tile" id="error-form-editor" data-multi-editor>
                        <div class="tile-header">
                            <ul class="tile-title-controls gap-1">
                                <li>
                                    <a href="#" class="btn btn-icon-sm btn-inverse active" data-toggle-tab="#errorTemplateEditor">
                                        <i class="icon icon-code"></i>
                                    </a>
                                </li>
                                <c:if test="${not form.errorSettings.useVelocity}">
                                    <li>
                                        <a href="#" class="btn btn-icon-sm btn-inverse" data-multi-editor-option="wysiwyg" data-toggle-tab="#error-wysiwyg-editor"
                                           data-action="check-velocity-script"
                                           data-action-options="type: error">
                                            <i class="icon icon-font"></i>
                                        </a>
                                    </li>
                                </c:if>
                                <li>
                                    <a href="#" class="btn btn-sm btn-icon-sm w-auto btn-inverse" data-toggle-tab="#error-form-builder-tab">
                                        <i class="icon icon-table"></i>
                                        <i class="icon icon-flask text-secondary" data-tooltip="${labMsq}"></i>
                                    </a>
                                </li>
                            </ul>
                            <div class="tile-controls">
                                <a href="#" class="btn-enlarge" data-enlarged-modal data-modal-set="title: <mvc:message code="settings.form.error"/>">
                                   <i class="icon icon-expand-arrows-alt"></i>
                                </a>
                            </div>
                        </div>
                        <div class="tile-body">
                            <!-- HTML CODE EDITOR -->
                            <div id="errorTemplateEditor" class="h-100"></div>
                            <!-- WYSIWYG EDITOR -->
                            <div id="error-wysiwyg-editor" class="full-height-ck-editor h-100">
                                <mvc:textarea path="errorSettings.template" cssClass="form-control js-editor js-wysiwyg"
                                              data-editor-options="allowExternalScript: true, isFullHtml: true"
                                              rows="14" cols="75" data-autosave="errorForm"
                                              id="errorTemplate"/>
                            </div>
                            <!-- FORM BUILDER -->
                            <div id="error-form-builder-tab" data-initializer="formbuilder">
                                <div id="errorFormBuilder" class="js-form-builder" data-target="#errorTemplate">
                                    <script id="config:formbuilder" type="application/json">
                                        {
                                            "formName": "${empty form.formName ? "new form" : form.formName}${'(error)'}",
                                            "data": ${empty form.errorSettings.formBuilderJson ? "\"\"" : form.errorSettings.formBuilderJson}
                                        }
                                    </script>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div data-show-by-checkbox="#error-type">
                    <label for="errorURL" class="form-label"><mvc:message code="URL"/></label>
                    <mvc:text path="errorSettings.url" cssClass="form-control" id="errorURL"/>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<script id="warning-html-generation-modal" type="text/x-mustache-template">
    <div class="modal modal-warning" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        <i class="icon icon-state-warning"></i>
                        <mvc:message code="warning" />
                    </h1>
                    <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body">
                    <p><mvc:message code="userform.builder.generateHtml.question" /></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary js-confirm-positive flex-grow-1">
                        <i class="icon icon-check"></i>
                        <b><mvc:message code="button.Proceed"/></b>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="warning-save-different-tabs" type="text/x-mustache-template">
    <div class="modal modal-warning" tabindex="-1" data-controller="userform-view">
        <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        <i class="icon icon-state-warning"></i>
                        <mvc:message code="warning" />
                    </h1>
                    <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body">
                    <mvc:form cssClass="g-3" id="choose-code-format-modal-form">
                        <mvc:message code="warning.userform.change.code"/>
                        <div class="row mt-3">
                            {{ if (needSuccessCodeChoose) { }}
                                <div class="col">
                                    <label for="success-template-mode" class="form-label"><mvc:message code="settings.form.success"/></label>
                                    <select name="success_template_mode" class="form-control js-select" id="success-template-mode" >
                                        <option value="HTML"><mvc:message code="HTML"/></option>
                                        <option value="FORM_BUILDER"><mvc:message code="userform.builder"/></option>
                                    </select>
                                    <div class="notification-simple notification-simple--warning mt-3" data-show-by-select="#success-template-mode" data-show-by-select-values="FORM_BUILDER">
                                        <i class="icon icon-state-warning"></i>
                                        <mvc:message code="userform.builder.generateHtml.question" />
                                    </div>
                                </div>
                            {{ } }}

                            {{ if (needErrorCodeChoose) { }}
                                <div class="col">
                                    <label for="error-template-mode" class="form-label"><mvc:message code="settings.form.error"/></label>
                                    <select name="error_template_mode" class="form-control js-select" id="error-template-mode" >
                                        <option value="HTML"><mvc:message code="HTML"/></option>
                                        <option value="FORM_BUILDER"><mvc:message code="userform.builder"/></option>
                                    </select>
                                    <div class="notification-simple notification-simple--warning mt-3" data-show-by-select="#error-template-mode" data-show-by-select-values="FORM_BUILDER">
                                        <i class="icon icon-state-warning"></i>
                                        <mvc:message code="userform.builder.generateHtml.question" />
                                    </div>
                                </div>
                            {{ } }}
                        </div>
                    </mvc:form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary flex-grow-1" data-action="save-specific-code-mode">
                        <i class="icon icon-check"></i>
                        <b><mvc:message code="button.Proceed"/></b>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<c:if test="${form.formId gt 0}">
    <script id="userform-activate-and-test" type="text/x-mustache-template">
        <div class="modal" tabindex="-1">
            <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title"><mvc:message code="userform.activate" /></h1>
                        <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p><mvc:message code="userform.test.activate.question" /></p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger js-confirm-negative flex-grow-1" data-bs-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="default.No"/></span>
                        </button>
                        <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive" data-bs-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="default.Yes"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </script>
</c:if>
