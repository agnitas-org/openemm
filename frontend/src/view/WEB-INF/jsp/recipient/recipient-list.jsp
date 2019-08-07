<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="ajax" uri="http://ajaxtags.org/tags/ajax" %>
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
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ComRecipientAction.ACTION_CONFIRM_DELETE %>"/>

<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>"/>

<agn:agnForm action="/recipient.do" data-form="search" class="form-vertical" id="recipientForm"
             data-controller="recipient-list">
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="advancedSearchVisible"/>
    <html:hidden property="recipientFieldsVisible"/>
    <html:hidden property="saveTargetVisible"/>
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

        <div class="tile" data-initializer="recipient-list">
            <div class="tile-header">
                <h2 class="headline"><bean:message key="default.search"/></h2>
                <ul class="tile-header-actions">
                    <li class="tab" id="basicSearch">
                        <a href="#" data-toggle-tab="#tab-basicSearch" data-toggle-tab-method="toggle">
                            <i class="icon icon-search"></i>
                            <bean:message key="recipient.search.base"/>
                            <i class="icon tab-toggle icon-angle-down"></i>
                        </a>
                    </li>

                    <li class="tab" id="advancedSearch">
                        <a href="#" data-toggle-tab="#tab-advancedSearch" data-toggle-tab-method="toggle"
                           data-action="choose-advanced-search">
                            <i class="icon icon-search"></i>
                            <bean:message key="recipient.AdvancedSearch"/>
                            <i class="icon tab-toggle icon-angle-down"></i>
                        </a>
                    </li>

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
                                    <button class="btn btn-block btn-secondary btn-regular" data-form-change
                                            data-form-submit type="button">
                                        <i class="icon icon-refresh"></i><span class="text"><bean:message
                                            key="button.Show"/></span>
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
                                            <c:if test="${field.key=='email' or field.key=='EMAIL'}">
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
                                            <c:if test="${!fieldSelected and field.key!='email' and field.key!='EMAIL'}">
                                                <option title="${field.key}" value="${field.key}">
                                                    <c:out value="${field.value}"/>
                                                </option>
                                            </c:if>
                                            <c:if test="${fieldSelected and field.key!='email' and field.key!='EMAIL'}">
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
                                        <i class="icon icon-refresh"></i><span class="text"><bean:message
                                            key="button.Refresh"/></span>
                                    </button>
                                </p>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content">
                <div id="tab-basicSearch" class="hidden">
                    <div class="tile-content-forms" style="padding-bottom: 0">
                        <div class="row">
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_mailinglist">
                                            <bean:message key="Mailinglist"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_mailinglist" styleClass="form-control js-select"
                                                       data-action="change-fields-to-search" property="listID"
                                                       data-form-change="0" titleKey="default.All">
                                            <html:option value="0" key="default.All"/>
                                            <c:if test="${hasAnyDisabledMailingLists == false}">
                                                <agn:agnOption value="-1"><bean:message key="No_Mailinglist"/></agn:agnOption>
                                            </c:if>
                                            <c:forEach var="mailinglist" items="${mailinglists}">
                                                <html:option
                                                        value="${mailinglist.id}">${mailinglist.shortname}</html:option>
                                            </c:forEach>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_targetgroup">
                                            <bean:message key="Target"/>
                                        </label>
									</div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_targetgroup" styleClass="form-control js-select"
                                                       data-action="change-target-group" property="targetID"
                                                       data-form-change="0" titleKey="default.All">
                                            <html:option value="0" key="default.All"/>
                                            <c:forEach var="target" items="${targets}">
                                                <html:option value="${target.id}">
                                                    ${target.targetName}
                                                </html:option>
                                            </c:forEach>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_recipient_type">
                                            <bean:message key="recipient.RecipientType"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_recipient_type"
                                                       styleClass="form-control js-select"
                                                       data-action="change-recipient-type" property="user_type"
                                                       data-form-change="0" titleKey="default.All"
                                                       disabled="${recipientForm.listID == -1}">
                                            <html:option value="" key="default.All"/>
                                            <html:option value="A" key="recipient.Administrator"/>
                                            <html:option value="T" key="TestSubscriber"/>
                                            <%@include file="recipient-novip-test.jspf" %>
                                            <html:option value="W" key="NormalSubscriber"/>
                                            <%@include file="recipient-novip-normal.jspf" %>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_recipient_state">
                                            <bean:message key="recipient.RecipientStatus"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_recipient_state" styleClass="form-control"
                                                       property="user_status" data-action="change-user-status"
                                                       data-form-change="0" titleKey="default.All"
                                                       disabled="${recipientForm.listID == -1}">
                                            <html:option value="0" key="default.All"/>
                                            <html:option value="1" key="recipient.MailingState1"/>
                                            <html:option value="2" key="recipient.MailingState2"/>
                                            <html:option value="3" key="recipient.OptOutAdmin"/>
                                            <html:option value="4" key="recipient.OptOutUser"/>
                                            <html:option value="5" key="recipient.MailingState5"/>
                                            <emm:ShowByPermission token="blacklist">
                                                <html:option value="6" key="recipient.MailingState6"/>
                                            </emm:ShowByPermission>
                                            <html:option value="7" key="recipient.MailingState7"/>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label for="search_first_name" class="control-label">
                                            <bean:message key="Firstname"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <html:text styleId="search_first_name" styleClass="form-control"
                                                   property="searchFirstName"/>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label for="search_name" class="control-label">
                                            <bean:message key="Lastname"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <html:text styleId="search_name" styleClass="form-control"
                                                   property="searchLastName"/>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label for="search_email" class="control-label">
                                            <bean:message key="mailing.MediaType.0"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <html:text styleId="search_email" styleClass="form-control"
                                                   property="searchEmail"/>
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
                                            <bean:message key="help"/>
                                        </button>

                                        <div class="btn-group pull-right">
                                            <button type="button" class="btn btn-regular"
                                                    data-form-set="resetSearch:true" data-action="reset-search">
                                                <bean:message key="button.search.reset"/></button>
                                            <button class="btn btn-primary btn-regular pull-right" type="button"
                                                    data-form-submit data-form-persist="page: '1'">
                                                <i class="icon icon-search"></i>
                                                <span class="text"><bean:message key="Search"/></span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                </div>
                <div id="tab-advancedSearch" class="hidden">
                    <div class="tile-content-forms" style="padding-bottom: 0">
                        <div class="row">
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_mailinglist_advanced">
                                            <bean:message key="Mailinglist"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_mailinglist_advanced"
                                                       styleClass="form-control js-select"
                                                       data-action="change-fields-to-search-advanced" property="listID"
                                                       data-form-change="0" titleKey="default.All">
                                            <html:option value="0" key="default.All"/>
                                            <c:if test="${hasAnyDisabledMailingLists == false}">
                                                <agn:agnOption value="-1"><bean:message key="default.none"/></agn:agnOption>
                                            </c:if>
                                            <c:forEach var="mailinglist" items="${mailinglists}">
                                                <html:option
                                                        value="${mailinglist.id}">${mailinglist.shortname}</html:option>
                                            </c:forEach>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_targetgroup_advanced">
                                            <bean:message key="Target"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_targetgroup_advanced"
                                                       styleClass="form-control js-select" property="targetID"
                                                       data-action="change-target-group-advanced" data-form-change="0"
                                                       titleKey="default.All">
                                            <html:option value="0" key="default.All"/>
                                            <c:forEach var="target" items="${targets}">
                                                <html:option value="${target.id}">
                                                    ${target.targetName}
                                                </html:option>
                                            </c:forEach>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_recipient_type_advanced">
                                            <bean:message key="recipient.RecipientType"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_recipient_type_advanced"
                                                       styleClass="form-control js-select" property="user_type"
                                                       data-action="change-recipient-type-advanced" data-form-change="0"
                                                       titleKey="default.All"
                                                       disabled="${recipientForm.listID == -1}">
                                            <html:option value="" key="default.All"/>
                                            <html:option value="A" key="recipient.Administrator"/>
                                            <html:option value="T" key="TestSubscriber"/>
                                            <%@include file="recipient-novip-test.jspf" %>
                                            <html:option value="W" key="NormalSubscriber"/>
                                            <%@include file="recipient-novip-normal.jspf" %>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label" for="search_recipient_state_advanced">
                                            <bean:message key="recipient.RecipientStatus"/>
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <agn:agnSelect styleId="search_recipient_state_advanced"
                                                       styleClass="form-control" property="user_status"
                                                       data-action="change-user-status-advanced" data-form-change="0"
                                                       titleKey="default.All"
                                                       disabled="${recipientForm.listID == -1}">
                                            <html:option value="0" key="default.All"/>
                                            <html:option value="1" key="recipient.MailingState1"/>
                                            <html:option value="2" key="recipient.MailingState2"/>
                                            <html:option value="3" key="recipient.OptOutAdmin"/>
                                            <html:option value="4" key="recipient.OptOutUser"/>
                                            <html:option value="5" key="recipient.MailingState5"/>
                                            <emm:ShowByPermission token="blacklist">
                                                <html:option value="6" key="recipient.MailingState6"/>
                                            </emm:ShowByPermission>
                                            <html:option value="7" key="recipient.MailingState7"/>
                                        </agn:agnSelect>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label">
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        <c:set var="FORM_NAME" value="recipientForm" scope="page"/>
                                        <c:set var="HIDE_SPECIAL_TARGET_FEATURES" value="true" scope="page"/>
                                        <%@include file="/WEB-INF/jsp/rules/rule_add.jsp" %>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label">
                                        </label>
                                    </div>
                                    <div class="col-md-12">
                                        </c:if>
                                        <%@include file="/WEB-INF/jsp/rules/rules_list.jsp" %>
                                        <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
                                    </div>
                                </div>
                            </div>
                        </div>
                        </c:if>

                        <div class="row">
                            <div class="col-sm-12">
                                <div class="form-group">
                                    <div class="col-sm-12">
                                        <button type="button" tabindex="-1" class="btn btn-regular"
                                                data-help="help_${helplanguage}/recipient/AdvancedSearchMsg.xml">
                                            <i class="icon icon-question-circle"></i>
                                            <bean:message key="help"/>
                                        </button>

                                        <div class="btn-group pull-right">
                                            <button type="button" class="btn btn-regular"
                                                    data-form-set="resetSearch:true" data-action="reset-search">
                                                <bean:message key="button.search.reset"/></button>
                                            <!-- hide if no queries are present BEGIN -->
                                            <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
                                                <button type="button" tabindex="-1" data-modal="target-group-save"
                                                        class="btn btn-regular">
                                                    <bean:message key="recipient.saveSearch"/>
                                                </button>
                                            </c:if>
                                            <!-- hide if no queries are present END -->

                                            <button id="refresh-button"
                                                    class="btn btn-primary btn-regular" type="button"
                                                    data-form-set="advancedSearch:true"
                                                    data-form-submit>
                                                <i class="icon icon-refresh"></i>
                                                <span class="text"><bean:message key="button.Refresh"/></span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                </div>


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

                                <c:forEach var="selectedField" items="${recipientForm.selectedFields}"
                                           varStatus="rowCounter">
                                    <c:forEach var="field" items="${fieldsMap}" varStatus="rowCounter">

                                        <c:if test="${field.key == selectedField}">
                                            <c:choose>
                                                <c:when test="${field.key == 'gender' or field.key == 'GENDER'}">
                                                    <c:set var="gender"><bean:write name="recipient" property="gender"/></c:set>
                                                    <display:column class="recipient_title" headerClass="js-table-sort"
                                                                    titleKey="recipient.Salutation" sortable="true"
                                                                    sortProperty="gender" paramId="recipientID"
                                                                    paramProperty="customer_id"
                                                                    url="/recipient.do?action=${ACTION_VIEW}">
                                                        <bean:message key="recipient.gender.${gender}.short"/>
                                                    </display:column>
                                                </c:when>
                                                <c:when test="${field.key == 'firstname' or field.key == 'FIRSTNAME'}">
                                                    <display:column class="recipient_firstname"
                                                                    headerClass="js-table-sort" titleKey="Firstname"
                                                                    sortable="true" sortProperty="firstname"
                                                                    paramId="recipientID" paramProperty="customer_id"
                                                                    url="/recipient.do?action=${ACTION_VIEW}">
                                                        <c:set var="firstname"><bean:write name="recipient"
                                                                                           property="firstname"/></c:set>
                                                        ${firstname}
                                                    </display:column>
                                                </c:when>
                                                <c:when test="${field.key == 'lastname' or field.key == 'LASTNAME'}">
                                                    <display:column class="recipient_lastname"
                                                                    headerClass="js-table-sort" titleKey="Lastname"
                                                                    sortable="true" sortProperty="lastname"
                                                                    paramId="recipientID" paramProperty="customer_id"
                                                                    url="/recipient.do?action=${ACTION_VIEW}">
                                                        <c:set var="lastname"><bean:write name="recipient"
                                                                                          property="lastname"/></c:set>
                                                        ${lastname}
                                                    </display:column>
                                                </c:when>

                                                <c:when test="${field.key == 'creation_date' or field.key == 'CREATION_DATE'}">
                                                    <c:set var="creation_date"><bean:write name="recipient"
                                                                                           property="creation_date"/></c:set>

                                                    <display:column headerClass="js-table-sort" sortable="true"
                                                                    titleKey="default.creationDate"
                                                                    sortProperty="creation_date" paramId="recipientID"
                                                                    paramProperty="customer_id"
                                                                    url="/recipient.do?action=${ACTION_VIEW}">
                                                        <fmt:parseDate value="${creation_date}" type="date"
                                                                       pattern="yyyy-MM-dd HH:mm:ss.S"
                                                                       var="formatedDate"/>
                                                        <fmt:formatDate value="${formatedDate}" type="date"
                                                                        pattern="${adminDateFormat}"
                                                                        timeZone="${adminTimeZone}"/>
                                                    </display:column>
                                                </c:when>
                                                <c:otherwise>
                                                    <display:column class="recipient_title" headerClass="js-table-sort"
                                                                    title="${field.value}" property="${field.key}"
                                                                    sortable="true" sortProperty="${field.key}"
                                                                    paramId="recipientID" paramProperty="customer_id"
                                                                    url="/recipient.do?action=${ACTION_VIEW}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </c:forEach>
                                </c:forEach>

                                <display:column headerClass="js-table-sort" property="email"
                                                titleKey="mailing.MediaType.0" sortable="true" paramId="recipientID"
                                                paramProperty="customer_id" url="/recipient.do?action=${ACTION_VIEW}"/>

                                <display:column class="table-actions">
                                    <emm:ShowByPermission token="recipient.show">
                                        <html:link styleClass="js-row-show hidden" titleKey="recipient.RecipientEdit"
                                                   page="/recipient.do?action=${ACTION_VIEW}&recipientID=${customer_id}"/>
                                    </emm:ShowByPermission>
                                    <emm:ShowByPermission token="recipient.delete">
                                        <c:set var="recipientDeleteMessage" scope="page">
                                            <bean:message key="recipient.RecipientDelete"/>
                                        </c:set>
                                        <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                                     data-tooltip="${recipientDeleteMessage}"
                                                     page="/recipient.do?action=${ACTION_CONFIRM_DELETE}&recipientID=${customer_id}&fromListPage=true">
                                            <i class="icon icon-trash-o"></i>
                                        </agn:agnLink>

                                    </emm:ShowByPermission>
                                </display:column>
                            </c:if>
                        </display:table>
                    </c:if>
                </div>

            </div>
        </div>

    </div>

    <script id="target-group-save" type="text/x-mustache-template">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close" data-dismiss="modal">
                            <i aria-hidden="true" class="icon icon-times-circle"></i>
                        </button>
                        <h4 class="modal-title"><bean:message key="recipient.saveSearch"/></h4>
                    </div>
                    <div class="modal-body">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="targetShortnameOverlay" class="control-label"><bean:message
                                        key="Name"/></label>
                            </div>
                            <div class="col-sm-8">
                                <input type="text" id="targetShortnameOverlay" class="form-control" maxlength="99"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="targetDescriptionOverlay" class="control-label"><bean:message
                                        key="Description"/></label>
                            </div>
                            <div class="col-sm-8">
                                <textarea id="targetDescriptionOverlay" class="form-control" rows="5"
                                          cols="32"></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><bean:message key="button.Cancel"/></span>
                            </button>
                            <button type="button" class="btn btn-primary btn-large"
                                    data-sync-from="#targetShortnameOverlay, #targetDescriptionOverlay"
                                    data-sync-to="#targetShortname, #targetDescription"
                                    data-form-set="needSaveTargetGroup: 'true'" data-form-target="#recipientForm"
                                    data-form-submit="" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <bean:message key="button.Save"/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>
</agn:agnForm>
