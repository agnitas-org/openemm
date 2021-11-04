<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="forceShowAdvancedSearchTab" type="java.lang.Boolean"--%>
<%--@elvariable id="hasAnyDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="queryBuilderFilters" type="java.lang.String"--%>
<%--@elvariable id="mailTrackingAvailable" type="java.lang.Boolean"--%>

<c:set var="RESET_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESET_PARAM_NAME%>"/>

<div id="tab-advancedSearch" class="hidden" ${forceShowAdvancedSearchTab ? 'data-tab-show="true"' : ''}>
    <div class="tile-content-forms" style="padding-bottom: 0">
        <jsp:include page="recipient-list-base-search-tab.jsp">
            <jsp:param name="advanced" value="true"/>
        </jsp:include>

        <div class="row" data-initializer="target-group-query-builder">
            <script id="config:target-group-query-builder" type="application/json">
                {
                    "mailTrackingAvailable": ${mailTrackingAvailable},
                    "helpLanguage": "${helplanguage}",
                    "queryBuilderRules": ${emm:toJson(form.searchQueryBuilderRules)},
                    "queryBuilderFilters": ${queryBuilderFilters}
                }
            </script>

            <div class="col-md-12">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label"></label>
                    </div>
                    <div class="col-md-12">
                        <div id="targetgroup-querybuilder">
                            <mvc:hidden path="searchQueryBuilderRules" id="queryBuilderRules"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>


        <div class="row">
            <div class="col-sm-12">
                <div class="form-group">
                    <div class="col-sm-12">
                        <button type="button" tabindex="-1" class="btn btn-regular"
                                data-help="help_${helplanguage}/recipient/AdvancedSearchMsg.xml">
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
                            <!-- hide if no queries are present BEGIN -->
                            <button id="target-group-save-button"
                                    type="button" tabindex="-1" class="btn btn-regular hidden"
                                    data-action="createNewTarget">
                                <mvc:message code="recipient.saveSearch"/>
                            </button>
                            <!-- hide if no queries are present END -->

                            <button id="refresh-button"
                                    class="btn btn-primary btn-regular" type="button"
                                    data-form-set="advancedSearch:true"
                                    data-sync-from="#search_mailinglist_advanced, #search_targetgroup_advanced, #search_recipient_type_advanced, #search_recipient_state_advanced"
                                    data-sync-to="#search_mailinglist, #search_targetgroup, #search_recipient_type, #search_recipient_state"
                                    data-form-persist="page: '1'"
                                    data-form-url="<c:url value='/recipient/search.action'/>"
                                    data-action="refresh-advanced-search-new">
                                <i class="icon icon-refresh"></i>
                                <span class="text"><mvc:message code="button.Refresh"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <hr>
</div>

<script id="new-targetgroup-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title"><mvc:message code="recipient.saveSearch"/></h4>
                </div>

                <mvc:form servletRelativeAction="/recipient/createTargetGroup.action" data-form-focus="shortname" method="post">
                    <input type="hidden" name="queryBuilderRules" value="{{- rules}}">
                    <div class="modal-body">
                        <div class="form-group" data-field="required">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <label for="newTargetName">
                                        <mvc:message var="nameMsg" code="default.Name"/>
                                        ${nameMsg}
                                    </label>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <input type="text" id="newTargetName" name="shortname" maxlength="99" class="form-control"
                                       data-field-required="" placeholder="${nameMsg}"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <label for="newTargetDescription">
                                        <mvc:message var="descriptionMsg" code="default.description"/>
                                        ${descriptionMsg}
                                    </label>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <textarea id="newTargetDescription" name="description" class="form-control" rows="5" cols="32" placeholder="${descriptionMsg}"></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><mvc:message code="button.Cancel"/></span>
                            </button>
                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text"><mvc:message code="button.Save"/></span>
                            </button>
                        </div>
                    </div>
                </mvc:form>
            </div>
        </div>
    </div>
</script>
