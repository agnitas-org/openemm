<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="isChangeable" value="${false}"/>
<emm:ShowByPermission token="mailing.parameter.change">
    <c:set var="isChangeable" value="${true}"/>
</emm:ShowByPermission>

<div class="tile" data-action="scroll-to">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingParameters">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="MailingParameter"/>
        </a>
    </div>

    <div id="tile-mailingParameters" class="tile-content tile-content-forms">
        <div class="table-responsive" data-controller="mailing-params">
            <script data-initializer="mailing-params" type="application/json">
                {
                  "params": ${emm:toJson(mailingSettingsForm.params)},
                  "isChangeable": ${isChangeable}
                }
            </script>
            <table class="table table-bordered table-striped" id="mailingParamsTable">
                <thead>
                    <tr>
                        <th><mvc:message code="default.Name"/></th>
                        <th><mvc:message code="Value"/></th>
                        <th><mvc:message code="default.description"/></th>
                        <c:if test="${isChangeable}">
                            <th></th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                    <%-- loads by js--%>
                </tbody>
            </table>
        </div>
    </div>
</div>
