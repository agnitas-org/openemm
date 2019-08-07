<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineTimeUnit" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionCriteria" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop.WorkflowEndType" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="org.agnitas.target.TargetNode" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<script type="text/javascript">
    // filling of localized messages for client functionality
    var localizedMessages = [];
    <c:forEach var="message" items="${clientMessages}">
            localizedMessages['${message}'] = '<bean:message key="${message}"/>';
    </c:forEach>

    // transferring of constants to client side
    var beanConstants = {

        // start/end icons constants
        startTypeOpen: '<%= WorkflowStartType.OPEN %>',
        startTypeDate: '<%= WorkflowStartType.DATE %>',
        startTypeEvent: '<%= WorkflowStartType.EVENT %>',
        endTypeAutomatic: '<%= WorkflowEndType.AUTOMATIC %>',
        endTypeDate: '<%= WorkflowEndType.DATE %>',
        startEventReaction: '<%= WorkflowStartEventType.EVENT_REACTION %>',
        startEventDate: '<%= WorkflowStartEventType.EVENT_DATE %>',

        // deadline icon constants
        deadlineTypeDelay: '<%= WorkflowDeadlineType.TYPE_DELAY %>',
        deadlineTypeFixedDeadline: '<%= WorkflowDeadlineType.TYPE_FIXED_DEADLINE %>',
        deadlineTimeUnitMinute: '<%= WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE %>',
        deadlineTimeUnitHour: '<%= WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR %>',
        deadlineTimeUnitDay: '<%= WorkflowDeadlineTimeUnit.TIME_UNIT_DAY %>',
        deadlineTimeUnitWeek: '<%= WorkflowDeadlineTimeUnit.TIME_UNIT_WEEK %>',
        deadlineTimeUnitMonth: '<%= WorkflowDeadlineTimeUnit.TIME_UNIT_MONTH %>',

        defaultImportDelayLimit : '<%= WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT %>',

        // general constants
        reactionOpened: '<%=  WorkflowReactionType.OPENED.name() %>',
        reactionNotOpened: '<%=  WorkflowReactionType.NOT_OPENED.name() %>',
        reactionClicked: '<%=  WorkflowReactionType.CLICKED.name() %>',
        reactionNotClicked: '<%=  WorkflowReactionType.NOT_CLICKED.name() %>',
        reactionBought: '<%=  WorkflowReactionType.BOUGHT.name() %>',
        reactionNotBought: '<%=  WorkflowReactionType.NOT_BOUGHT.name() %>',
        reactionDownload: '<%=  WorkflowReactionType.DOWNLOAD.name() %>',
        reactionChangeOfProfile: '<%=  WorkflowReactionType.CHANGE_OF_PROFILE.name() %>',
        reactionWaitingForConfirm: '<%=  WorkflowReactionType.WAITING_FOR_CONFIRM.name() %>',
        reactionOptIn: '<%=  WorkflowReactionType.OPT_IN.name() %>',
        reactionOptOut: '<%=  WorkflowReactionType.OPT_OUT.name() %>',
        reactionClickedLink: '<%=  WorkflowReactionType.CLICKED_LINK.name() %>',
        reactionOpenedAndClicked: '<%=  WorkflowReactionType.OPENED_AND_CLICKED.name() %>',
        reactionOpenedOrClicked: '<%=  WorkflowReactionType.OPENED_OR_CLICKED.name() %>',
        reactionConfirmedOptIn: '<%=  WorkflowReactionType.CONFIRMED_OPT_IN.name() %>',

        workflowURL: '<c:url value="/workflow.do"/>',
        componentURL: '<c:url value="/sc?compID={component-id}"/>',

        // decision constants
        decisionTypeDecision: '<%= WorkflowDecisionType.TYPE_DECISION %>',
        decisionTypeAutoOptimization: '<%= WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION %>',
        decisionReaction: '<%= WorkflowDecisionCriteria.DECISION_REACTION %>',
        decisionProfileField: '<%= WorkflowDecisionCriteria.DECISION_PROFILE_FIELD %>',
        decisionAOCriteriaClickRate: '<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE %>',
        decisionAOCriteriaOpenrate: '<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE %>',
        decisionAOCriteriaTurnover : '<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE %>',

        operators: [],
        operatorsMap: {},

        genderOptions: {
            <%= Recipient.GENDER_MALE %>: "Male",
            <%= Recipient.GENDER_FEMALE %>: "Female",
            <%= Recipient.GENDER_UNKNOWN %>: "Unknown"
        },

        chainOperatorOptions: {
            <%= TargetNode.CHAIN_OPERATOR_AND %>: "<bean:message key="default.and"/>",
            <%= TargetNode.CHAIN_OPERATOR_OR %>: "<bean:message key="default.or"/>"
        }
    };

    <logic:iterate collection="${operators}" id="operator">
        <c:set var="types" value="${operatorsTypeSupportMap[operator]}"/>
        beanConstants.operatorsMap[${operator.operatorCode}] = '${operator.operatorSymbol}';
        beanConstants.operators.push({
          id: '${operator.operatorCode}',
          text: '${operator.operatorSymbol}',
          data: {
            types: '${empty types ? '' : types}'
          }
        });
    </logic:iterate>
</script>
