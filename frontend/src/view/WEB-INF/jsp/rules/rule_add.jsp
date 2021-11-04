<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ page import="org.agnitas.target.ConditionalOperator" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="HIDE_SPECIAL_TARGET_FEATURES" type="java.lang.Boolean"--%>
<%--@elvariable id="TARGET_LOCKED" type="java.lang.Boolean"--%>

<%--@elvariable id="FORM_NAME" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_INTERVAL_MAILING" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_MAILING_OPENED" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_MAILING_CLICKED" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_MAILING_RECEIVED" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_MAILING_REVENUE" type="java.lang.String"--%>
<%--@elvariable id="COLUMN_MAILING_CLICKED_ON_SPECIFIC_LINK" type="java.lang.String"--%>

<%--@elvariable id="_colsel_column_name" type="java.lang.String"--%>
<%--@elvariable id="_colsel_shortname" type="java.lang.String"--%>
<%--@elvariable id="_colsel_data_type" type="java.lang.String"--%>
<%--@elvariable id="_colsel_shortname" type="java.lang.String"--%>

<bean:define id="index" name="${FORM_NAME}" property="numTargetNodes" toScope="page" />

<c:if test="${empty TARGET_LOCKED}">
	<c:set var="TARGET_LOCKED" value="false" scope="page" />
</c:if>

<table id="add-rule-table" class="table table-bordered table-form" data-controller="rules-add">
    <tr>
        <td>
            <c:choose>
                <c:when test="${index != 0}">
                    <html:select styleClass="form-control" property="chainOperatorNew" size="1" disabled="${TARGET_LOCKED}">
                        <html:option value="<%= Integer.toString(ChainOperator.AND.getOperatorCode()) %>" key="default.and" />
                        <html:option value="<%= Integer.toString(ChainOperator.OR.getOperatorCode()) %>" key="default.or" />
                    </html:select>
                </c:when>
                <c:otherwise>
                    <html:hidden property="chainOperatorNew" value="0"/>
                </c:otherwise>
            </c:choose>
        </td>

        <td>
            <html:select styleClass="form-control" property="parenthesisOpenedNew" size="1" disabled="${TARGET_LOCKED}">
                <html:option value="0">&nbsp</html:option>
                <html:option value="1">(</html:option>
            </html:select>
        </td>

        <td>
            <agn:agnSelect property="columnAndTypeNew" value="email" size="1"  styleClass="form-control js-select" disabled="${TARGET_LOCKED}" data-action="columnAndTypeNew">
                <emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>">
                    <agn:agnOption data-extra="${_colsel_data_type}" value="${fn:toUpperCase(_colsel_column_name)}">${_colsel_shortname}</agn:agnOption>
                </emm:ShowColumnInfo>
                <agn:agnOption data-extra="INTERVAL_MAILING" value="${COLUMN_INTERVAL_MAILING}"><bean:message key="receivedIntervalMailing"/></agn:agnOption>
                
                <c:if test="${not HIDE_SPECIAL_TARGET_FEATURES}">
                    <agn:agnOption data-extra="MAILING" value="${COLUMN_MAILING_OPENED}"><bean:message key="target.rule.mailingOpened"/></agn:agnOption>
                    <agn:agnOption data-extra="MAILING" value="${COLUMN_MAILING_CLICKED}"><bean:message key="target.rule.mailingClicked"/></agn:agnOption>
                    <agn:agnOption data-extra="MAILING" value="${COLUMN_MAILING_RECEIVED}"><bean:message key="target.rule.mailingReceived"/></agn:agnOption>
                     <agn:agnOption data-extra="MAILING" value="${COLUMN_MAILING_REVENUE}"><bean:message key="target.rule.mailingRevenue"/></agn:agnOption>
                     <agn:agnOption data-extra="MAILING_LINKS" value="${COLUMN_MAILING_CLICKED_ON_SPECIFIC_LINK}"><bean:message key="target.rule.mailingClickedSpecificLink"/></agn:agnOption>
                </c:if>

                <agn:agnOption data-extra="DATE" value="CURRENT_TIMESTAMP" key="default.sysdate"><bean:message key="default.sysdate"/></agn:agnOption>
            </agn:agnSelect>
        </td>

        <td>
            <agn:agnSelect property="primaryOperatorNew" size="1"  styleClass="form-control" disabled="${TARGET_LOCKED}" data-action="primaryOperatorNew">
                <logic:iterate collection="<%= ConditionalOperator.values() %>" id="all_operator">
                    <c:if test="${not HIDE_SPECIAL_TARGET_FEATURES or (not (all_operator.operatorKey eq 'yes' or all_operator.operatorKey eq 'no'))}">
                        <html:option value="${all_operator.operatorCode}"><bean:message key="target.operator.${all_operator.operatorKey}" /></html:option>
                    </c:if>
                </logic:iterate>
            </agn:agnSelect>
        </td>

        <td>
            <div class="row">
                <div class="col-sm-12">
                    <html:text property="primaryValueNew" styleClass="form-control" readonly="${TARGET_LOCKED}" />
                </div>
            </div>
        </td>

        <td>
            <html:select styleClass="form-control" property="parenthesisClosedNew" size="1" disabled="${TARGET_LOCKED}">
                <html:option value="0">&nbsp</html:option>
                <html:option value="1">)</html:option>
            </html:select>
        </td>

        <td class="table-actions">
            <c:if test="${not TARGET_LOCKED}">
                <c:set var="addMessage" scope="page">
                    <bean:message key="button.Add"/>
                </c:set>
                <agn:agnLink href="#" class="btn btn-regular btn-secondary" data-form-set="addTargetNode:true" data-form-submit="" data-tooltip="${addMessage}">
                    <i class="icon icon-plus-circle"></i>
                </agn:agnLink>
            </c:if>
        </td>

    </tr>
</table>
