<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.beans.TargetLight" %>
<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="splitId" type="java.lang.Integer"--%>
<%--@elvariable id="wmSplit" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="SHOW_TARGET_MODE_TOGGLE" type="java.lang.Boolean"--%>
<%--@elvariable id="TARGET_MODE_TOGGLE_DISABLED" type="java.lang.Boolean"--%>
<%--@elvariable id="splitBaseMessage" type="java.lang.String"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="splitPartMessage" type="java.lang.String"--%>
<%--@elvariable id="splitTargets" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="splitTargetsForSplitBase" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND %>"/>
<c:set var="LIST_SPLIT_PREFIX" value="<%= TargetLight.LIST_SPLIT_PREFIX %>"/>
<c:set var="INTERVAL_MAILING_TYPE" value="<%= MailingType.INTERVAL %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>

<script type="text/javascript">
    (function(){
        var splits = {};
        var splitIndex;

        <c:set var="aktName" value=""/>
        <c:forEach var="target" items="${splitTargets}">
            <c:set var="aNamePage" value="${target.targetName}"/>

            <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX), fn:length(aNamePage))}"/>
            <c:set var="aNameBase" value="${fn:substring(aNameCode, 0, fn:indexOf(aNameCode, '_'))}"/>
            <c:set var="aNamePart" value="${fn:substring(aNameCode, fn:indexOf(aNameCode, '_') + 1, fn:length(aNameCode))}"/>

            <c:choose>
                <c:when test="${fn:contains(aNameBase, '.')}">
                    <c:set var="aLabel" value=""/>

                    <fmt:parseNumber var="aSplitPieceIndex" value="${aNamePart}" type="number" integerOnly="true"/>
                    <fmt:parseNumber var="aSplitPiece" value="${fn:split(aNameBase, ';')[aSplitPieceIndex - 1]}" type="number" integerOnly="true"/>
                    <c:set var="aLabel" value="${aNamePart}. ${aSplitPiece}%"/>
                </c:when>
                <c:otherwise>
                    <c:set var="aLabel"><mvc:message code="listsplit.${aNameBase}.${aNamePart}"/></c:set>
                </c:otherwise>
            </c:choose>

            // aLabel: ${aLabel}

            splitIndex = '${aNameBase}';
            splits[splitIndex] = $.extend(splits[splitIndex], {
                '${aNamePart}': '${aLabel}'
            });
            </c:if>
        </c:forEach>

        AGN.Opt.MailingBaseFormSplits = splits;
    })();
</script>

<div id="mailingTargets" class="tile" data-action="change-mailing-settings">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingTargets">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="Targets"/>
        </a>
    </div>
    <div id="tile-mailingTargets" class="tile-content tile-content-forms" data-action="scroll-to">
        <%@include file="../fragments/mailing-settings-targets-select.jspf"%>

		<c:if test="${SHOW_TARGET_MODE_TOGGLE}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="yes-no-btn" data-action="change-target-mode">
                        <mvc:radiobutton path="targetMode" value="${TARGET_MODE_AND}" id="targetModeAndBtn" disabled="${TARGET_MODE_TOGGLE_DISABLED}"/>
                        <label for="targetModeAndBtn" class="yes-no-btn-left">AND</label>
                        <mvc:radiobutton path="targetMode" value="${TARGET_MODE_OR}" id="targetModeOrBtn" disabled="${TARGET_MODE_TOGGLE_DISABLED}"/>
                        <label for="targetModeOrBtn" class="yes-no-btn-right">OR</label>
                   	</div>
                    <span id="target-mode-desc" class="text" style="margin-left: 10px;">
                        <%-- loads by js--%>
                    </span>
                </div>
            </div>
        </c:if>
        
        <c:if test="${mailingId > 0}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <p class="help-block">
                        <mvc:message code="report.numberRecipients"/>:
                        <strong id="calculatedRecipientsBadge">?</strong>
                    </p>
                    <button type="button" class="btn btn-regular" data-action="calculateRecipients">
                        <span><mvc:message code="button.Calculate"/></span>
                    </button>
                </div>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${isTemplate}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label checkbox-control-label" for="needsTargetToggle">
                            <mvc:message code="mailing.needsTarget"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <c:set var="isNeedsTargetCheckboxDisabled" value="${not MAILING_EDITABLE or mailingSettingsForm.mailingType eq DATEBASED_MAILING_TYPE or  mailingSettingsForm.mailingType eq INTERVAL_MAILING_TYPE}"/>
                        <label class="toggle">
                            <mvc:checkbox path="needsTarget" id="needsTargetToggle" disabled="${isNeedsTargetCheckboxDisabled}"/>
                            <div class="toggle-control"></div>
                        </label>
                        <c:if test="${isNeedsTargetCheckboxDisabled}">
                            <mvc:hidden path="needsTarget"/>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <mvc:hidden path="needsTarget"/>
            </c:otherwise>
        </c:choose>

        <div class="form-group" data-field="double-select" data-provider="opt" data-provider-src="MailingBaseFormSplits">
            <div class="col-sm-4">
                <label class="control-label">
                    <label for="settingsTargetgroupsListSplit"><mvc:message code="mailing.listsplit"/></label>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/ListSplitMsg.xml" tabindex="-1" type="button"></button>
                </label>
            </div>

            <div class="col-sm-8">
                <mvc:select path="splitBase" id="settingsTargetgroupsListSplit"
                            cssClass="form-control js-double-select-trigger"
                            disabled="${not MAILING_EDITABLE or wmSplit or workflowId gt 0}">
                    <c:if test="${splitId eq -1}">
                        <mvc:option value="yes"><mvc:message code="default.Yes"/></mvc:option>
                    </c:if>
                    <mvc:option value="none"><mvc:message code="listsplit.none"/></mvc:option>

                    <c:set var="aktName" value=""/>
                    <c:forEach var="target" items="${splitTargets}" end="500">
                        <c:set var="aNamePage" value="${target.targetName}"/>

                        <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
                            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX), fn:length(aNamePage))}"/>
                            <c:set var="aNameBase" value="${fn:substring(aNameCode, 0, fn:indexOf(aNameCode, '_'))}"/>
                            
                            <c:choose>
                                <c:when test="${fn:contains(aNameBase, '.')}">
                                    <c:set var="aLabel" value=""/>
                                    <c:forEach var="aSplitPiece" items="${fn:split(aNameBase, ';')}" varStatus="status">
                                        <fmt:parseNumber var="aSplitPiece" value="${aSplitPiece}" type="number" integerOnly="true"/>
                                        <c:set var="aLabel" value="${aLabel}${aSplitPiece}%"/>
                                        <c:if test="${not status.last}">
                                            <c:set var="aLabel" value="${aLabel} / "/>
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="aLabel"><mvc:message code="listsplit.${aNameBase}"/></c:set>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${aNameBase ne aktName}">
                                <c:set var="aktName" value="${aNameBase}"/>
                                <mvc:option value="${aNameBase}">${aLabel}</mvc:option>
                            </c:if>
                        </c:if>
                    </c:forEach>
                    <c:if test="${wmSplit}">
                        <mvc:option value="${mailingSettingsForm.splitBase}" selected="selected">${splitBaseMessage}</mvc:option>
                    </c:if>
                </mvc:select>
            </div>

            <div class="col-sm-offset-4 col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <mvc:select path="splitPart" id="settingsTargetgroupsListSplitPart" disabled="${not MAILING_EDITABLE or wmSplit or workflowId gt 0}"
                                     cssClass="form-control js-double-select-target">
                            <c:choose>
                                <c:when test="${wmSplit}">
                                    <mvc:option value="${mailingSettingsForm.splitPart}">${splitPartMessage}</mvc:option>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="target" items="${splitTargetsForSplitBase}" end="500">
                                        <c:set var="aNamePage" value="${target.targetName}"/>
                                        <c:if test="${fn:startsWith(aNamePage, LIST_SPLIT_PREFIX)}">
                                            <c:set var="aNameCode" value="${fn:substring(aNamePage, fn:length(LIST_SPLIT_PREFIX) + 1, fn:length(aNamePage))}"/>
                                            <c:set var="aNamePart" value="${fn:substring(aNameCode, fn:indexOf(aNameCode, '_') + 1, fn:length(aNameCode))}"/>
                                            <c:if test="${splitId eq 0}">
                                                <c:set var="aNamePart" value=''/>
                                            </c:if>
                                            <mvc:option value="${aNamePart}">
                                                <mvc:message code="listsplit.${mailingSettingsForm.splitBase}.${aNamePart}"/>
                                            </mvc:option>
                                        </c:if>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </mvc:select>
                    </div>
                    <%@include file="../fragments/edit-with-campaign-btn.jspf" %>
                </div>
            </div>
        </div>
    </div>
</div>
