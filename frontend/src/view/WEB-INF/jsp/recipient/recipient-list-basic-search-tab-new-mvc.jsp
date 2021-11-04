<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map"--%>
<%--@elvariable id="hasAnyDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="recipientList" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<c:set var="RESET_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESET_PARAM_NAME%>"/>

<div id="tab-basicSearch" class="hidden">
    <div class="tile-content-forms" style="padding-bottom: 0">
        <jsp:include page="recipient-list-base-search-tab.jsp">
            <jsp:param name="advanced" value="false"/>
        </jsp:include>

        <div class="row">
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_first_name" class="control-label">
                            <mvc:message code="Firstname"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <mvc:text path="searchFirstName" id="search_first_name" cssClass="form-control"/>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_name" class="control-label">
                            <mvc:message code="Lastname"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <mvc:text path="searchLastName" id="search_name" cssClass="form-control"/>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_email" class="control-label">
                            <mvc:message code="mailing.MediaType.0"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <mvc:text path="searchEmail" id="search_email" cssClass="form-control"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-12">
                <div class="form-group">
                    <div class="col-sm-12">
                        <button type="button" tabindex="-1" class="btn btn-regular"
                                data-help="help_${helplanguage}/recipient/SearchMsg.xml">
                            <i class="icon icon-question-circle"></i>
                            <mvc:message code="help"/>
                        </button>

                        <div class="btn-group pull-right">
                            <c:url var="resetSearchLink" value="/recipient/search.action">
                                <c:param name="${RESET_SEARCH_PARAM_NAME}" value="true"/>
                            </c:url>
                            <a data-form-url="${resetSearchLink}" data-form-submit class="btn btn-regular">
                                <mvc:message code="button.search.reset"/>
                            </a>

                            <c:if test="${recipientList.getFullListSize() <= 0}">
                                <c:set var="isCreateRecipientButtonShown"
                                       value="${not empty fn:trim(form.searchFirstName) or not empty fn:trim(form.searchLastName) or not empty fn:trim(form.searchEmail)}"/>

                                <c:if test="${isCreateRecipientButtonShown}">
                                    <c:url var="createNewRecipientUrl" value="/recipient/create.action">
                                        <c:param name="firstname" value="${form.searchFirstName}"/>
                                        <c:param name="lastname" value="${form.searchLastName}"/>
                                        <c:param name="email" value="${form.searchEmail}"/>
                                    </c:url>
                                    <a href="${createNewRecipientUrl}" class="btn btn-primary btn-regular">
                                        <i class="icon icon-plus"></i>
                                        <span class="text"><mvc:message code="button.create.recipient"/></span>
                                    </a>
                                </c:if>
                            </c:if>

                            <button class="btn btn-primary btn-regular pull-right" type="button"
                                    data-sync-from="#search_mailinglist, #search_targetgroup, #search_recipient_type, #search_recipient_state"
                                    data-sync-to="#search_mailinglist_advanced, #search_targetgroup_advanced, #search_recipient_type_advanced, #search_recipient_state_advanced"
                                    data-form-url="<c:url value='/recipient/search.action'/>"
                                    data-action="refresh-basic-search-new" data-form-persist="page: '1'">
                                <i class="icon icon-search"></i>
                                <span class="text"><mvc:message code="Search"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <hr>
</div>
