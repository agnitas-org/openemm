<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
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
            <bean:message key="${affectedMailingsMessageKey}"/>
        </c:if>

        <c:choose>
            <c:when test="${affectedMailingsCount > 0}">
                <display:table name="affectedMailings" id="affectedMailing" class="errorTable" length="${affectedMailingsCount}">
                    <display:column>
                        <c:set var="mailingName" value="${affectedMailing.shortname}"/>
                        <c:set var="mailingLink"><html:rewrite page="/mailing/${affectedMailing.id}/settings.action"/></c:set>
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
                    <bean:message key="error.showNumberOfLeft" arg0="${fn:length(affectedMailings) - affectedMailingsCount}"/>
                </c:if>
            </c:when>
            <c:when test="${affectedMailingsLightweightCount > 0}">
                <display:table name="affectedMailingsLightweight" id="affectedMailing" class="errorTable" length="${affectedMailingsLightweightCount}">
                    <display:column>
                        <c:set var="mailingName" value="${affectedMailing.shortname}"/>
                        <c:set var="mailingLink"><html:rewrite page="/mailing/${affectedMailing.mailingID}/settings.action"/></c:set>

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
                    <bean:message key="error.showNumberOfLeft" arg0="${fn:length(affectedMailingsLightweight) - affectedMailingsLightweightCount}"/>
                </c:if>
            </c:when>
        </c:choose>
    </c:set>
</c:if>

<c:if test="${affectedReportsCount > 0}">
    <c:set var="affectedReportsTable">
        <c:if test="${not empty affectedReportsMessageKey}">
            <bean:message key="${affectedReportsMessageKey}"/>
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
            <bean:message key="error.showNumberOfLeft" arg0="${fn:length(affectedReports) - affectedReportsCount}"/>
        </c:if>
    </c:set>
</c:if>

<c:if test="${affectedDependentWorkflowsCount > 0}">
    <c:set var="affectedDependentWorkflowsTable">
        <c:if test="${not empty affectedDependentWorkflowsMessageKey}">
            <bean:message key="${affectedDependentWorkflowsMessageKey}" arg0=""/> <%--hardcode. Will change it, when change 'error.profiledb.dependency.workflow'--%>
        </c:if>

        <display:table name="affectedDependentWorkflows" id="affectedDependentWorkflow" class="errorTable" length="${affectedDependentWorkflowsCount}">
            <display:column>
                 <span title="<c:out value="${affectedDependentWorkflow}" escapeXml="true"/>" style="color: orange;">
                         ${affectedDependentWorkflow}<c:out value="${fn:trim(fn:substring(affectedDependentWorkflow, 0, AFFECTED_ENTITY_NAME_MAX_LENGTH-2))}" escapeXml="true"/>&#x2026;
                 </span>
            </display:column>
        </display:table>

        <c:if test="${affectedDependentWorkflowsCount < fn:length(affectedDependentWorkflows)}">
            <bean:message key="error.showNumberOfLeft" arg0="${fn:length(affectedDependentWorkflows) - affectedDependentWorkflowsCount}"/>
        </c:if>
    </c:set>
</c:if>

<logic:messagesPresent property="org.apache.struts.action.GLOBAL_MESSAGE" message="true">
    <script type="text/javascript" data-message="">
        <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="true">
            AGN.Lib.Messages('<bean:message key="default.Success"/>', '${emm:escapeJs(msg)}', 'success');
        </html:messages>
    </script>
</logic:messagesPresent>


<c:set var="showWarningMessages" value="false"/>
<c:set var="warningMessagesHtmlCode">
    <logic:messagesPresent property="de.agnitas.GLOBAL_WARNING" message="true">
        <c:set var="showWarningMessages" value="true"/>
        <html:messages id="msg" property="de.agnitas.GLOBAL_WARNING" message="true">${msg}<br/></html:messages>
    </logic:messagesPresent>

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


<c:set var="showPermanentWarningMessages" value="false"/>
<c:set var="permanentWarningMessagesHtmlCode">
    <c:if test="${not empty affectedMailingsTable and fn:toLowerCase(affectedMailingsMessageType) eq 'warning_permanent'}">
        <c:set var="showPermanentWarningMessages" value="true"/>
        ${affectedMailingsTable}
    </c:if>

    <c:if test="${not empty affectedReportsTable and fn:toLowerCase(affectedReportsMessageType) eq 'warning_permanent'}">
        <c:set var="showPermanentWarningMessages" value="true"/>
        ${affectedReportsTable}
    </c:if>

    <c:if test="${not empty affectedDependentWorkflowsTable and fn:toLowerCase(affectedDependentWorkflowsMessageType) eq 'warning_permanent'}">
        <c:set var="showWarningMessages" value="true"/>
        ${affectedDependentWorkflowsTable}
    </c:if>
</c:set>


<c:set var="showErrorMessages" value="false"/>
<c:set var="errorMessagesHtmlCode">
    <c:choose>
        <c:when test="${not empty formFieldErrorDontShow && formFieldErrorDontShow}">
            <logic:messagesPresent property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">
                <c:set var="showErrorMessages" value="true"/>
                <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">${msg}<br/></html:messages>
            </logic:messagesPresent>
        </c:when>
        <c:otherwise>
            <logic:messagesPresent message="false">
                <c:set var="showErrorMessages" value="true"/>
                <html:messages id="msg" message="false">${msg}<br/></html:messages>
            </logic:messagesPresent>
        </c:otherwise>
    </c:choose>

    <logic:notEmpty name="customErrorMessage">
        <c:set var="showErrorMessages" value="true"/>
        ${customErrorMessage}
    </logic:notEmpty>

    <logic:notEmpty name="errorReport">
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
    </logic:notEmpty>

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

<logic:equal name="showWarningMessages" value="true">
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
                    AGN.Lib.Messages('<bean:message key="warning"/>', messageHtmlCode, 'warning');
                }
            }
        })();
    </script>
</logic:equal>

<logic:equal name="showPermanentWarningMessages" value="true">
    <script type="text/html" id="messages-warnings-permanent-${SUFFIX}" data-message="">
        ${permanentWarningMessagesHtmlCode}
    </script>

    <script type="text/javascript" data-message="">
      (function(){
        var messageResource = $('script#messages-warnings-permanent-${SUFFIX}[data-message=""]');
        var messageHtmlCode = '';
        if (messageResource) {
          messageHtmlCode = messageResource.html();
          messageResource.remove();
          if (messageHtmlCode) {
            AGN.Lib.Messages('<bean:message key="warning"/>', messageHtmlCode, 'warning_permanent');
          }
        }
      })();
    </script>
</logic:equal>

<logic:equal name="showErrorMessages" value="true">
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
            AGN.Lib.Messages('<bean:message key="Error"/>', messageHtmlCode, 'alert');
          }
        }
      })();
    </script>
</logic:equal>

<c:if test="${POPUPS_FIELDS_ERRORS ne null}">
    <c:forEach var="fieldError" items="${POPUPS_FIELDS_ERRORS}">
        <script type="text/html" data-message="${fieldError.fieldName}">
                <mvc:message code="${fieldError.message.code}" arguments="${fieldError.argumentsStr}"/>
        </script>
    </c:forEach>
</c:if>

<logic:messagesPresent property="de.agnitas.GLOBAL_WARNING_PERMANENT" message="true">
    <script type="text/javascript" data-message>
        <html:messages id="msg" property="de.agnitas.GLOBAL_WARNING_PERMANENT" message="true" >
            AGN.Lib.Messages('<bean:message key="warning" />', '${emm:escapeJs(msg)}', 'warning_permanent');
        </html:messages>
    </script>
</logic:messagesPresent>
