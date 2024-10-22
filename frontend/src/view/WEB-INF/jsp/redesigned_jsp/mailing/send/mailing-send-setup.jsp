<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="approvePossible" type="java.lang.Boolean"--%>
<%--@elvariable id="isThresholdClearanceExceeded" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>

<c:set var="isFollowUpMailing" value="${form.mailingtype eq MailingType.FOLLOW_UP.code}" scope="request" />
<c:set var="isNormalMailing" value="${form.mailingtype eq MailingType.NORMAL.code}" scope="request" />
<c:set var="isDateBasedMailing" value="${form.mailingtype eq MailingType.DATE_BASED.code}" scope="request" />
<c:set var="isIntervalMailing" value="${form.mailingtype eq MailingType.INTERVAL.code}" scope="request" />
<c:set var="isActionBasedMailing" value="${form.mailingtype eq MailingType.ACTION_BASED.code}" scope="request" />
<c:set var="canLoadStatusBox" value="${isNormalMailing or isFollowUpMailing}" scope="request" />

<c:set var="activeStatusKey" value="<%= MailingStatus.ACTIVE.getMessageKey() %>"/>
<c:set var="disableStatusKey" value="<%= MailingStatus.DISABLE.getMessageKey() %>"/>
<c:set var="newStatusKey" value="<%= MailingStatus.NEW.getMessageKey() %>"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, form.workflowId)}" scope="request"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="request"/>

<c:set var="tmpMailingID" value="${form.mailingID}" scope="request" />
<c:set var="isTemplate" value="${form.isTemplate}" scope="request" />

<c:set var="sidemenu_active" 		value="Mailings"	            scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 	            scope="request" />
<c:set var="agnHelpKey" 			value="preview" 	            scope="request" />

<c:set var="agnEditViewKey" value="${isTemplate ? 'template-send-view' : 'mailing-send-view'}" scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 	     scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 		     scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		     scope="request" />
        <c:set var="agnHighlightKey" 		value="template.testing"     scope="request" />
        <c:set var="agnHelpKey"             value="mailingsCheck"        scope="request" />
        <c:url var="agnBreadcrumbsRootUrl"  value="/mailing/list.action" scope="request">
            <c:param name="forTemplates" value="true"/>
        </c:url>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${tmpMailingID}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${form.isMailingGrid}">
                <c:set var="agnNavigationKey" value="GridMailingView" scope="request" />

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${tmpMailingID}"/>
                </emm:instantiate>
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" value="mailingView" scope="request" />

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${tmpMailingID}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavConditionsParams}" property="isActiveMailing" value="${form.worldMailingSend}" />
            <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
            <c:set target="${agnNavConditionsParams}" property="isPostMailing" value="${not empty isPostMailing and isPostMailing}" />
        </emm:instantiate>

        <c:set var="agnTitleKey" 		     value="Mailing" 	            scope="request" />
        <c:set var="agnHighlightKey" 	     value="Send_Mailing"           scope="request" />
        <c:set var="sidemenu_sub_active"     value="default.Overview" 		scope="request" />
        <c:set var="agnHelpKey" 		     value="mailingsCheck"          scope="request" />
        <c:url var="agnBreadcrumbsRootUrl"   value="/mailing/list.action"   scope="request" />
    </c:otherwise>
</c:choose>

<%--  ----- Breadcrubms ---- --%>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
    </emm:instantiate>
</emm:instantiate>

<c:if test="${not isTemplate}">
    <%--  ----- ACTIONS---- --%>
    <emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <c:if test="${approvePossible and not form.hasDeletedTargetGroups}">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:url var="approveUrl" value="/mailing/send/${tmpMailingID}/unlock.action"/>

                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

                <c:set target="${element}" property="type" value="href"/>
                <c:set target="${element}" property="url" value="${approveUrl}"/>
                <c:set target="${element}" property="iconBefore" value="icon-check"/>
                <c:set target="${element}" property="name">
                    <mvc:message code="default.unlock"/>
                </c:set>
            </emm:instantiate>
        </c:if>

        <emm:ShowByPermission token="mailing.send.world">
            <c:choose>
                <c:when test="${isNormalMailing or isFollowUpMailing}">
                    <c:if test="${not isWorkflowDriven and canSendOrActivateMailing and not form.hasDeletedTargetGroups}">
                        <emm:instantiate var="element" type="java.util.LinkedHashMap">
                            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                            <c:set target="${element}" property="extraAttributes" value="data-action='send-world'"/>
                            <c:set target="${element}" property="iconBefore" value="icon-paper-plane"/>
                            <c:set target="${element}" property="name">
                                <mvc:message code="button.Send"/>
                            </c:set>
                        </emm:instantiate>
                    </c:if>
                </c:when>

                <c:when test="${isActionBasedMailing}">
                    <c:if test="${not isWorkflowDriven}">
                        <c:if test="${not form.worldMailingSend and canSendOrActivateMailing}">
                            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                                <c:url var="confirmActionBasedActivationUrl" value="/mailing/send/${tmpMailingID}/actionbased/activation/confirm.action"/>
                                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                                <c:set target="${element}" property="type" value="href"/>
                                <c:set target="${element}" property="url" value="${confirmActionBasedActivationUrl}"/>
                                <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
                                <c:set target="${element}" property="iconBefore" value="icon-paper-plane"/>
                                <c:set target="${element}" property="name">
                                    <mvc:message code="button.Activate"/>
                                </c:set>
                            </emm:instantiate>
                        </c:if>

                        <c:if test="${form.worldMailingSend and empty bounceFilterNames}">
                            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                                <c:url var="confirmActionBasedDeactivationUrl" value="/mailing/send/${tmpMailingID}/deactivate/confirm.action"/>
                                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                                <c:set target="${element}" property="type" value="href"/>
                                <c:set target="${element}" property="url" value="${confirmActionBasedDeactivationUrl}"/>
                                <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
                                <c:set target="${element}" property="iconBefore" value="icon-state-alert"/>
                                <c:set target="${element}" property="name">
                                    <mvc:message code="btndeactivate"/>
                                </c:set>
                            </emm:instantiate>
                        </c:if>
                    </c:if>
                </c:when>

                <c:when test="${isDateBasedMailing}">
                    <c:if test="${not isWorkflowDriven}">
                        <c:choose>
                            <c:when test="${form.worldMailingSend}">
                                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                                    <c:url var="confirmDateBasedDeactivationUrl" value="/mailing/send/${tmpMailingID}/deactivate/confirm.action"/>
                                    <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                                    <c:set target="${element}" property="type" value="href"/>
                                    <c:set target="${element}" property="url" value="${confirmDateBasedDeactivationUrl}"/>
                                    <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
                                    <c:set target="${element}" property="iconBefore" value="icon-state-alert"/>
                                    <c:set target="${element}" property="name">
                                        <mvc:message code="btndeactivate"/>
                                    </c:set>
                                </emm:instantiate>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${canSendOrActivateMailing and not form.hasDeletedTargetGroups}">
                                    <emm:instantiate var="element" type="java.util.LinkedHashMap">
                                        <c:url var="confirmDateBasedActivationUrl" value="/mailing/send/datebased/activation/confirm.action"/>
                                        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                                        <c:set target="${element}" property="extraAttributes" value="data-form-target='#delivery-settings-form' data-form-confirm='' data-form-url='${confirmDateBasedActivationUrl}'"/>
                                        <c:set target="${element}" property="iconBefore" value="icon-paper-plane"/>
                                        <c:set target="${element}" property="name">
                                            <mvc:message code="button.Activate"/>
                                        </c:set>
                                    </emm:instantiate>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <c:if test="${isThresholdClearanceExceeded and not form.hasDeletedTargetGroups}">
                        <emm:instantiate var="element" type="java.util.LinkedHashMap">
                            <c:url var="resumeSendingLink" value="/mailing/send/${form.mailingID}/resume-sending.action"/>
                            <c:set target="${itemActionsSettings}" property="2" value="${element}"/>

                            <c:set target="${element}" property="extraAttributes" value="data-action='resume-sending' data-link='${resumeSendingLink}'"/>
                            <c:set target="${element}" property="iconBefore" value="icon-paper-plane"/>
                            <c:set target="${element}" property="name">
                                <mvc:message code="mailing.ResumeDelivery"/>
                            </c:set>
                        </emm:instantiate>
                    </c:if>
                </c:when>

                <c:when test="${isIntervalMailing}">
                    <c:if test="${form.workStatus eq activeStatusKey}">
                        <emm:instantiate var="element" type="java.util.LinkedHashMap">
                            <c:url var="deactivateIntervalUrl" value="/mailing/send/${form.mailingID}/deactivate-interval.action"/>
                            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                            <c:set target="${element}" property="type" value="href"/>
                            <c:set target="${element}" property="url" value="${deactivateIntervalUrl}"/>
                            <c:set target="${element}" property="iconBefore" value="icon-state-alert"/>
                            <c:set target="${element}" property="name">
                                <mvc:message code="btndeactivate"/>
                            </c:set>
                        </emm:instantiate>
                    </c:if>

                    <c:if test="${(form.workStatus eq disableStatusKey or form.workStatus eq newStatusKey) and canSendOrActivateMailing and not form.hasDeletedTargetGroups}">
                        <c:url var="activateIntervalUrl" value="/mailing/send/activate-interval.action" />
                        <emm:instantiate var="element" type="java.util.LinkedHashMap">
                            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                            <c:set target="${element}" property="extraAttributes" value="data-form-target='#delivery-settings-form' data-form-url='${activateIntervalUrl}' data-form-submit" />
                            <c:set target="${element}" property="iconBefore" value="icon-paper-plane"/>
                            <c:set target="${element}" property="name">
                                <mvc:message code="button.Activate"/>
                            </c:set>
                        </emm:instantiate>
                    </c:if>
                </c:when>
            </c:choose>
        </emm:ShowByPermission>
    </emm:instantiate>
</c:if>
