<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="optimizationForm" type="com.agnitas.mailing.autooptimization.form.OptimizationForm"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<c:set var="AO_CRITERIA_CLICKRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE%>"/>
<c:set var="AO_CRITERIA_OPENRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE%>"/>
<c:set var="AO_CRITERIA_REVENUE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE%>"/>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<script type="text/javascript">
    $(function() {
        var activeGroups = ['group1','group2'];
        var baseURL = AGN.url('/optimization/ajax/splits.action');
        var targets = $('#targets').select2();
        var targetExpressionElem = $('#target-expression');
        var targetExpression = targetExpressionElem.val();
        var targetsElem = targetExpression.split(',');
        var prevTarget = targets.select2('val');
        var addTarget = $('#addTarget');
        var currentTarget = $('#chosen-targets');
        var listSplit = $('#splitType').select2();
        var selectedValue = listSplit.select2('val');

        addTarget.click(function() {
            targetsElem.push(prevTarget);
            targetExpressionElem.val(targetsElem.toString());
        });

        targets.change(function() {
            var targetID = targets.select2('data').id;
            if (prevTarget != null || prevTarget != 'undefined') {
                var index = $.inArray(prevTarget, targetsElem);
                targetsElem.splice(index, 1);
                targetExpressionElem.val(targetsElem.toString());
            }
            targetsElem.push(targetID);
            targetExpressionElem.val(targetsElem.toString());
            prevTarget = targetID;
        });

        $('button', currentTarget).on('click', function() {
            var targetId = $(this).attr('id');
            targetId = targetId.replace('targetID_', '');
            var index = $.inArray(targetId, targetsElem);
            targetsElem.splice(index, 1);
            targetExpressionElem.val(targetsElem.toString());
        });

        getTargets();

        listSplit.change(function() {
            selectedValue = listSplit.select2('data').id;
            getTargets();
        });

        function getTargets() {
            $.ajax({
                type: 'GET',
                url: baseURL + '?splitType=' + selectedValue + '&cacheKiller=${emm:milliseconds()}'
            }).done(function(response) {
                activeGroups = response.split(';');
                showTargets();
            });
        }

        function showTargets() {
            for (var i = 3; i < 6; i++) {
                $('#group' + i + 'Div').hide();
                if( activeGroups.indexOf( 'group' + i) == -1) {
                    $('#group' + i).selectedIndex = 0;
                }
            }
            var numActiveGroups = 0;
            for (var index = 0; index < activeGroups.length; index++) {
                var activeGroup = $('#' + activeGroups[index]+'Div');
                if (activeGroup != null) {
                    activeGroup.show();
                    numActiveGroups++;
                }
            }
            $('#splitSize').val(numActiveGroups);
        }
    });
</script>

<mvc:form servletRelativeAction="/optimization/save.action" modelAttribute="optimizationForm" data-form="resource" method="POST">
	<mvc:hidden path="optimizationID" />
	<mvc:hidden path="campaignID" />
	<mvc:hidden path="companyID" />
	<mvc:hidden path="campaignName" />
	<input type="hidden" name="splitSize" id="splitSize"/>
    <mvc:hidden id="target-expression" path="targetExpression" />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="mailing.autooptimization.edit" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name">
                        <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
                        ${nameMsg}
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" id="mailing_name" cssClass="form-control" maxlength="99" size="42" placeholder="${nameMsg}" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name">
                        <c:set var="descriptionMsg"><mvc:message code="default.description"/></c:set>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="mailing_description" cssClass="form-control" rows="5" cols="32" placeholder="${descriptionMsg}" />
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.settings" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-8 col-sm-push-4">
                    <div class="well block"><mvc:message code="campaign.autoopt.overwrite.hint" /></div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="Mailinglist" /></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="mailinglistID" size="1" cssClass="form-control js-select" disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                        <c:forEach var="mailingList" items="${mailingLists}">
                            <mvc:option value="${mailingList.id}">${mailingList.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="Target" /></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:select path="targetID" size="1" cssClass="form-control js-select" id="targets" disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                                <c:forEach items="${targets}" var="target">
                                    <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                        <div class="input-group-btn">
                            <c:if test="${optimizationForm.status eq 0}">
                                <button id="addTarget" type="button" class="btn btn-regular" data-form-submit>
                                    <i class="icon icon-plus"></i>
                                </button>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <ul id="chosen-targets" class="list-group">
                        <c:forEach items="${chosenTargets}" var="aTarget" begin="0" end="100">
                            <li class="list-group-item">
                                <span id="atarget_${aTarget.id}" class="text <c:if test="${aTarget.deleted != 0}">warning</c:if>">${aTarget.targetName}</span>
                                <c:if test="${optimizationForm.status eq 0}">
                                    <div class="list-group-item-controls">
                                        <button type="button" class="" id="targetID_${aTarget.id}" data-form-submit>
                                            <i class="icon icon-times-circle"></i>
                                        </button>
                                    </div>
                                </c:if>
                            </li>
                        </c:forEach>
                        <li class="list-group-item disabled <c:if test="${not empty optimizationForm.targetExpression}">hidden</c:if>">
                            <span class="text"><mvc:message code="statistic.all_subscribers"/></span>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label for="checkbox_target_mode" id="checkbox_target_mode_label">
                            <mvc:checkbox path="targetMode" id="checkbox_target_mode" value="1" />
                            <mvc:message code="mailing.targetmode.and"/>
                        </label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="campaign.autoopt.evaltype" /></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="evalType" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                        <mvc:option value="${AO_CRITERIA_CLICKRATE}">
                            <mvc:message code="campaign.autoopt.evaltype.clicks" />
                        </mvc:option>
                        <mvc:option value="${AO_CRITERIA_OPENRATE}">
                            <mvc:message code="campaign.autoopt.evaltype.open" />
                        </mvc:option>
                        <emm:ShowByPermission token="stats.revenue">
                            <mvc:option value="${AO_CRITERIA_REVENUE}">
                                <mvc:message code="statistic.revenue" />
                            </mvc:option>
                        </emm:ShowByPermission>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="mailing.autooptimization.threshold" /></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text cssClass="form-control" path="thresholdString" readonly="${optimizationForm.status != STATUS_NOT_STARTED}" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label>
                            <mvc:checkbox path="doublechecking" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" />
                            <mvc:message code="doublechecking.email" />
                        </label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="mailing.listsplit"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="splitType" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="splitType" data-field-vis="">
                        <c:forEach var="splitType" items="${splitTypes}">
                            <c:choose>
                                <c:when test="${not empty splitType[1]}">
                                    <mvc:option value="${splitType[0]}" data-field-vis-hide=""><mvc:message code="${splitType[1]}"/></mvc:option>
                                </c:when>
                                <c:otherwise>
                                    <mvc:option value="${splitType[0]}" data-field-vis-hide=""><mvc:message code="${splitType[2]}"/></mvc:option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
                <c:if test="${optimizationForm.status eq 0}">
                <div class="form-group" id="group1Div">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="campaign.autoopt.testgroup"/> 1</label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="group1" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="group1">
                            <mvc:option value="0">---</mvc:option>
                            <c:forEach var="group" items="${groups}">
                                <mvc:option value="${group.value}">${group.text}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
                <div class="form-group" id="group2Div">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="campaign.autoopt.testgroup"/> 2</label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="group2" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="group2">
                            <mvc:option value="0">---</mvc:option>
                            <c:forEach var="group" items="${groups}">
                                <mvc:option value="${group.value}">${group.text}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
                <div class="form-group" id="group3Div">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="campaign.autoopt.testgroup"/> 3</label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="group3" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="group3">
                            <mvc:option value="0">---</mvc:option>
                            <c:forEach var="group" items="${groups}">
                                <mvc:option value="${group.value}">${group.text}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
                <div class="form-group" id="group4Div">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="campaign.autoopt.testgroup"/> 4</label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="group4" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="group4">
                            <mvc:option value="0">---</mvc:option>
                            <c:forEach var="group" items="${groups}">
                                <mvc:option value="${group.value}">${group.text}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
                <div class="form-group" id="group5Div">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="campaign.autoopt.testgroup"/> 5</label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="group5" cssClass="form-control js-select" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" id="group5">
                            <mvc:option value="0">---</mvc:option>
                            <c:forEach var="group" items="${groups}">
                                <mvc:option value="${group.value}">${group.text}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            </c:if>

            <c:if test="${optimizationForm.status ne 0}">
                <mvc:hidden path="group1" />
                <mvc:hidden path="group2" />
                <mvc:hidden path="group3" />
                <mvc:hidden path="group4" />
                <mvc:hidden path="group5" />

                <mvc:hidden path="mailinglistID" />
                <mvc:hidden path="splitType" />
                <mvc:hidden path="targetID" />
                <mvc:hidden path="evalType" />
            </c:if>

            <c:if test="${optimizationForm.optimizationID != 0}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="default.status" /></label>
                    </div>
                    <div class="col-sm-8">
                        <p class="form-control-static">
                            <c:choose>
                                <c:when test="${optimizationForm.status == STATUS_NOT_STARTED}">
                                    <mvc:message code="mailing.autooptimization.status.not_started" />
                                </c:when>
                                <c:when	test="${optimizationForm.status ==  STATUS_TEST_SEND || optimizationForm.status == STATUS_EVAL_IN_PROGRESS}">
                                    <mvc:message code="optimization.status.running" />
                                </c:when>
                                <c:when test="${optimizationForm.status == STATUS_FINISHED}">
                                    <mvc:message code="mailing.autooptimization.status.finished" />
                                </c:when>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-sm-8 col-sm-push-4">
                    <div class="well block"><mvc:message code="mailing.autooptimization.info"/></div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="testMailingsSendDate"><mvc:message code="mailing.autooptimization.testmailingssenddate"/>:</label>
                </div>
                <fmt:parseDate var="sendDateTime" value="${optimizationForm.testMailingsSendDateAsString}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                <input type="hidden" name="testMailingsSendDateAsString" value="${optimizationForm.testMailingsSendDateAsString}"/>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendDate" value="${sendDateTime}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>
                            <input id="testMailingsSendDate" type="text" class="form-control datepicker-input js-datepicker" value="${sendDate}"
                                   data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', min: true"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendTime" value="${sendDateTime}" pattern="HH:mm"/>
                            <input id="testMailingsSendTime" type="text" class="form-control js-timepicker" value="${sendTime}" data-timepicker-options="mask: 'h:s'"/>
                        </div>
                        <div class="input-group-addon">
                            <span class="addon">
                                <i class="icon icon-clock-o"></i>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="resultSendDate"><mvc:message code="mailing.autooptimization.resultsenddate"/>:</label>
                </div>
                <fmt:parseDate var="sendDateTime" value="${optimizationForm.resultSendDateAsString}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                <input type="hidden" name="resultSendDateAsString" value="${optimizationForm.resultSendDateAsString}"/>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendDate" value="${sendDateTime}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>
                            <input id="resultSendDate" type="text" class="form-control datepicker-input js-datepicker" value="${sendDate}" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', min: true"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendTime" value="${sendDateTime}" pattern="HH:mm" timeZone="${adminTimeZone}"/>
                            <input id="resultSendTime" type="text" class="form-control js-timepicker" value="${sendTime}" data-timepicker-options="mask: 'h:s'"/>
                        </div>
                        <div class="input-group-addon">
                            <span class="addon">
                                <i class="icon icon-clock-o"></i>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <c:if test="${optimizationForm.status ne STATUS_NOT_STARTED}">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="default.status"/></h2>
            </div>
            <div class="tile-content">
                <c:url var="optimizationStatisticViewLink" value="/optimization/ajax/${optimizationForm.optimizationID}/load-statistic.action">
                    <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                </c:url>

                <div class="table-wrapper" data-load="${optimizationStatisticViewLink}" data-load-interval="15000"></div>
            </div>
        </div>
    </c:if>

    <c:if test="${!(optimizationForm.status == STATUS_NOT_STARTED)}">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"></h2>
            </div>
            <div class="tile-content">
                <iframe src="${optimizationForm.reportUrl}" border="0" scrolling="auto" width="100%" height="600px" frameborder="0">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>
    </c:if>
</mvc:form>

<script data-initializer="campaign-autooptimization-view" type="application/json">
    {
        "datePattern": "${fn:toLowerCase(adminDateFormat)}"
    }
</script>
