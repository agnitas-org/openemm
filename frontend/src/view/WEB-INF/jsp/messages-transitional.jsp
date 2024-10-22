<%--  TODO: EMMGUI-714: remove after old design will be removed  --%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="affectedMailingsMessageKey" type="java.lang.String"--%>
<%--@elvariable id="affectedMailingsMessageType" type="java.lang.String"--%>
<%--@elvariable id="affectedMailings" type="java.util.List"--%>

<%--@elvariable id="affectedReportsMessageKey" type="java.lang.String"--%>
<%--@elvariable id="affectedReportsMessageType" type="java.lang.String"--%>
<%--@elvariable id="affectedReports" type="java.util.List"--%>

<%--@elvariable id="affectedDependentWorkflowsMessageKey" type="java.lang.String"--%>
<%--@elvariable id="affectedDependentWorkflowsMessageType" type="java.lang.String"--%>
<%--@elvariable id="affectedDependentWorkflows" type="java.util.List"--%>

<%--@elvariable id="affectedMailingsLightweight" type="java.util.List"--%>

<c:set var="SUFFIX" value="${emm:milliseconds()}"/>
<c:set var="AFFECTED_ENTITY_NAME_MAX_LENGTH" value="30"/>
<c:set var="AFFECTED_ENTITIES_LIMIT" value="20"/>

<%-- Min(list.length, AFFECTED_ENTITIES_LIMIT) --%>
<c:set var="affectedMailingsCount" value="${fn:length(affectedMailings)}"/>
<c:if test="${affectedMailingsCount > AFFECTED_ENTITIES_LIMIT}">
    <c:set var="affectedMailingsCount" value="${AFFECTED_ENTITIES_LIMIT}"/>
</c:if>

<%-- Min(list.length, AFFECTED_ENTITIES_LIMIT) --%>
<c:set var="affectedMailingsLightweightCount" value="${fn:length(affectedMailingsLightweight)}"/>
<c:if test="${affectedMailingsLightweightCount > AFFECTED_ENTITIES_LIMIT}">
    <c:set var="affectedMailingsLightweightCount" value="${AFFECTED_ENTITIES_LIMIT}"/>
</c:if>

<%-- Min(list.length, AFFECTED_ENTITIES_LIMIT) --%>
<c:set var="affectedReportsCount" value="${fn:length(affectedReports)}"/>
<c:if test="${affectedReportsCount > AFFECTED_ENTITIES_LIMIT}">
    <c:set var="affectedReportsCount" value="${AFFECTED_ENTITIES_LIMIT}"/>
</c:if>

<%-- Min(list.length, AFFECTED_ENTITIES_LIMIT) --%>
<c:set var="affectedDependentWorkflowsCount" value="${fn:length(affectedDependentWorkflows)}"/>
<c:if test="${affectedDependentWorkflowsCount > AFFECTED_ENTITIES_LIMIT}">
    <c:set var="affectedDependentWorkflowsCount" value="${AFFECTED_ENTITIES_LIMIT}"/>
</c:if>


<%-- Keep in mind that mailings and lightweight mailings are basically the same entities so they're never combined --%>
<%-- If mailing are there then lightweight mailings should be ignored --%>
<c:if test="${affectedReportsCount > 0 and (affectedMailingsCount > 0 or affectedMailingsLightweightCount > 0)}">
    <c:choose>
        <%-- Reports + mailings exceed a limit --%>
        <c:when test="${affectedReportsCount + affectedMailingsCount > AFFECTED_ENTITIES_LIMIT}">
            <c:choose>
                <c:when test="${affectedReportsCount > AFFECTED_ENTITIES_LIMIT / 2 and affectedMailingsCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedReportsCount" value="${AFFECTED_ENTITIES_LIMIT / 2}"/>
                    <c:set var="affectedMailingsCount" value="${AFFECTED_ENTITIES_LIMIT / 2}"/>
                </c:when>
                <c:when test="${affectedReportsCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedReportsCount" value="${AFFECTED_ENTITIES_LIMIT - affectedMailingsCount}"/>
                </c:when>
                <c:when test="${affectedMailingsCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedMailingsCount" value="${AFFECTED_ENTITIES_LIMIT - affectedReportsCount}"/>
                </c:when>
            </c:choose>
        </c:when>
        <%-- Reports + lightweight mailings exceed a limit --%>
        <c:when test="${affectedReportsCount + affectedMailingsLightweightCount > AFFECTED_ENTITIES_LIMIT}">
            <c:choose>
                <c:when test="${affectedReportsCount > AFFECTED_ENTITIES_LIMIT / 2 and affectedMailingsLightweightCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedReportsCount" value="${AFFECTED_ENTITIES_LIMIT / 2}"/>
                    <c:set var="affectedMailingsLightweightCount" value="${AFFECTED_ENTITIES_LIMIT / 2}"/>
                </c:when>
                <c:when test="${affectedReportsCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedReportsCount" value="${AFFECTED_ENTITIES_LIMIT - affectedMailingsLightweightCount}"/>
                </c:when>
                <c:when test="${affectedMailingsLightweightCount > AFFECTED_ENTITIES_LIMIT / 2}">
                    <c:set var="affectedMailingsLightweightCount" value="${AFFECTED_ENTITIES_LIMIT - affectedReportsCount}"/>
                </c:when>
            </c:choose>
        </c:when>
    </c:choose>
</c:if>

<c:if test="${affectedMailingsCount > 0 or affectedMailingsLightweightCount > 0}">
    <c:set var="affectedMailingsTable">
        <c:if test="${not empty affectedMailingsMessageKey}">
            <mvc:message code="${affectedMailingsMessageKey}"/>
        </c:if>

        <c:choose>
            <c:when test="${affectedMailingsCount > 0}">
                <display:table name="affectedMailings" id="affectedMailing" class="errorTable" length="${affectedMailingsCount}">
                    <display:column>
                        <c:set var="mailingName" value="${affectedMailing.shortname}"/>
                        <c:url var="mailingLink" value="/mailing/${affectedMailing.id}/settings.action" />
                        <c:choose>
                            <c:when test="${fn:length(mailingName) > AFFECTED_ENTITY_NAME_MAX_LENGTH}">
                                <a href="${mailingLink}" title="<c:out value="${mailingName}" escapeXml="true"/>" style="color: red;">
                                    <c:out value="${fn:trim(fn:substring(mailingName, 0, AFFECTED_ENTITY_NAME_MAX_LENGTH-2))}" escapeXml="true"/>&#x2026;
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${mailingLink}" style="color: red;">
                                    <c:out value="${mailingName}" escapeXml="true"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                </display:table>

                <c:if test="${affectedMailingsCount < fn:length(affectedMailings)}">
                    <mvc:message code="error.showNumberOfLeft" arguments="${fn:length(affectedMailings) - affectedMailingsCount}"/>
                </c:if>
            </c:when>
            <c:when test="${affectedMailingsLightweightCount > 0}">
                <display:table name="affectedMailingsLightweight" id="affectedMailing" class="errorTable" length="${affectedMailingsLightweightCount}">
                    <display:column>
                        <c:set var="mailingName" value="${affectedMailing.shortname}"/>
                        <c:url var="mailingLink" value="/mailing/${affectedMailing.mailingID}/settings.action" />

                        <c:choose>
                            <c:when test="${fn:length(mailingName) > AFFECTED_ENTITY_NAME_MAX_LENGTH}">
                                <a href="${mailingLink}" title="<c:out value="${mailingName}" escapeXml="true"/>" style="color: orange;">
                                    <c:out value="${fn:trim(fn:substring(mailingName, 0, AFFECTED_ENTITY_NAME_MAX_LENGTH-2))}" escapeXml="true"/>&#x2026;
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${mailingLink}" style="color: orange;">
                                    <c:out value="${mailingName}" escapeXml="true"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                </display:table>

                <c:if test="${affectedMailingsLightweightCount < fn:length(affectedMailingsLightweight)}">
                    <mvc:message code="error.showNumberOfLeft" arguments="${fn:length(affectedMailingsLightweight) - affectedMailingsLightweightCount}"/>
                </c:if>
            </c:when>
        </c:choose>
    </c:set>
</c:if>

<c:if test="${affectedReportsCount > 0}">
    <c:set var="affectedReportsTable">
        <c:if test="${not empty affectedReportsMessageKey}">
            <mvc:message code="${affectedReportsMessageKey}"/>
        </c:if>

        <display:table name="affectedReports" id="affectedReport" class="errorTable" length="${affectedReportsCount}">
            <display:column>
                <c:set var="reportName" value="${affectedReport.shortname}"/>
                <c:url var="reportLink" value="/statistics/report/${affectedReport.id}/view.action"/>

                <c:choose>
                    <c:when test="${fn:length(reportName) > AFFECTED_ENTITY_NAME_MAX_LENGTH}">
                        <a href="${reportLink}" title="<c:out value="${reportName}" escapeXml="true"/>" style="color: orange;">
                            <c:out value="${fn:trim(fn:substring(reportName, 0, AFFECTED_ENTITY_NAME_MAX_LENGTH-2))}" escapeXml="true"/>&#x2026;
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${reportLink}" style="color: orange;">
                            <c:out value="${reportName}" escapeXml="true"/>
                        </a>
                    </c:otherwise>
                </c:choose>
            </display:column>
        </display:table>

        <c:if test="${affectedReportsCount < fn:length(affectedReports)}">
            <mvc:message code="error.showNumberOfLeft" arguments="${fn:length(affectedReports) - affectedReportsCount}"/>
        </c:if>
    </c:set>
</c:if>

<c:if test="${affectedDependentWorkflowsCount > 0}">
    <c:set var="affectedDependentWorkflowsTable">
        <c:if test="${not empty affectedDependentWorkflowsMessageKey}">
            <mvc:message code="${affectedDependentWorkflowsMessageKey}" arguments=""/> <%--hardcode. Will change it, when change 'error.profiledb.dependency.workflow'--%>
        </c:if>

        <display:table name="affectedDependentWorkflows" id="affectedDependentWorkflow" class="errorTable" length="${affectedDependentWorkflowsCount}">
            <display:column>
                 <span title="<c:out value="${affectedDependentWorkflow}" escapeXml="true"/>" style="color: orange;">
                         ${affectedDependentWorkflow}<c:out value="${fn:trim(fn:substring(affectedDependentWorkflow, 0, AFFECTED_ENTITY_NAME_MAX_LENGTH-2))}" escapeXml="true"/>&#x2026;
                 </span>
            </display:column>
        </display:table>

        <c:if test="${affectedDependentWorkflowsCount < fn:length(affectedDependentWorkflows)}">
            <mvc:message code="error.showNumberOfLeft" arguments="${fn:length(affectedDependentWorkflows) - affectedDependentWorkflowsCount}"/>
        </c:if>
    </c:set>
</c:if>

<emm:messagesPresent type="success">
    <script type="text/javascript" data-message="">
        <emm:messages var="msg" type="success">
            AGN.Lib.Messages('<mvc:message code="default.Success"/>', '${emm:escapeJs(msg)}', 'success');
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="info">
    <script type="text/javascript" data-message="">
        <emm:messages var="msg" type="info">
            AGN.Lib.Messages('<mvc:message code="Info"/>', '${emm:escapeJs(msg)}', 'info');
        </emm:messages>
    </script>
</emm:messagesPresent>


<c:set var="showWarningMessages" value="false"/>
<c:set var="warningMessagesHtmlCode">
    <emm:messagesPresent type="warning">
        <c:set var="showWarningMessages" value="true"/>
        <emm:messages var="msg" type="warning">${msg}<br/></emm:messages>
    </emm:messagesPresent>

    <c:if test="${not empty affectedMailingsTable and fn:toLowerCase(affectedMailingsMessageType) eq 'warning'}">
        <c:set var="showWarningMessages" value="true"/>
        ${affectedMailingsTable}
    </c:if>

    <c:if test="${not empty affectedReportsTable and fn:toLowerCase(affectedReportsMessageType) eq 'warning'}">
        <c:set var="showWarningMessages" value="true"/>
        ${affectedReportsTable}
    </c:if>

    <c:if test="${not empty affectedDependentWorkflowsTable and fn:toLowerCase(affectedDependentWorkflowsMessageType) eq 'warning'}">
        <c:set var="showWarningMessages" value="true"/>
        ${affectedDependentWorkflowsTable}
    </c:if>
</c:set>

<c:set var="showErrorMessages" value="false"/>
<c:set var="errorMessagesHtmlCode">
    <emm:messagesPresent type="error">
        <c:set var="showErrorMessages" value="true"/>
        <emm:messages var="msg" type="error">${msg}<br/></emm:messages>
    </emm:messagesPresent>

    <c:if test="${errorReport ne null and errorReport.size() gt 0}">
        <%-- Isn't shown if there's no error messages --%>
        <display:table name='errorReport' id='errorMessageReportRow' class='errorTable' >
            <display:column  headerClass='head_name' class='name'  sortable='false' titleKey='mailing.tag'>
                <c:choose>
                    <c:when test='${not empty errorMessageReportRow[1]}'>
                        ${errorMessageReportRow[1]}
                    </c:when>
                    <c:otherwise>
                        ${errorMessageReportRow[2]}
                    </c:otherwise>
                </c:choose>
            </display:column>
        </display:table>
    </c:if>

    <c:if test="${not empty affectedMailingsTable and fn:toLowerCase(affectedMailingsMessageType) eq 'alert'}">
        <c:set var="showErrorMessages" value="true"/>
        ${affectedMailingsTable}
    </c:if>

    <c:if test="${not empty affectedReportsTable and fn:toLowerCase(affectedReportsMessageType) eq 'alert'}">
        <c:set var="showErrorMessages" value="true"/>
        ${affectedReportsTable}
    </c:if>

    <c:if test="${not empty affectedDependentWorkflowsTable and fn:toLowerCase(affectedDependentWorkflowsMessageType) eq 'alert'}">
        <c:set var="showWarningMessages" value="true"/>
        ${affectedDependentWorkflowsTable}
    </c:if>

    <c:if test="${not empty mediapoolAffectedEntities}">
        <c:set var="showErrorMessages" value="true"/>
        <jsp:include page="grid/mediapool/messages/mediapool-affected-list-message.jsp">
            <jsp:param name="affectedEntitiesLimit" value="${AFFECTED_ENTITIES_LIMIT}"/>
            <jsp:param name="affectedEntityNameMaxLength" value="${AFFECTED_ENTITY_NAME_MAX_LENGTH}"/>
        </jsp:include>
    </c:if>
</c:set>

<c:if test="${showWarningMessages}">
    <script type="text/html" id="messages-warnings-${SUFFIX}" data-message="">
        ${warningMessagesHtmlCode}
    </script>

    <script type="text/javascript" data-message="">
        (function(){
            var messageResource = $('script#messages-warnings-${SUFFIX}[data-message=""]');
            var messageHtmlCode = '';
            if (messageResource) {
                messageHtmlCode = messageResource.html();
                messageResource.remove();
                if (messageHtmlCode) {
                    AGN.Lib.Messages('<mvc:message code="warning"/>', messageHtmlCode, 'warning');
                }
            }
        })();
    </script>
</c:if>

<c:if test="${showErrorMessages}">
    <script type="text/html" id="messages-errors-${SUFFIX}" data-message="">
        ${errorMessagesHtmlCode}
    </script>

    <script type="text/javascript" data-message="">
      (function(){
        var messageResource = $('script#messages-errors-${SUFFIX}[data-message=""]');
        var messageHtmlCode = '';
        if (messageResource) {
          messageHtmlCode = messageResource.html();
          messageResource.remove();
          if (messageHtmlCode) {
            AGN.Lib.Messages('<mvc:message code="Error"/>', messageHtmlCode, 'alert');
          }
        }
      })();
    </script>
</c:if>

<emm:messagesPresent type="error" formField="true">
    <emm:fieldMessages var="msg" type="error" fieldNameVar="fieldName">
        <script type="text/html" data-message="${fieldName}">
            ${msg}
        </script>
    </emm:fieldMessages>
</emm:messagesPresent>
