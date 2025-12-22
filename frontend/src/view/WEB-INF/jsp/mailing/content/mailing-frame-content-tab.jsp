<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingGrid" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
<%--@elvariable id="showDynamicTemplateToggle" type="java.lang.Boolean"--%>
<%--@elvariable id="mediatypes" type="com.agnitas.emm.core.mediatypes.common.MediaTypes"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<c:set var="saveBtnExtraAttrs">
    <c:choose>
        <c:when test="${(not empty isMailingEditable and not isMailingEditable) or not MAILING_EDITABLE or isSettingsReadonly}">
            disabled
        </c:when>
        <c:otherwise>
            data-form-set="preventReload: true" ${param.fromContentTab ? 'data-action="save"' : 'data-form-submit-event'}
        </c:otherwise>
    </c:choose>
</c:set>

<c:set var="frameContentControls">
    <button type="button" class="btn btn-secondary full-screen-shown px-2" ${saveBtnExtraAttrs}>
        <i class="icon icon-save"></i>
        <mvc:message code="button.Save"/>
    </button>
    <button type="button" class="btn btn-icon btn-secondary" data-full-screen>
        <i class="icon icon-expand-arrows-alt"></i>
    </button>
</c:set>

<emm:ShowByPermission token="template.show">
    <emm:ShowByPermission token="settings.extended">
        <c:if test="${param.showHtml}">
            <div class="tile tile--highlighted" id="html-content-tile"
                 data-toggle-tile
                 data-field="validator"
                 data-hide-by-select="#emailMailFormat" data-hide-by-select-values="0">
                <div class="tile-header px-2 py-1">
                    <h1 class="tile-title gap-1">
                        <i class="icon icon-caret-up"></i>
                        <span class="fs-3 fw-semibold"><mvc:message code="mailing.HTML_Version"/></span>
                    </h1>
                    <div class="tile-controls">
                        ${frameContentControls}
                    </div>
                </div>
                <div class="tile-body d-flex flex-column p-0">
                    <mvc:textarea path="${param.fromContentTab ? '' : 'emailMediatype.'}htmlTemplate" id="htmlTemplate"
                                  cssClass="form-control js-editor"
                                  readonly="${not MAILING_EDITABLE or isSettingsReadonly ? true : ''}"
                                  data-action="validate-on-change"
                                  data-show-char-counter="tile-footer"
                                  data-field-validator="reject-script-element"/>
                </div>
                <div class="tile-footer text-secondary fs-3 border-top text-nowrap">
                    <c:if test="${showDynamicTemplateToggle}">
                        <span class="form-check form-switch mt-auto overflow-hidden">
                            <mvc:checkbox cssClass="form-check-input" path="useDynamicTemplate" id="dynamic-template-large" data-sync="#dynamic-template" role="switch" disabled="${isSettingsReadonly}"/>
                            <label class="form-label form-check-label text-truncate">
                                <c:if test="${isTemplate}">
                                    <mvc:message code="mailing.dynamic_template.preset"/>
                                    <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/TemplateUpdateMailingMsg.xml"></a>
                                </c:if>
                                <c:if test="${not isTemplate}">
                                    <mvc:message code="mailing.dynamic_template"/>
                                </c:if>
                            </label>
                        </span>
                    </c:if>
                    <c:if test="${not showDynamicTemplateToggle}">
                        <mvc:hidden path="useDynamicTemplate" id="dynamic-template"/>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${param.showText}">
            <div class="tile tile--highlighted" id="text-content-tile" data-field="validator" data-toggle-tile>
                <div class="tile-header px-2 py-1">
                    <h1 class="tile-title gap-1">
                        <i class="icon icon-caret-up"></i>
                        <span class="fs-3 fw-semibold"><mvc:message code="Text_Version"/></span>
                    </h1>
                    <div class="tile-controls">
                        ${frameContentControls}
                    </div>
                </div>
                <div class="tile-body d-flex flex-column p-0">
                    <mvc:textarea path="${param.fromContentTab ? '' : 'emailMediatype.'}textTemplate" id="textTemplate"
                                  cssClass="form-control js-editor-text"
                                  readonly="${not MAILING_EDITABLE or isSettingsReadonly ? true : ''}"
                                  data-action="validate-on-change"
                                  data-show-char-counter="tile-footer"
                                  data-field-validator="reject-script-element"/>
                </div>
            </div>
        </c:if>

        <%@ include file="fragments/extended-mediatypes-template-inputs.jspf" %>

    </emm:ShowByPermission>
    <emm:HideByPermission token="settings.extended">
        <mvc:hidden path="${param.fromContentTab ? '' : 'emailMediatype.'}textTemplate"/>
        <mvc:hidden path="${param.fromContentTab ? '' : 'emailMediatype.'}htmlTemplate"/>
        <mvc:hidden path="useDynamicTemplate"/>
    </emm:HideByPermission>
</emm:ShowByPermission>
<emm:HideByPermission token="template.show">
    <mvc:hidden path="${param.fromContentTab ? '' : 'emailMediatype.'}textTemplate"/>
    <mvc:hidden path="${param.fromContentTab ? '' : 'emailMediatype.'}htmlTemplate"/>
</emm:HideByPermission>
