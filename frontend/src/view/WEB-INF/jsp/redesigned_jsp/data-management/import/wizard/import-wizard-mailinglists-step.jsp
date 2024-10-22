<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>

<c:set var="step" value="7" />
<c:url var="backUrl" value="/recipient/import/wizard/step/preScan.action" />

<mvc:form servletRelativeAction="/recipient/import/wizard/step/mailinglists.action" modelAttribute="importWizardSteps" cssClass="tiles-container" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <div class="notification-simple notification-simple--lg notification-simple--info">
                        <span>
                            <mvc:message code="import.SubscribeLists"/>
                            <a href="#" type="button" class="icon icon-question-circle" data-help="help_${helplanguage}/importwizard/step_6/SubscribeLists.xml"></a>
                        </span>
                    </div>
                </div>
                <div class="col-12">
                    <div class="bordered-box-sm">
                        <div class="row g-2">
                            <div class="col-12">
                                <h3 class="text-dark"><mvc:message code="Mailinglists"/></h3>
                            </div>

                            <c:forEach var="mailinglist" items="${mailinglists}">
                                <div class="col-12">
                                    <div class="form-check form-switch">
                                        <mvc:checkbox id="mailing-list-${mailinglist.id}" path="helper.mailingLists" cssClass="form-check-input" role="switch" value="${mailinglist.id}" />
                                        <label class="form-label form-check-label fw-normal text-truncate" for="mailing-list-${mailinglist.id}">
                                            ${mailinglist.shortname} (ID ${mailinglist.id})
                                        </label>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
