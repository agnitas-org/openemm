<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="EVAL_TYPE_OPENRATE" value="<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE %>" scope="page"/>
<c:set var="EVAL_TYPE_CLICKS" value="<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE %>" scope="page"/>

<display:table id="key" name="${keys}" class="table table-bordered table-striped table-hover js-table">
    <c:set var="mailing" value="${stats[key]}"/>

    <display:column titleKey="Mailing" headerClass="js-table-sort">
        <c:choose>
            <c:when test="${not empty mailing.shortname}">
                ${mailing.shortname}
            </c:when>
            <c:otherwise>
                <mvc:message code="NotAvailableShort"/>
            </c:otherwise>
        </c:choose>
    </display:column>

    <display:column titleKey="statistic.clicker" headerClass="js-table-sort">
       ${mailing.clicks}
    </display:column>

    <display:column titleKey="statistic.Opened_Mails" headerClass="js-table-sort">
       ${mailing.opened}
    </display:column>

    <display:column titleKey="Recipients" headerClass="js-table-sort">
       ${mailing.totalMails}
    </display:column>

	<emm:ShowByPermission token="stats.revenue">
    	<display:column titleKey="statistic.revenue" headerClass="js-table-sort">
       		${mailing.revenue}
    	</display:column>
    </emm:ShowByPermission>

    <c:choose>
        <c:when test="${evalType == EVAL_TYPE_CLICKS}">
            <display:column titleKey="Clickrate" headerClass="js-table-sort">
                <fmt:formatNumber pattern="###.##%" value="${mailing.clickRate}"/>
            </display:column>
        </c:when>

        <c:when test="${evalType == EVAL_TYPE_OPENRATE}">
            <display:column titleKey="campaign.autoopt.evaltype.open" headerClass="js-table-sort">
                <fmt:formatNumber pattern="###.##%" value="${mailing.openRate}"/>
            </display:column>
        </c:when>

		<c:when test="${evalType == EVAL_TYPE_OPENRATE}">
			<emm:ShowByPermission token="stats.revenue">
            	<display:column titleKey="statistic.revenue" headerClass="js-table-sort">
                	<fmt:formatNumber maxFractionDigits="2" minFractionDigits="2" value="${mailing.revenue}"/>
            	</display:column>
            </emm:ShowByPermission>
        </c:when>
    </c:choose>
</display:table>
