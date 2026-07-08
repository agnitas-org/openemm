<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="hasActiveDelivery" type="java.lang.Boolean"--%>

<c:set var="agnTitleKey" 			      value="Reports" 								                          scope="request" />
<c:set var="sidemenu_active" 		      value="Statistics" 								                      scope="request" />
<c:set var="sidemenu_sub_active" 	      value="Reports" 								                          scope="request" />
<c:set var="agnBreadcrumbsRootKey"	      value="Reports" 								                          scope="request" />
<c:url var="agnBreadcrumbsRootUrl"        value="/statistics/reports.action?restoreSort=true"                     scope="request" />
<c:set var="agnHelpKey" 			      value="reports" 								                          scope="request" />
<c:set var="agnEditViewKey" 			  value="stat-report-view" 								                  scope="request" />
<c:set var="agnHighlightKey"              value="${birtReportForm.reportId gt 0 ? 'report.edit' : 'report.new'}"  scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${birtReportForm.reportId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="report.new"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${birtReportForm.shortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${birtReportForm.reportId gt 0}">
        <emm:instantiate var="action" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${action}"/>

            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap"/>
            <c:set target="${action}" property="dropDownItems" value="${dropDownItems}"/>
            <c:set target="${action}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${action}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>
                <c:set target="${dropDownItem}" property="url">
                    <c:url value="/statistics/report/evaluate.action"/>
                </c:set>

                <c:set target="${dropDownItem}" property="extraAttributes" value="data-form-target='#birt-report-view' data-evaluate-loader" />
                <c:set target="${dropDownItem}" property="name"><mvc:message code="Evaluate"/></c:set>
            </emm:instantiate>

            <c:if test="${hasActiveDelivery}">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="1" value="${dropDownItem}"/>
                    <c:set target="${dropDownItem}" property="extraAttributes" value="data-action='confirm-deactivate-deliveries'"/>
                    <c:set target="${dropDownItem}" property="name"><mvc:message code="report.deactivate.all"/></c:set>
                </emm:instantiate>
            </c:if>

            <emm:ShowByPermission token="report.birt.delete">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/statistics/report/delete.action?bulkIds=${birtReportForm.reportId}" />
                    </c:set>
                    <c:set target="${dropDownItem}" property="extraAttributes" value=" data-confirm=''"/>
                    <c:set target="${dropDownItem}" property="name"><mvc:message code="button.Delete"/></c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </emm:instantiate>
    </c:if>

    <emm:ShowByPermission token="report.birt.change">
        <emm:instantiate var="action" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${action}"/>
            <c:set target="${action}" property="extraAttributes" value="data-form-target='#birt-report-view' data-form-submit"/>
            <c:set target="${action}" property="iconBefore" value="icon-save"/>
            <c:set target="${action}" property="name"><mvc:message code="button.Save"/></c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
