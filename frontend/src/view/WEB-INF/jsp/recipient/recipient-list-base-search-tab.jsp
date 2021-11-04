<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>

<c:set var="isAdvanced" value="${param.advanced}"/>
<c:set var="advancedSuffix" value="${isAdvanced ? '_advanced' : ''}"/>

<c:set var="USER_TYPE_ADMIN" value="<%= BindingEntry.UserType.Admin.getTypeCode() %>"/>
<c:set var="USER_TYPE_TEST" value="<%= BindingEntry.UserType.TestUser.getTypeCode() %>"/>
<c:set var="USER_TYPE_NORMAL" value="<%= BindingEntry.UserType.World.getTypeCode() %>"/>
<c:set var="USER_TYPE_TEST_VIP" value="<%= BindingEntry.UserType.TestVIP.getTypeCode() %>"/>
<c:set var="USER_TYPE_NORMAL_VIP" value="<%= BindingEntry.UserType.WorldVIP.getTypeCode() %>"/>

<div class="row">
    <div class="col-md-3">
        <div class="form-group">
            <div class="col-md-12">
                <label class="control-label" for="search_mailinglist${advancedSuffix}">
                    <mvc:message code="Mailinglist"/>
                </label>
            </div>
            <div class="col-md-12">
                <mvc:select path="filterMailinglistId" cssClass="form-control js-select"
                            id="search_mailinglist${advancedSuffix}" data-action="change-mailinglist-id" size="1"
                            data-form-change="0">
                    <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                    <c:if test="${not hasAnyDisabledMailingLists}">
                        <mvc:option value="-1"><mvc:message code="No_Mailinglist"/></mvc:option>
                    </c:if>
                    <c:forEach var="mailinglist" items="${mailinglists}">
                        <mvc:option value="${mailinglist.id}">${mailinglist.shortname}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="form-group">
            <div class="col-md-12">
                <label class="control-label" for="search_targetgroup${advancedSuffix}">
                    <mvc:message code="Target"/>
                </label>
            </div>
            <div class="col-md-12">
                <mvc:select path="filterTargetId" cssClass="form-control js-select"
                            id="search_targetgroup${advancedSuffix}" size="1"
                            data-form-change="0">
                    <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                    <c:forEach var="target" items="${targets}">
                        <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="form-group">
            <div class="col-md-12">
                <label class="control-label" for="search_recipient_type${advancedSuffix}">
                    <mvc:message code="recipient.RecipientType"/>
                </label>
            </div>
            <div class="col-md-12">
                <!--todo: GWUA-4769 sort out why field is not disabled after refresh-->
                <mvc:select path="filterUserType" cssClass="form-control js-select"
                            id="search_recipient_type${advancedSuffix}" size="1"
                            data-form-change="0" disabled="${form.filterMailinglistId eq -1 ? 'disabled' : ''}">
                    <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                    <mvc:option value="${USER_TYPE_ADMIN}"><mvc:message code="recipient.Administrator"/></mvc:option>
                    <mvc:option value="${USER_TYPE_TEST}"><mvc:message code="TestSubscriber"/></mvc:option>
                    <%@include file="/WEB-INF/jsp/recipient/recipient-novip-test-new.jspf" %>
                    <mvc:option value="${USER_TYPE_NORMAL}"><mvc:message code="NormalSubscriber"/></mvc:option>
                    <%@include file="/WEB-INF/jsp/recipient/recipient-novip-normal-new.jspf" %>
                </mvc:select>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="form-group">
            <div class="col-md-12">
                <label class="control-label" for="search_recipient_state${advancedSuffix}">
                    <mvc:message code="recipient.RecipientStatus"/>
                </label>
            </div>
            <div class="col-md-12">
                <mvc:select path="filterUserStatus" cssClass="form-control js-select" size="1"
                            id="search_recipient_state${advancedSuffix}"
                            data-form-change="0" disabled="${form.filterMailinglistId eq -1 ? 'disabled' : ''}">
                    <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                    <mvc:option value="1"><mvc:message code="recipient.MailingState1"/></mvc:option>
                    <mvc:option value="2"><mvc:message code="recipient.MailingState2"/></mvc:option>
                    <mvc:option value="3"><mvc:message code="recipient.OptOutAdmin"/></mvc:option>
                    <mvc:option value="4"><mvc:message code="recipient.OptOutUser"/></mvc:option>
                    <mvc:option value="5"><mvc:message code="recipient.MailingState5"/></mvc:option>
                    <emm:ShowByPermission token="blacklist">
                        <mvc:option value="6"><mvc:message code="recipient.MailingState6"/></mvc:option>
                    </emm:ShowByPermission>
                    <mvc:option value="7"><mvc:message code="recipient.MailingState7"/></mvc:option>
                </mvc:select>
            </div>
        </div>
    </div>
</div>
