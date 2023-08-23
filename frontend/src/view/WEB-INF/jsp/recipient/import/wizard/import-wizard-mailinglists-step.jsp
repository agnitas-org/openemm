<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/mailinglists.action" modelAttribute="importWizardSteps" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-notification tile-notification-info">
            <mvc:message code="SubscribeLists"/>
            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_6/SubscribeLists.xml"></button>
        </div>
        <div class="tile-content tile-content-forms">
            <c:forEach var="mailinglist" items="${mailinglists}">
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <div class="checkbox">
                            <label class="import_classic_malists_label" for='mailing-list-${mailinglist.id}'>
                                <mvc:checkbox path="helper.mailingLists" id='mailing-list-${mailinglist.id}' value="${mailinglist.id}"/>
                                ${mailinglist.shortname} (ID ${mailinglist.id})
                            </label>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:set>
    <c:set var="step" value="7"/>
    <c:url var="backUrl" value="/recipient/import/wizard/step/preScan.action"/>
    
    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
