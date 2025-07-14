<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateName" type="java.lang.String"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="templateShortname" type="java.lang.String"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="showDynamicTemplateToggle" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="TEXTAREA_WIDTH" value="75" scope="page"/>
<c:set var="emailSettingsDisabled" value="${not MAILING_EDITABLE or not emailSettingsEditable}"/>
<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>

<c:if test="${empty templateShortname}">
    <c:set var="templateShortname"><mvc:message code="mailing.No_Template"/></c:set>
</c:if>

<c:set var="layoutNameBlock">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><mvc:message code="Layout"/></label>
        </div>
        <div class="col-sm-8">
            <input type="text" class="form-control" value="${gridTemplateName}" readonly>
        </div>
    </div>
</c:set>
<c:set var="templateNameBlock">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><mvc:message code="Template"/></label>
        </div>
        <div class="col-sm-8">
            <input type="text" class="form-control" value="${templateShortname}" readonly>
        </div>
    </div>
</c:set>

<script id="frame-tile-template" type="text/html">
<emm:ShowByPermission token="template.show">
    <div id="frame-tile" class="tile" data-action="scroll-to">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-mailingTemplate">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="mailing.frame"/>
            </a>
            <c:if test="${not isTemplate and mailingId ne 0}">
                <ul class="tile-header-nav">
                    <li class="active">
                        <a href="#" data-toggle-tab="#tab-mailingTemplateBase"><mvc:message code="default.basic"/></a>
                    </li>
                    <emm:ShowByPermission token="settings.extended">
                    <li>
                        <a href="#" data-toggle-tab="#tab-mailingTemplateAdvanced" data-extends-tab="#tab-mailingTemplateBase"><mvc:message code="default.advanced"/></a>
                    </li>
                    </emm:ShowByPermission>
                </ul>
            </c:if>
        </div>
        <div id="tile-mailingTemplate" class="tile-content tile-content-forms">
            <c:if test="${not isTemplate}">
                <div id="tab-mailingTemplateBase">
                    <c:choose>
                        <c:when test="${mailingId eq 0}">
                            <c:choose>
                                <c:when test="${isMailingGrid}">
                                    ${layoutNameBlock}
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${isCopying}">
                                        ${templateNameBlock}
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${isMailingGrid}">
                                    ${layoutNameBlock}
                                </c:when>
                                <c:otherwise>
                                    ${templateNameBlock}
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div id="tab-mailingTemplateAdvanced" class="${mailingId ne 0 ? 'hidden' : ''}">
            </c:if>

            <emm:ShowByPermission token="settings.extended">
                <div class="inline-tile form-group" data-field="validator">
                    <div class="inline-tile-header">
                        <h2 class="headline"><mvc:message code="Text_Version"/></h2>
                        <ul class="inline-tile-header-actions">
                            <li>
                                <a href="#" data-modal="modal-editor"
                                   data-modal-set="title: <mvc:message code="Text_Version"/>, target: textTemplate, id: textTemplateLarge, type: text"
                                   data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                                    <i class="icon icon-arrows-alt"></i>
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="inline-tile-content">
                        <div class="row">
                            <div class="col-sm-12">
                                <mvc:textarea path="emailMediatype.textTemplate" id="textTemplate" rows="14"
                                              cols="${TEXTAREA_WIDTH}"
                                              data-field-validator="reject-script-element"
                                              data-action="count-textarea-chars"
                                              cssClass="form-control js-editor-text"
                                              readonly="${not MAILING_EDITABLE or isSettingsReadonly}"/>
                                <div class="align-right" data-char-counter-for="textTemplate">
                                    <span class="small status">&nbsp;</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <c:if test="${mailingSettingsForm.emailMediatype.mailFormat != 0 and not isMailingGrid}">
                    <div class="inline-tile form-group" data-field="validator">
                        <div class="inline-tile-header">
                            <h2 class="headline"><mvc:message code="mailing.HTML_Version"/></h2>
                            <ul class="inline-tile-header-actions">
                                <li>
                                    <a href="#" data-modal="modal-editor"
                                       data-modal-set="title: <mvc:message code="mailing.HTML_Version"/>, target: htmlTemplate, id: htmlTemplateLarge"
                                       data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                                        <i class="icon icon-arrows-alt"></i>
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="inline-tile-content">
                            <div class="row">
                                <div class="col-sm-12">
                                    <mvc:textarea path="emailMediatype.htmlTemplate" id="htmlTemplate" rows="14"
                                                  cols="${TEXTAREA_WIDTH}"
                                                  data-field-validator="reject-script-element"
                                                  data-action="count-textarea-chars"
                                                  cssClass="form-control js-editor" readonly="${not MAILING_EDITABLE or isSettingsReadonly}"/>
                                    <div class="align-right" data-char-counter-for="htmlTemplate">
                                        <span class="small status">&nbsp;</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${showDynamicTemplateToggle}">
                    <div class="form-group checkbox">
                        <label class="toggle">
                            <mvc:checkbox path="useDynamicTemplate" disabled="${isSettingsReadonly}"/>
                            <div class="toggle-control"></div>
                            <span class="text">
                                <c:if test="${isTemplate}">
                                    <mvc:message code="mailing.dynamic_template.preset"/>
                                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/TemplateUpdateMailingMsg.xml" tabindex="-1" type="button"></button>
                                </c:if>
                                <c:if test="${not isTemplate}">
                                    <mvc:message code="mailing.dynamic_template"/>
                                </c:if>
                            </span>
                        </label>
                    </div>
                </c:if>
            </emm:ShowByPermission>
            <emm:HideByPermission token="settings.extended">
                <mvc:hidden path="emailMediatype.textTemplate"/>
                <mvc:hidden path="emailMediatype.htmlTemplate"/>
                <mvc:hidden path="useDynamicTemplate"/>
            </emm:HideByPermission>

            <c:if test="${not isTemplate}">
            </div>
            </c:if>
        </div>
    </div>
</emm:ShowByPermission>
<emm:HideByPermission token="template.show">
    <mvc:hidden path="emailMediatype.textTemplate"/>
    <mvc:hidden path="emailMediatype.htmlTemplate"/>
</emm:HideByPermission>
</script>

<script id="modal-editor" type="text/x-mustache-template">
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
                    <textarea id="{{= id }}" data-sync="\#{{= target}}"
                              data-action="count-textarea-chars"
                              class="form-control js-editor{{- (typeof(type) == 'undefined') ? '' : '-' + type }}" ${not MAILING_EDITABLE or isSettingsReadonly ? 'readonly' : ''}></textarea>
                    <div class="modal-char-counter" data-char-counter-for="{{= id }}" style="display: block">
                        <span class="small status">&nbsp;</span>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>

                        <c:choose>
                            <c:when test="${isTemplate}">
                                <c:set var="permToken" value="template.change"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="permToken" value="mailing.change"/>
                            </c:otherwise>
                        </c:choose>
                        <emm:ShowByPermission token="${permToken}">
                            <button type="button" class="btn btn-primary btn-large" data-sync-from="\#{{= id }}"
                                    data-sync-to="\#{{= target }}" data-dismiss="modal"
                                    data-form-target='#mailingSettingsForm' ${MAILING_EDITABLE and not isSettingsReadonly ? 'data-form-submit-event' : 'disabled'} data-controls-group='save'>
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
