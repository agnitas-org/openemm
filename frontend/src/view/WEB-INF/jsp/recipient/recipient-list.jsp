<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ page import="org.agnitas.util.AgnUtils" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="listForm" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="deactivatePagination" type="java.lang.Boolean"--%>
<%--@elvariable id="recipientList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="loadRecipients" type="java.lang.Boolean"--%>

<c:set var="USER_TYPE_ADMIN" value="<%= BindingEntry.UserType.Admin %>"/>
<c:set var="USER_TYPE_TEST" value="<%= BindingEntry.UserType.TestUser %>"/>
<c:set var="USER_TYPE_NORMAL" value="<%= BindingEntry.UserType.World %>"/>
<c:set var="USER_TYPE_TEST_VIP" value="<%= BindingEntry.UserType.TestVIP %>"/>
<c:set var="USER_TYPE_NORMAL_VIP" value="<%= BindingEntry.UserType.WorldVIP %>"/>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>

<mvc:form servletRelativeAction="/recipient/list.action"
          cssClass="form-vertical"
          id="recipientForm"
          modelAttribute="listForm"
          data-form="resource"
          data-action="search-recipient"
          data-controller="recipient-list"
          data-validator-options="ignore_qb_validation: true, skip_empty: true">

    <div data-form-content="">
        <input type="hidden" name="page" value="${recipientList.pageNumber}"/>
        <input type="hidden" name="sort" value="${recipientList.sortCriterion}"/>
        <input type="hidden" name="dir" value="${recipientList.sortDirection}"/>

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "recipient-overview": {
                    "rows-count": ${listForm.numberOfRows},
                    "fields": ${emm:toJson(listForm.selectedFields)}
                }
            }
        </script>

        <div class="tile" data-initializer="recipient-list">
            <script id="config:recipient-list" type="application/json">
                {
                    "initialRules": ${emm:toJson(listForm.searchQueryBuilderRules)}
                }
            </script>
            <div class="tile-header">
                <h2 class="headline">
                    <mvc:message code="default.search"/>
                </h2>
                <ul class="tile-header-actions">
                    <%--Basic search tab --%>
                    <li class="tab" id="basicSearch">
                        <a href="#" data-toggle-tab="#tab-basicSearch" data-toggle-tab-method="toggle"
                           data-sync-from="#search_mailinglist_advanced, #search_altg_advanced, #search_targetgroup_advanced, #search_recipient_type_advanced, #search_recipient_state_advanced"
                           data-sync-to="#search_mailinglist, #search_altg, #search_targetgroup, #search_recipient_type, #search_recipient_state" data-action="chose-basic-search">
                            <i class="icon icon-search"></i>
                            <mvc:message code="recipient.search.base"/>
                            <i class="icon tab-toggle icon-angle-down"></i>
                        </a>
                    </li>

                    <%--Advanced search tab --%>
                    <li class="tab" id="advancedSearch">
                        <a href="#" data-toggle-tab="#tab-advancedSearch" data-toggle-tab-method="toggle"
                           data-sync-from="#search_mailinglist, #search_altg, #search_targetgroup, #search_recipient_type, #search_recipient_state"
                           data-sync-to="#search_mailinglist_advanced, #search_altg_advanced, #search_targetgroup_advanced, #search_recipient_type_advanced, #search_recipient_state_advanced"
                           data-action="choose-advanced-search">
                            <i class="icon icon-search"></i>
                            <mvc:message code="recipient.AdvancedSearch"/>
                            <i class="icon tab-toggle icon-angle-down"></i>
                        </a>
                    </li>

                    <c:set var="showDuplicateTab" value="false"/>
                    <%@include file="/WEB-INF/jsp/recipient/duplicate-tab-link.jspf"%>

                    <%--Show dropdown--%>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-eye"></i>
                            <span class="text"><mvc:message code="button.Show"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><mvc:message code="listSize"/></li>

                            <li>
                                <label class="label">
                                    <mvc:radiobutton path="numberOfRows" value="20"/>
                                    <span class="label-text">20</span>
                                </label>
                                <label class="label">
                                    <mvc:radiobutton path="numberOfRows" value="50"/>
                                    <span class="label-text">50</span>
                                </label>
                                <label class="label">
                                    <mvc:radiobutton path="numberOfRows" value="100"/>
                                    <span class="label-text">100</span>
                                </label>
                            </li>

                            <li class="divider"></li>
                            <li>
                                <p>
                                    <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                            data-form-submit>
                                        <i class="icon icon-refresh"></i>
                                        <span class="text"><mvc:message code="button.Show"/></span>
                                    </button>
                                </p>
                            </li>
                        </ul>
                    </li>

                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-columns"></i>
                            <span class="text"><mvc:message code="settings.fields"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><mvc:message code="settings.fields"/></li>
                            <li>
                                <p>
                                    <mvc:select path="selectedFields" cssClass="form-control js-select" multiple="multiple">
                                        <c:forEach var="field" items="${fieldsMap}" varStatus="rowCounter">
                                            <c:set var="column" value="${field.key}"/>
                                            <c:set var="fieldName" value="${field.value}"/>

                                            <c:set var="isDefaultField" value="${listForm.isDefaultColumn(column)}"/>
                                            <c:set var="fieldSelected" value="${listForm.isSelectedColumn(column)}"/>

                                            <c:if test="${isDefaultField}">
                                                <option title="${column}" value="${column}" disabled>${fieldName}</option>
                                            </c:if>

                                            <c:if test="${not isDefaultField}">
                                                <option title="${column}" value="${column}" ${fieldSelected ? 'selected' : ''}>${fieldName}</option>
                                            </c:if>
                                        </c:forEach>
                                    </mvc:select>
                                </p>
                            </li>
                            <li>
                                <p>
                                    <button class="btn btn-block btn-secondary btn-regular" type="button"
                                            data-form-change data-form-submit>
                                        <i class="icon icon-refresh"></i>
                                        <span class="text"><mvc:message code="button.Refresh"/></span>
                                    </button>
                                </p>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>

            <div class="tile-content">
                <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-basic-search-tab.jsp"/>
                <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-advanced-search-tab.jsp"/>

                <c:set var="allowedDeletion" value="false"/>
                <emm:ShowByPermission token="recipient.delete">
                    <c:set var="allowedDeletion" value="true"/>
                </emm:ShowByPermission>
                <div class="${deactivatePagination ? 'table-wrapper hide-pagination' : 'table-wrapper'}">
                    <c:choose>
                        <c:when test="${loadRecipients}">
                            <display:table class="table table-bordered table-striped table-hover js-table"
                                           id="recipient"
                                           name="recipientList"
                                           sort="external"
                                           requestURI="/recipient/list.action?loadRecipients=true"
                                           partialList="true"
                                           size="${listForm.numberOfRows}"
                                           excludedParams="*">
                                <c:if test="${recipientList.fullListSize > countOfRecipients}">
                                    <display:caption>
                                        <div class="l-tile-recipient-info-box align-left">
                                            <span> <mvc:message code="recipient.search.max_recipients" arguments="${countOfRecipients}"/></span>
                                        </div>
                                    </display:caption>
                                </c:if>
                                <c:if test="${not empty recipient}">
        
                                    <%@include file="/WEB-INF/jsp/recipient/additional-fields.jspf"%>
        
                                    <display:column headerClass="js-table-sort" titleKey="mailing.MediaType.0"
                                                    sortable="true" sortProperty="email">
                                        <emm:ShowByPermission token="mailing.encrypted.send">
                                            <c:choose>
                                                <c:when test="${recipient.encryptedSend}">
                                                    <img data-tooltip="<mvc:message code="recipient.encrypted.possible"/>" class="icon lock-icon" src="<c:url value="/assets/core/images/lock_icon.svg"/>" alt="">
                                                </c:when>
                                                <c:otherwise>
                                                    <img data-tooltip="<mvc:message code="recipient.encrypted.notpossible"/>" class="icon unlock-icon" src="<c:url value="/assets/core/images/unlock_icon.svg"/>" alt="">
                                                </c:otherwise>
                                            </c:choose>
                                        </emm:ShowByPermission>

                                        ${recipient.email}
                                    </display:column>
        
                                    <display:column class="table-actions" headerClass="${allowedDeletion ? '' : 'hidden'}" sortable="false">
                                        <span class="hidden" data-recipient-id="${recipient.id}"></span>
                                        <emm:ShowByPermission token="recipient.show">
                                            <c:url var="viewLink" value="/recipient/${recipient.id}/view.action"/>
                                            <a href="${viewLink}" class="hidden js-row-show"></a>
                                        </emm:ShowByPermission>
        
                                        <c:if test="${allowedDeletion}">
                                            <c:url var="deletionLink" value="/recipient/${recipient.id}/confirmDelete.action"/>
                                            <mvc:message var="deletionTooltip" code="recipient.RecipientDelete"/>
        
                                            <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deletionTooltip}">
                                                <i class="icon icon-trash-o"></i>
                                            </a>
                                        </c:if>
                                    </display:column>
        
                                </c:if>
                            </display:table>
                        </c:when>
                        <c:otherwise>
                            <div class="table-control">
                                <div class="well align-left">
                                    <span><mvc:message code="info.recipients.choose"/></span>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>
        </div>
    </div>
</mvc:form>
