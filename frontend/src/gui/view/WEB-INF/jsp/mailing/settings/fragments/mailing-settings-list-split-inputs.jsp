<%@ page import="com.agnitas.beans.TargetLight" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="splitId" type="java.lang.Integer"--%>
<%--@elvariable id="wmSplit" type="java.lang.Boolean"--%>
<%--@elvariable id="splitBaseMessage" type="java.lang.String"--%>
<%--@elvariable id="splitPartMessage" type="java.lang.String"--%>
<%--@elvariable id="splitTargets" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="splitTargetsForSplitBase" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<c:set var="LIST_SPLIT_PREFIX" value="<%= TargetLight.LIST_SPLIT_PREFIX %>"/>

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

<div data-field="double-select" data-provider="opt" data-provider-src="MailingBaseFormSplits">
    <label for="settingsTargetgroupsListSplit" class="form-label">
        <mvc:message code="mailing.listsplit"/>
        <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/ListSplitMsg.xml"></a>
    </label>

    <mvc:select path="${param.splitBasePath}" id="settingsTargetgroupsListSplit"
                cssClass="form-control js-double-select-trigger"
                data-workflow-driven="${workflowDriven}"
                disabled="${param.disabled}">
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
            <mvc:option value="${param.splitBaseValue}" selected="selected">${splitBaseMessage}</mvc:option>
        </c:if>
    </mvc:select>

    <mvc:select path="${param.splitPartPath}" id="settingsTargetgroupsListSplitPart" disabled="${param.disabled}"
                cssClass="form-control js-double-select-target mt-2" data-workflow-driven="${workflowDriven}">
        <c:choose>
            <c:when test="${wmSplit}">
                <mvc:option value="${param.splitPartValue}">${splitPartMessage}</mvc:option>
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
                            <mvc:message code="listsplit.${param.splitBaseValue}.${aNamePart}"/>
                        </mvc:option>
                    </c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </mvc:select>
</div>
