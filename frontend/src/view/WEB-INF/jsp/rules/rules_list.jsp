<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ page import="org.agnitas.target.ConditionalOperator" %>
<%@ page import="org.agnitas.target.PseudoColumn" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.agnitas.target.ColumnType" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="_colsel_column_name" type="java.lang.String"--%>
<%--@elvariable id="_colsel_shortname" type="java.lang.String"--%>
<%--@elvariable id="_colsel_data_type" type="java.lang.String"--%>
<%--@elvariable id="_colsel_shortname" type="java.lang.String"--%>
<%--@elvariable id="interval_mailings" type="java.util.List"--%>
<%--@elvariable id="all_mailings" type="java.util.List"--%>
<%--@elvariable id="all_mailings_urls" type="java.util.List"--%>

<c:set var="MAILTYPE_TEXT" value="<%=Recipient.MAILTYPE_TEXT%>" scope="page"/>
<c:set var="MAILTYPE_HTML" value="<%=Recipient.MAILTYPE_HTML%>" scope="page"/>
<c:set var="MAILTYPE_HTML_OFFLINE" value="<%=Recipient.MAILTYPE_HTML_OFFLINE%>" scope="page"/>

<c:set var="COLUMN_TYPE_DATE" value="<%= ColumnType.COLUMN_TYPE_DATE %>" scope="page" />
<c:set var="COLUMN_TYPE_NUMERIC" value="<%= ColumnType.COLUMN_TYPE_NUMERIC %>" scope="page" />
<c:set var="COLUMN_TYPE_INTERVAL_MAILING" value="<%= ColumnType.COLUMN_TYPE_INTERVAL_MAILING %>" scope="page" />
<c:set var="COLUMN_TYPE_STRING" value="<%= ColumnType.COLUMN_TYPE_STRING %>" scope="page" />
<c:set var="COLUMN_TYPE_MAILING_RECEIVED" value="<%= ColumnType.COLUMN_TYPE_MAILING_RECEIVED %>" scope="page" />
<c:set var="COLUMN_TYPE_MAILING_OPENED" value="<%= ColumnType.COLUMN_TYPE_MAILING_OPENED %>" scope="page" />
<c:set var="COLUMN_TYPE_MAILING_CLICKED" value="<%= ColumnType.COLUMN_TYPE_MAILING_CLICKED %>" scope="page" />
<c:set var="COLUMN_TYPE_MAILING_REVENUE" value="<%= ColumnType.COLUMN_TYPE_MAILING_REVENUE %>" scope="page" />
<c:set var="COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK" value="<%= ColumnType.COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK %>" scope="page" />


<c:set var="DATE_OPERATORS" value="<%= ConditionalOperator.getValidOperatorsForDate() %>" />
<c:set var="STRING_OPERATORS" value="<%= ConditionalOperator.getValidOperatorsForString() %>" />
<c:set var="NUMERIC_OPERATORS" value="<%= ConditionalOperator.getValidOperatorsForNumber() %>" />
<c:set var="MOD_SECONDARY_OPERATORS" value="<%= ConditionalOperator.getSecondaryOperatorsForMod() %>" />
<c:set var="MAILING_OPERATORS" value="<%=ConditionalOperator.getValidOperatorsForMailingOperators()%>" />
<c:set var="INTERVAL_MAILING_OPERATORS" value="<%=ConditionalOperator.getValidOperatorsForMailingOperators()%>" />

<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>" scope="page" />
<c:set var="OPERATOR_MOD" value="<%= ConditionalOperator.MOD.getOperatorCode() %>" scope="page" />

<c:set var="COLUMN_INTERVAL_MAILING" value="<%= PseudoColumn.INTERVAL_MAILING %>" scope="page" />
<c:set var="COLUMN_MAILING_OPENED" value="<%= PseudoColumn.OPENED_MAILING %>" scope="page" />
<c:set var="COLUMN_MAILING_CLICKED" value="<%= PseudoColumn.CLICKED_IN_MAILING %>" scope="page" />
<c:set var="COLUMN_MAILING_RECEIVED" value="<%= PseudoColumn.RECEIVED_MAILING %>" scope="page" />
<c:set var="COLUMN_MAILING_CLICKED_ON_SPECIFIC_LINK" value="<%= PseudoColumn.CLICKED_SPECIFIC_LINK_IN_MAILING %>" scope="page" />

<c:if test="${empty TARGET_LOCKED}">
	<c:set var="TARGET_LOCKED" value="false" scope="page" />
</c:if>

<bean:define id="allColumnsAndTypes" name="${FORM_NAME}" property="allColumnsAndTypes" toScope="page" />

<jsp:useBean id="columnNameToShortNameMap" class="java.util.HashMap" scope="page"/>

<c:set var="RULES_SERVICE_CONFIG">
    OPERATOR_IS: ${OPERATOR_IS},
    OPERATOR_MOD: ${OPERATOR_MOD},
    URL_GET_MAILING_LINKS: '<c:url value="/GetMailingTrackableLinks.do"/>'
</c:set>

<div class="hidden" data-initializer="rules-config" data-config="${RULES_SERVICE_CONFIG}"></div>
<div class="hidden">
    <select name="all_columns_config">
        <emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>">
            <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(_colsel_column_name)}" value="${_colsel_shortname}"/>
            <option value="${fn:toUpperCase(_colsel_column_name)}" data-extra="${_colsel_data_type}">${_colsel_shortname}</option>
        </emm:ShowColumnInfo>

        <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(COLUMN_INTERVAL_MAILING)}"><bean:message key="receivedIntervalMailing"/></c:set>
        <option value="${fn:toUpperCase(COLUMN_INTERVAL_MAILING)}" data-extra="INTERVAL_MAILING">
            <bean:message key="receivedIntervalMailing"/>
        </option>

        <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(COLUMN_MAILING_OPENED)}"><bean:message key="target.rule.mailingOpened"/></c:set>
        <option value="${fn:toUpperCase(COLUMN_MAILING_OPENED)}" data-extra="MAILING">
            <bean:message key="target.rule.mailingOpened"/>
        </option>

        <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(COLUMN_MAILING_CLICKED)}"><bean:message key="target.rule.mailingClicked"/></c:set>
        <option value="${fn:toUpperCase(COLUMN_MAILING_CLICKED)}" data-extra="MAILING">
            <bean:message key="target.rule.mailingClicked"/>
        </option>

        <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(COLUMN_MAILING_RECEIVED)}"><bean:message key="target.rule.mailingReceived"/></c:set>
        <option value="${fn:toUpperCase(COLUMN_MAILING_RECEIVED)}" data-extra="MAILING">
            <bean:message key="target.rule.mailingReceived"/>
        </option>

        <c:set target="${columnNameToShortNameMap}" property="${fn:toUpperCase(COLUMN_MAILING_CLICKED_ON_SPECIFIC_LINK)}"><bean:message key="target.rule.mailingClickedSpecificLink"/></c:set>
        <option value="${fn:toUpperCase(COLUMN_MAILING_CLICKED_ON_SPECIFIC_LINK)}" data-extra="MAILING_LINKS">
            <bean:message key="target.rule.mailingClickedSpecificLink"/>
        </option>

        <c:set target="${columnNameToShortNameMap}" property="CURRENT_TIMESTAMP"><bean:message key="default.sysdate"/></c:set>
        <option value="CURRENT_TIMESTAMP" data-extra="DATE">
            <bean:message key="default.sysdate"/>
        </option>
    </select>

    <select name="interval_mailings_config">
        <c:if test="${not empty interval_mailings}">
            <logic:iterate name="interval_mailings" id="light_mailing">
                <option value="${light_mailing.mailingID}">${light_mailing.shortname}</option>
            </logic:iterate>
        </c:if>
    </select>

    <select name="mod_secondary_operators_config">
        <c:forEach items="${MOD_SECONDARY_OPERATORS}" var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="all_mailings_config">
        <c:if test="${not empty all_mailings}">
            <logic:iterate name="all_mailings" id="light_mailing">
                <option value="${light_mailing.mailingID}">${light_mailing.shortname}</option>
            </logic:iterate>
        </c:if>
    </select>

    <select name="all_mailings_urls_config">
        <c:forEach items="${all_mailings_urls}" var="entry">
            <optgroup label="${entry.key}">
            <c:forEach items="${entry.value}" var="url">
                <option value="${url.id}">${url.fullUrl}</option>
            </c:forEach>
        </c:forEach>
    </select>

    <select name="date_operators_config">
        <c:forEach items="${DATE_OPERATORS}" var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="mailing_operators_config">
        <c:forEach items="${MAILING_OPERATORS}"  var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="interval_mailing_operators_config">
        <c:forEach items="${INTERVAL_MAILING_OPERATORS}"  var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="numeric_operators_config">
        <c:forEach items="${NUMERIC_OPERATORS}"  var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="string_operators_config">
        <c:forEach items="${STRING_OPERATORS}"  var="operator">
            <c:if test="${not empty operator}">
                <option value="${operator.operatorCode}"><bean:message key='target.operator.${operator.operatorKey}' /></option>
            </c:if>
        </c:forEach>
    </select>

    <select name="mail_types_config">
        <option value="${MAILTYPE_TEXT}"><bean:message key='recipient.mailingtype.text' /></option>
        <option value="${MAILTYPE_HTML}"><bean:message key='HTML' /></option>
        <option value="${MAILTYPE_HTML_OFFLINE}"><bean:message key='recipient.mailingtype.htmloffline' /></option>
    </select>

    <select name="genders_config">
        <option value="0"><bean:message key='recipient.gender.0.short' /></option>
        <option value="1"><bean:message key='recipient.gender.1.short' /></option>
        <option value="2"><bean:message key='recipient.gender.2.short' /></option>
    </select>

    <select name="date_formats_config">
        <option value="yyyymmdd"><bean:message key='default.date.format.YYYYMMDD' /></option>
        <option value="mmdd"><bean:message key='default.date.format.MMDD' /></option>
        <option value="dd"><bean:message key='default.date.format.DD' /></option>
        <option value="mm"><bean:message key='default.date.format.MM' /></option>
        <option value="yyyy"><bean:message key='default.date.format.YYYY' /></option>
    </select>
</div>

<div data-controller="rules-list" id="rulesList">
    <c:if test="${fn:length(allColumnsAndTypes) > 0}">
    <div class="table-responsive" data-controller="rules-list">

        <table class="table table-bordered table-striped table-form">


            <logic:iterate id="currentColumnAndType" name="${FORM_NAME}" property="allColumnsAndTypes" indexId="index">
                <c:if test="${not empty currentColumnAndType}">

                    <c:set var="showErrorMessage" value="false"/>
                    <logic:messagesPresent name="rulesValidationErrors" property="targetrule.${index}.errors">
                        <c:set var="showErrorMessage" value="true"/>
                    </logic:messagesPresent>

                    <tr class="${showErrorMessage ? "has-error-row" : ""}">

                        <bean:define id="columnType" name="${FORM_NAME}" property="columnType[${index}]" toScope="page" />
                        <bean:define id="primaryOperator" name="${FORM_NAME}" property="primaryOperator[${index}]" toScope="page" />
                        <bean:define id="columnName" name="${FORM_NAME}" property="columnName[${index}]" toScope="page" />

                        <c:choose>
                            <c:when test="${index != 0}">
                                <td>
                                    <html:select styleClass="form-control" name="${FORM_NAME}" property="chainOperator[${index}]" size="1" disabled="${TARGET_LOCKED}">
                                        <html:option value="<%= Integer.toString(ChainOperator.AND.getOperatorCode()) %>" key="default.and" />
                                        <html:option value="<%= Integer.toString(ChainOperator.OR.getOperatorCode()) %>" key="default.or" />
                                    </html:select>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td>
                                    <html:hidden name="${FORM_NAME}" property="chainOperator[${index}]" value="<%= Integer.toString(ChainOperator.NONE.getOperatorCode()) %>" />
                                </td>
                            </c:otherwise>
                        </c:choose>

                        <td>
                            <html:select styleClass="form-control" name="${FORM_NAME}" property="parenthesisOpened[${index}]" size="1" disabled="${TARGET_LOCKED}" >
                                <html:option value="0">&nbsp</html:option>
                                <html:option value="1">(</html:option>
                            </html:select>
                        </td>
                        <td>
                            <%-- Extract column name from name#type --%>
                            <c:choose>
                                <c:when test="${not empty currentColumnAndType}">
                                    <c:set var="selectedColumnName" value="${fn:toUpperCase(currentColumnAndType)}"/>
                                    <c:if test="${fn:contains(selectedColumnName, '#')}">
                                        <c:set var="selectedColumnName" value="${fn:substring(selectedColumnName, 0, fn:indexOf(selectedColumnName, '#'))}"/>
                                    </c:if>

                                    <%-- Support legacy column name --%>
                                    <c:if test="${selectedColumnName eq 'SYSDATE'}">
                                        <c:set var="selectedColumnName" value="CURRENT_TIMESTAMP"/>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="selectedColumnName" value=""/>
                                </c:otherwise>
                            </c:choose>

                            <agn:agnSelect property="columnAndType[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}"
                                           data-selected-column="${selectedColumnName}" data-ruleid="${index}" data-action="columnAndType">
                                <%-- All the available options are going to be lazily loaded. Until that moment the only [selected] option is there --%>
                                <c:if test="${not empty selectedColumnName}">
                                    <option value="${selectedColumnName}" selected>${columnNameToShortNameMap[selectedColumnName]}</option>
                                </c:if>
                            </agn:agnSelect>
                        </td>
                        <td>
                            <agn:agnSelect name="${FORM_NAME}" property="primaryOperator[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}" data-ruleid="${index}" data-action="primaryOperator">
                                <logic:iterate name="${FORM_NAME}" property="validTargetOperators[${index}]" id="operator">
                                    <c:if test="${not empty operator}">
                                        <html:option value="${operator.operatorCode}"><bean:message key="target.operator.${operator.operatorKey}" /></html:option>
                                    </c:if>
                                </logic:iterate>
                            </agn:agnSelect>
                        </td>

                        <c:choose>
                            <c:when test="${columnType == COLUMN_TYPE_DATE && primaryOperator != OPERATOR_IS}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <html:text name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control" readonly="${TARGET_LOCKED}" />
                                        </div>
                                        <div class="col-sm-6">
                                            <html:select name="${FORM_NAME}" property="dateFormat[${index}]" styleClass="form-control" size="1" disabled="${TARGET_LOCKED}">
                                                <html:option value="yyyymmdd" key="default.date.format.YYYYMMDD" />
                                                <html:option value="mmdd" key="default.date.format.MMDD" />
                                                <html:option value="yyyymm" key="default.date.format.YYYYMM" />
                                                <html:option value="dd" key="default.date.format.DD" />
                                                <html:option value="mm" key="default.date.format.MM" />
                                                <html:option value="yyyy" key="default.date.format.YYYY" />
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_NUMERIC}">
                                <c:choose>
                                    <c:when test="${primaryOperator == OPERATOR_IS}">
                                        <td>
                                            <div class="row">
                                                <div class="col-sm-12">
                                                    <html:select name="${FORM_NAME}" property="primaryValue[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}">
                                                        <html:option value="null">null</html:option>
                                                        <html:option value="not null">not null</html:option>
                                                    </html:select>
                                                </div>
                                            </div>
                                        </td>
                                    </c:when>
                                    <c:when test="${primaryOperator != OPERATOR_MOD}">
                                        <c:choose>
                                            <c:when test="${fn:toLowerCase(columnName) == 'mailtype'}">
                                                <td>
                                                    <div class="row">
                                                        <div class="col-sm-12">
                                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}">
                                                                <html:option value="${MAILTYPE_TEXT}" key="recipient.mailingtype.text" />
                                                                <html:option value="${MAILTYPE_HTML}" key="HTML" />
                                                                <html:option value="${MAILTYPE_HTML_OFFLINE}" key="recipient.mailingtype.htmloffline" />
                                                            </html:select>
                                                        </div>
                                                    </div>
                                                </td>
                                            </c:when>
                                            <c:when test="${fn:toLowerCase(columnName) == 'gender'}">
                                                <td>
                                                    <div class="row">
                                                        <div class="col-sm-12">
                                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}">
                                                                <html:option value="0" key="recipient.gender.0.short" />
                                                                <html:option value="1" key="recipient.gender.1.short" />
                                                                <emm:ShowByPermission token="recipient.gender.extended">
                                                                    <html:option value="4" key="recipient.gender.4.short" />
                                                                    <html:option value="5" key="recipient.gender.5.short" />
                                                                </emm:ShowByPermission>
                                                                <html:option value="2" key="recipient.gender.2.short" />
                                                            </html:select>
                                                        </div>
                                                    </div>
                                                </td>
                                            </c:when>
                                            <c:otherwise>
                                                <td>
                                                    <div class="row">
                                                        <div class="col-sm-12">
                                                            <html:text name="${FORM_NAME}" property="primaryValue[${index}]" size="60" styleClass="form-control" readonly="${TARGET_LOCKED}"/>
                                                        </div>
                                                    </div>
                                                </td>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <td>
                                            <div class="row">
                                                <div class="col-sm-4">
                                                    <html:text name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control" readonly="${TARGET_LOCKED}" />
                                                </div>
                                                <div class="col-sm-4">
                                                    <html:select name="${FORM_NAME}" property="secondaryOperator[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}">
                                                        <logic:iterate collection="<%= ConditionalOperator.getSecondaryOperatorsForMod() %>" id="operator">
                                                            <html:option value="${operator.operatorCode}"><bean:message key="target.operator.${operator.operatorKey}" /></html:option>
                                                        </logic:iterate>
                                                    </html:select>
                                                </div>
                                                <div class="col-sm-4">
                                                    <html:text name="${FORM_NAME}" property="secondaryValue[${index}]" styleClass="form-control" readonly="${TARGET_LOCKED}"/>
                                                </div>
                                            </div>
                                        </td>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_INTERVAL_MAILING}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="interval_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_MAILING_RECEIVED}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="all_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_MAILING_OPENED}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="all_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_MAILING_CLICKED}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="all_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_MAILING_REVENUE}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:select name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="all_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <agn:agnSelect name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control js-select" disabled="${TARGET_LOCKED}" data-ruleid="${index}" data-action="resetMailingUrls">
                                                <logic:iterate name="all_mailings" id="light_mailing">
                                                    <html:option value="${light_mailing.mailingID}">${light_mailing.shortname}</html:option>
                                                </logic:iterate>
                                            </agn:agnSelect>
                                        </div>
                                        <div class="col-sm-6">
                                            <agn:agnSelect name="${FORM_NAME}" property="secondaryValue[${index}]" styleClass="form-control js-select js-option-popovers" data-ruleid="${index}" data-action="updateMailingLinkId" disabled="${TARGET_LOCKED}">
                                                <logic:iterate name="${FORM_NAME}" property="validLinks[${index}]" id="link">
                                                    <agn:agnOption value="${link.id}">${link.fullUrl}</agn:agnOption>
                                                </logic:iterate>
                                            </agn:agnSelect>
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:when test="${columnType == COLUMN_TYPE_STRING && primaryOperator != OPERATOR_IS}">
                                <td>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <html:text name="${FORM_NAME}" property="primaryValue[${index}]" styleClass="form-control" readonly="${TARGET_LOCKED}" />
                                        </div>
                                    </div>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${primaryOperator == OPERATOR_IS}">
                                    <td>
                                        <div class="row">
                                            <div class="col-sm-12">
                                                <html:select name="${FORM_NAME}" property="primaryValue[${index}]" size="1" styleClass="form-control" disabled="${TARGET_LOCKED}">
                                                    <html:option value="null">null</html:option>
                                                    <html:option value="not null">not null</html:option>
                                                </html:select>
                                            </div>
                                        </div>
                                    </td>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                        <td>
                            <html:select styleClass="form-control" name="${FORM_NAME}" property="parenthesisClosed[${index}]" size="1" disabled="${TARGET_LOCKED}">
                                <html:option value="0">&nbsp</html:option>
                                <html:option value="1">)</html:option>
                            </html:select>
                        </td>
                        <emm:ShowByPermission token="targets.change">
                            <c:if test="${not TARGET_LOCKED}">
                                <!-- add / remove button -->
                                <td class="table-actions">
                                    <c:set var="deleteMessage" scope="page">
                                        <bean:message key="button.Delete"/>
                                    </c:set>
                                    <agn:agnLink class="btn btn-regular btn-alert" href="#" data-form-set="targetNodeToRemove:${index}" data-form-submit="0" data-tooltip="${deleteMessage}">
                                        <i class="icon icon-minus-circle"></i>
                                    </agn:agnLink>
                                </td>
                            </c:if>
                        </emm:ShowByPermission>
                    </tr>

                    <c:if test="${showErrorMessage}">
                        <tr class="error-row">
                            <td colspan="7">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;
                                <html:messages id="msg" message="false" name="rulesValidationErrors" property="targetrule.${index}.errors">
                                    ${msg}<br/>
                                </html:messages>
                            </td>
                        </tr>
                    </c:if>
                </c:if>
            </logic:iterate>

        </table>
    </div>
    </c:if>
</div>