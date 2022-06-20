<%@ page language="java" contentType="text/html; charset=utf-8" %>
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

<c:set var="genderLabelMsg"><mvc:message code="Gender"/></c:set>
<c:set var="genderLabelMsgUC" value="${fn:toUpperCase(genderLabelMsg)}"/>

<c:if test="${empty id}">
    <c:set var="id" value="0"/>
</c:if>

<mvc:form servletRelativeAction="/salutation/${id}/save.action" id="form" modelAttribute="form" data-form="resource">
    <c:choose>
    	<c:when test="${salutationCompanyId eq 0}">
            <c:set var="readOnly" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="readOnly" value="false"/>
        </c:otherwise>
	</c:choose>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <c:choose>
                    <c:when test="${empty id or id == 0}">
                        <mvc:message code="default.salutation.shortname"/>
                    </c:when>
                    <c:otherwise>
                        <mvc:message code="settings.EditFormOfAddress"/>
                    </c:otherwise>
                </c:choose>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <c:if test="${id ne 0}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            ID:
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <p class="form-control-static">${id}</p>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="salutation-description" class="control-label">
                        <mvc:message code="Description"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="description" readonly="${readOnly}" id="salutation-description" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="salutation-male" class="control-label">
                        ${genderLabelMsgUC}=${MALE_GENDER} (<mvc:message code="Male"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="genderMapping[${MALE_GENDER}]" readonly="${readOnly}" id="salutation-male" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="salutation-female" class="control-label">
                        ${genderLabelMsgUC}=${FEMALE_GENDER} (<mvc:message code="Female"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="genderMapping[${FEMALE_GENDER}]" readonly="${readOnly}" id="salutation-female" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="salutation-unknown" class="control-label">
                        ${genderLabelMsgUC}=${UNKNOWN_GENDER} (<mvc:message code="Unknown"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="genderMapping[${UNKNOWN_GENDER}]" readonly="${readOnly}" id="salutation-unknown" cssClass="form-control"/>
                </div>
            </div>

            <emm:ShowByPermission token="recipient.gender.extended">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="salutation-practice" class="control-label">
                            ${genderLabelMsgUC}=${PRAXIS_GENDER} (<mvc:message code="PracticeShort"/>)
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="genderMapping[${PRAXIS_GENDER}]" readonly="${readOnly}" id="salutation-practice" cssClass="form-control" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="salutation-company" class="control-label">
                            ${genderLabelMsgUC}=${COMPANY_GENDER} (<mvc:message code="recipient.gender.5.short"/>)
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="genderMapping[${COMPANY_GENDER}]" readonly="${readOnly}" id="salutation-company" cssClass="form-control" />
                    </div>
                </div>
            </emm:ShowByPermission>
        </div>
    </div>
</mvc:form>
