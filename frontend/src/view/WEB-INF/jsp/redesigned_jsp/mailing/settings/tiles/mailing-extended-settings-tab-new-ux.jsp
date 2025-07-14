<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="showDynamicTemplateToggle" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingGrid" type="java.lang.Boolean"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>

<c:set var="isParamsReadonly" value="${true}"/>
<emm:ShowByPermission token="mailing.parameter.change">
    <c:set var="isParamsReadonly" value="${isSettingsReadonly or not MAILING_EDITABLE}"/>
</emm:ShowByPermission>

<%@ include file="fragments/mailing-settings-reference-content-new-ux.jspf" %>

<emm:ShowByPermission token="mailing.parameter.show">
    <c:if test="${not isParamsReadonly or fn:length(mailingSettingsForm.params) gt 0}">
        <div>
            <label class="form-label" for="mailingParamsTable"><mvc:message code="MailingParameter"/></label>
            <div id="mailingParamsTable" data-input-table>
                <script data-initializer="mailing-params" type="application/json">
                    {
                      "params": ${emm:toJson(mailingSettingsForm.params)}
                    }
                </script>
                <script data-config type="application/json">
                    {
                      "data": ${emm:toJson(mailingSettingsForm.params)},
                      "readonly": ${isParamsReadonly}
                    }
                </script>
                <script data-row-template type="text/x-mustache-template">
                    <tr>
                       <td><input type="text" class="form-control" ${isParamsReadonly ? 'readonly' : ''} data-name="name" value="{{- name }}" placeholder='<mvc:message code="default.Name"/>'></td>
                       <td><input type="text" class="form-control" ${isParamsReadonly ? 'readonly' : ''} data-name="value" value="{{- value }}" placeholder='<mvc:message code="Value"/>'></td>
                       <td><input type="text" class="form-control" ${isParamsReadonly ? 'readonly' : ''} data-name="description" value="{{- description }}" placeholder='<mvc:message code="Description"/>'></td>
                    </tr>
                </script>
            </div>
        </div>
    </c:if>
</emm:ShowByPermission>
