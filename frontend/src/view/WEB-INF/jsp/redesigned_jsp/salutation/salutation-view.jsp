<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.importvalues.Gender" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.salutation.form.SalutationForm"--%>
<%--@elvariable id="salutationCompanyId" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<c:set var="MALE_GENDER" value="<%= Gender.MALE.getStorageValue() %>"/>
<c:set var="FEMALE_GENDER" value="<%= Gender.FEMALE.getStorageValue() %>"/>
<c:set var="UNKNOWN_GENDER" value="<%= Gender.UNKNOWN.getStorageValue() %>"/>
<c:set var="PRAXIS_GENDER" value="<%= Gender.PRAXIS.getStorageValue() %>"/>
<c:set var="COMPANY_GENDER" value="<%= Gender.COMPANY.getStorageValue() %>"/>

<mvc:message var="genderLabelMsg" code="Gender"/>
<c:set var="genderLabelMsgUC" value="${fn:toUpperCase(genderLabelMsg)}"/>

<c:if test="${empty id}">
    <c:set var="id" value="0"/>
</c:if>

<c:set var="readOnly" value="${salutationCompanyId eq 0}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/salutation/${id}/save.action"
                  id="form" modelAttribute="form"
                  data-form="resource"
                  data-disable-controls="save">

            <div class="modal-header">
                <h1 class="modal-title">
                    <c:choose>
                        <c:when test="${empty id or id == 0}">
                            <mvc:message code="default.salutation.shortname"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="settings.EditFormOfAddress"/>
                        </c:otherwise>
                    </c:choose>
                </h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body row g-3 pt-2">

                <c:if test="${id ne 0}">
                    <div class="col-12">
                        <label for="salutation-id" class="form-label">ID</label>
                        <input type="text" class="form-control" readonly value="${id}">
                    </div>
                </c:if>

                <div class="col-12">
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="salutation-description">${nameMsg}*</label>
                    <mvc:text path="description" readonly="${readOnly}" id="salutation-description" cssClass="form-control" placeholder="${nameMsg}" data-field="required"/>
                </div>

                <div class="col-12">
                    <label for="salutation-male" class="form-label">
                        ${genderLabelMsgUC}=${MALE_GENDER} (<mvc:message code="Male"/>)
                    </label>
                    <mvc:text path="genderMapping[${MALE_GENDER}]" readonly="${readOnly}" id="salutation-male" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label for="salutation-female" class="form-label">
                        ${genderLabelMsgUC}=${FEMALE_GENDER} (<mvc:message code="Female"/>)
                    </label>
                    <mvc:text path="genderMapping[${FEMALE_GENDER}]" readonly="${readOnly}" id="salutation-female" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label for="salutation-unknown" class="form-label">
                        ${genderLabelMsgUC}=${UNKNOWN_GENDER} (<mvc:message code="Unknown"/>)
                    </label>
                    <mvc:text path="genderMapping[${UNKNOWN_GENDER}]" readonly="${readOnly}" id="salutation-unknown" cssClass="form-control"/>
                </div>

                <emm:ShowByPermission token="recipient.gender.extended">
                    <div class="col-12">
                        <label for="salutation-practice" class="form-label">
                            ${genderLabelMsgUC}=${PRAXIS_GENDER} (<mvc:message code="PracticeShort"/>)
                        </label>
                        <mvc:text path="genderMapping[${PRAXIS_GENDER}]" readonly="${readOnly}" id="salutation-practice" cssClass="form-control" />
                    </div>

                    <div class="col-12">
                        <label for="salutation-company" class="form-label">
                            ${genderLabelMsgUC}=${COMPANY_GENDER} (<mvc:message code="recipient.gender.5.short"/>)
                        </label>
                        <mvc:text path="genderMapping[${COMPANY_GENDER}]" readonly="${readOnly}" id="salutation-company" cssClass="form-control" />
                    </div>
                </emm:ShowByPermission>
            </div>
            <emm:ShowByPermission token="salutation.change">
                <c:if test="${not readOnly}">
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary flex-grow-1" data-controls-group="save" data-form-submit>
                            <i class="icon icon-save"></i>
                            <mvc:message code="button.Save"/>
                        </button>
                    </div>
                </c:if>
            </emm:ShowByPermission>
        </mvc:form>
    </div>
</div>
