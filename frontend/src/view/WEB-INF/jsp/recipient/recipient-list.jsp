<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="recipientForm" type="com.agnitas.web.ComRecipientForm"--%>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map"--%>
<%--@elvariable id="hasAnyDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="recipientList" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<c:set var="ACTION_LIST" value="<%= ComRecipientAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= ComRecipientAction.ACTION_VIEW %>"/>
<c:set var="ACTION_CHECK_LIMITACCESS" value="<%= ComRecipientAction.ACTION_CHECK_LIMITACCESS %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ComRecipientAction.ACTION_CONFIRM_DELETE %>"/>

<emm:instantiate var="DEFAULT_FIELDS" type="java.util.LinkedHashMap">
    <c:set target="${DEFAULT_FIELDS}" property="1" value="email" />
</emm:instantiate>

<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>"/>

<c:set var="controllerName" value="recipient-list"/>
<emm:ShowByPermission token="recipient.advanced.search.migration">
    <c:set var="controllerName" value="recipient-list-new"/>
    <c:set var="validatorOptions" value="ignore_qb_validation: true, skip_empty: true"/>
</emm:ShowByPermission>

<agn:agnForm action="/recipient.do" data-form="search" class="form-vertical" id="recipientForm" data-action="search-recipient"
                 data-controller="${controllerName}" data-validator-options="${validatorOptions}">
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="overview" value="true"/>
    <html:hidden property="action"/>
    <html:hidden property="actionList" value="${ACTION_LIST}"/>
    <html:hidden property="needSaveTargetGroup" value="false" styleId="needSaveTargetGroup"/>
    <html:hidden property="targetShortname" styleId="targetShortname"/>
    <html:hidden property="targetDescription" styleId="targetDescription"/>
    <html:hidden property="advancedSearch" value="false"/>

    <div data-form-content="">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "recipient-overview": {
                    "rows-count": ${recipientForm.numberOfRows},
                    "fields": ${emm:toJson(recipientForm.selectedFields)}
                }
            }
        </script>

        <div class="tile" data-initializer="${controllerName}" data-config="">

            <c:url var="limitAccessUrl" value="/recipient.do?action=${ACTION_CHECK_LIMITACCESS}" />
            <c:url var="viewUrl" value="/recipient.do?action=${ACTION_VIEW}" />

            <emm:HideByPermission token="recipient.advanced.search.migration">
                <script id="config:recipient-list" type="application/json">
                    {
                        "CHECK_LIMITACCESS_URL": "${limitAccessUrl}&recipientID={RECIPIENT_ID}",
                        "VIEW_URL": "${viewUrl}&recipientID={RECIPIENT_ID}"
                    }
                </script>
            </emm:HideByPermission>
            <emm:ShowByPermission token="recipient.advanced.search.migration">
                <script id="config:recipient-list-new" type="application/json">
                    {
                        "CHECK_LIMITACCESS_URL": "${limitAccessUrl}&recipientID={RECIPIENT_ID}",
                        "VIEW_URL": "${viewUrl}&recipientID={RECIPIENT_ID}",
                        "initialRules": ${emm:toJson(recipientForm.queryBuilderRules)}
                    }
                </script>
            </emm:ShowByPermission>

            <div class="tile-header">
                <h2 class="headline">
                    <bean:message key="default.search"/>
                </h2>
                <ul class="tile-header-actions">
                    <c:if test="${not param.showDuplicateTab}">
                        <%--Basic search tab --%>
                        <li class="tab" id="basicSearch">
                            <a href="#" data-toggle-tab="#tab-basicSearch" data-toggle-tab-method="toggle">
                                <i class="icon icon-search"></i>
                                <bean:message key="recipient.search.base"/>
                                <i class="icon tab-toggle icon-angle-down"></i>
                            </a>
                        </li>

                        <%--Advanced search tab --%>
                        <li class="tab" id="advancedSearch">
                            <a href="#" data-toggle-tab="#tab-advancedSearch" data-toggle-tab-method="toggle"
                               data-action="choose-advanced-search">
                                <i class="icon icon-search"></i>
                                <bean:message key="recipient.AdvancedSearch"/>
                                <i class="icon tab-toggle icon-angle-down"></i>
                            </a>
                        </li>
                    </c:if>

                    <c:set var="showDuplicateTab" value="false"/>
                    <%@include file="/WEB-INF/jsp/recipient/duplicate-tab-link.jspf"%>

                    <%--Show dropdown--%>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-eye"></i>
                            <span class="text"><bean:message key="button.Show"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><bean:message key="listSize"/></li>
                            <li>
                                <label class="label">
                                    <html:radio property="numberOfRows" value="20"/>
                                    <span class="label-text">20</span>
                                </label>
                                <label class="label">
                                    <html:radio property="numberOfRows" value="50"/>
                                    <span class="label-text">50</span>
                                </label>
                                <label class="label">
                                    <html:radio property="numberOfRows" value="100"/>
                                    <span class="label-text">100</span>
                                </label>
                                <logic:iterate collection="${recipientForm.columnwidthsList}" indexId="i" id="width">
                                    <html:hidden property="columnwidthsList[${i}]"/>
                                </logic:iterate>
                            </li>
                            <li class="divider"></li>
                            <li>
                                <p>
                                    <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                        <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                    </button>
                                </p>
                            </li>
                        </ul>
                    </li>

                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-columns"></i>
                            <span class="text"><bean:message key="settings.fields"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><bean:message key="settings.fields"/></li>
                            <li>
                                <p>
                                    <select class="form-control js-select" tabindex="-1" multiple="multiple" name="selectedFields">
                                        <c:forEach var="field" items="${fieldsMap}" varStatus="rowCounter">
                                            <c:set var="isDefaultField" value="${DEFAULT_FIELDS.values().contains(fn:toLowerCase(field.key))}"/>

                                            <c:if test="${isDefaultField}">
                                                 <option title="${field.key}" value="${field.key}" disabled>
                                                    <c:out value="${field.value}"/>
                                                </option>
                                            </c:if>
                                            <c:set var="fieldSelected" value="${false}"/>
                                            <c:forEach var="selectedField" items="${recipientForm.selectedFields}"
                                                       varStatus="rowCounter">
                                                <c:if test="${field.key == selectedField}">
                                                    <c:set var="fieldSelected" value="${true}"/>
                                                </c:if>
                                            </c:forEach>
                                            <c:if test="${not fieldSelected and not isDefaultField}">
                                                <option title="${field.key}" value="${field.key}">
                                                    <c:out value="${field.value}"/>
                                                </option>
                                            </c:if>
                                            <c:if test="${fieldSelected and not isDefaultField}">
                                                <option title="${field.key}" value="${field.key}" selected>
                                                    <c:out value="${field.value}"/>
                                                </option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </p>
                            </li>
                            <li>
                                <p>
                                    <button class="btn btn-block btn-secondary btn-regular" type="button"
                                            data-form-change data-form-submit>
                                        <i class="icon icon-refresh"></i>
                                        <span class="text"><bean:message key="button.Refresh"/></span>
                                    </button>
                                </p>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>

            <div class="tile-content">
                <emm:ShowByPermission token="recipient.advanced.search.migration">
                    <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-basic-search-tab-new.jsp"/>
                    <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-advanced-search-tab-new.jsp"/>
                </emm:ShowByPermission>

                <emm:HideByPermission token="recipient.advanced.search.migration">
                    <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-basic-search-tab.jsp"/>
                    <jsp:include page="/WEB-INF/jsp/recipient/recipient-list-advanced-search-tab.jsp"/>
                </emm:HideByPermission>

                <div class="${recipientForm.deactivatePagination ? 'table-wrapper hide-pagination' : 'table-wrapper'}">
                    <c:if test="${recipientForm.overview}">
                        <display:table class="table table-bordered table-striped table-hover js-table" id="recipient"
                                       name="recipientList" sort="external"
                                       requestURI="/recipient.do?action=${ACTION_LIST}&__fromdisplaytag=true"
                                       partialList="true" size="${recipientForm.numberOfRows}" excludedParams="*">
                            <c:if test="${recipientList.getFullListSize() > countOfRecipients}">
                                <display:caption>
                                    <div class="l-tile-recipient-info-box align-left">
                                        <span> <bean:message key="recipient.search.max_recipients"
                                                             arg0="${countOfRecipients}"/></span>
                                    </div>
                                </display:caption>
                            </c:if>
                            <c:if test="${not empty recipient}">
                                <c:set var="customer_id"><bean:write name="recipient" property="customer_id"/></c:set>

                                <%@include file="/WEB-INF/jsp/recipient/additional-fields.jspf"%>

                                <display:column headerClass="js-table-sort" property="email"
                                                titleKey="mailing.MediaType.0" sortable="true"/>

                                <c:set var="allowedDeletion" value="false"/>
                                <emm:ShowByPermission token="recipient.delete">
                                    <c:set var="allowedDeletion" value="true"/>
                                </emm:ShowByPermission>

                                <display:column class="table-actions" headerClass="${allowedDeletion ? '' : 'hidden'}" sortable="false">
                                    <span class="hidden" data-recipient-id="${customer_id}"></span>
                                    <emm:ShowByPermission token="recipient.show">
                                        <html:link styleClass="js-row-show" titleKey="recipient.RecipientEdit"
                                                   page="/recipient.do?action=${ACTION_VIEW}&recipientID=${customer_id}"/>
                                    </emm:ShowByPermission>

                                    <c:if test="${allowedDeletion}">
                                        <c:set var="recipientDeleteMessage" scope="page">
                                            <bean:message key="recipient.RecipientDelete"/>
                                        </c:set>
                                        <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete"
                                                     data-tooltip="${recipientDeleteMessage}"
                                                     page="/recipient.do?action=${ACTION_CONFIRM_DELETE}&recipientID=${customer_id}&fromListPage=true">
                                            <i class="icon icon-trash-o"></i>
                                        </agn:agnLink>
                                    </c:if>
                                </display:column>
                            </c:if>
                        </display:table>
                    </c:if>
                </div>

            </div>
        </div>
    </div>

    <%@include file="/WEB-INF/jsp/recipient/recipient-list-target-group-save-template.jspf" %>
</agn:agnForm>

