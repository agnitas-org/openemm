<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria" %>
<%@ page import="org.agnitas.emm.core.commons.util.Constants" %>

<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="optimizationForm" type="com.agnitas.mailing.autooptimization.web.forms.ComOptimizationForm"--%>

<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateFormatPattern" type="java.lang.String"--%>

<c:set var="AO_CRITERIA_CLICKRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE%>"/>
<c:set var="AO_CRITERIA_OPENRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE%>"/>
<c:set var="AO_CRITERIA_REVENUE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE%>"/>

<script type="text/javascript">
    $(function() {
        var activeGroups = new Array('group1','group2');
        var baseURL = '${pageContext.request.contextPath}/optimize_ajax.do';
        var companyID = ${optimizationForm.optimization.companyID};
        var campaignID = ${optimizationForm.optimization.campaignID};
        var optimizationID = ${optimizationForm.optimization.id};
        var mailtracking = ${mailtracking};
        var sessionID = '${sessionID}';
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
                url: baseURL+'?companyID='+companyID+'&campaignID='+campaignID+'&method=splits&splitType='+selectedValue
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

<agn:agnForm action="/optimize" id="optimizationForm" data-form="resource">
	<html:hidden property="optimizationID" />
	<html:hidden property="campaignID" />
	<html:hidden property="companyID" />
    <html:hidden property="previousAction" value="2"/>    <%--Because StrutsActionBase.ACTION_VIEW = 2--%>
	<input type="hidden" name="splitSize" id="splitSize"/>
    <html:hidden styleId="target-expression" property="targetExpression" />
	<input type="hidden" name="method" />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="mailing.autooptimization.edit" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name"><bean:message key="default.Name"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="mailing_name" styleClass="form-control" property="shortname" maxlength="99" size="42"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name"><bean:message key="default.description"/></label>
                </div>
                <div class="col-sm-8">
                    <html:textarea styleId="mailing_description" styleClass="form-control" property="description" rows="5" cols="32"/>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.settings" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-8 col-sm-push-4">
                    <div class="well block"><bean:message key="campaign.autoopt.overwrite.hint" /></div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="Mailinglist" /></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="mailinglistID" size="1" styleClass="form-control js-select"
                                 disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                        <logic:iterate id="mailingList" collection="${mailingLists}">
                            <html:option value="${mailingList.id}">${mailingList.shortname}</html:option>
                        </logic:iterate>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="Target" /></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <html:select property="targetID" size="1" styleClass="form-control js-select" styleId="targets" disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                                <logic:iterate id="target" collection="${targets}">
                                    <html:option value="${target.id}">${target.targetName}</html:option>
                                </logic:iterate>
                            </html:select>
                        </div>
                        <div class="input-group-btn">
                            <logic:equal name="optimizationForm" property="status" value="0">
                                <button id="addTarget" type="button" class="btn btn-regular" data-form-set="method: save" data-form-submit>
                                    <i class="icon icon-plus"></i>
                                </button>
                            </logic:equal>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <ul id="chosen-targets" class="list-group">
                        <logic:iterate collection="${chosenTargets}" id="aTarget" length="100">
                            <li class="list-group-item">
                                <span id="atarget_${aTarget.id}" class="text <c:if test="${aTarget.deleted != 0}">warning</c:if>">${aTarget.targetName}</span>
                                <logic:equal name="optimizationForm" property="status" value="0">
                                    <div class="list-group-item-controls">
                                        <button type="button" class="" id="targetID_${aTarget.id}" data-form-set="method: save" data-form-submit>
                                            <i class="icon icon-times-circle"></i>
                                        </button>
                                    </div>
                                </logic:equal>
                            </li>
                        </logic:iterate>
                        <li class="list-group-item disabled <logic:notEmpty name="optimizationForm" property="targetExpression">hidden</logic:notEmpty>">
                            <span class="text"><bean:message key="statistic.all_subscribers"/></span>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label for="checkbox_target_mode" id="checkbox_target_mode_label">
                            <html:checkbox styleId="checkbox_target_mode" property="targetMode" value="1"/>
                            <bean:message key="mailing.targetmode.and"/>
                        </label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="campaign.autoopt.evaltype" /></label>
                </div>
                <div class="col-sm-8">
                    <html:select styleClass="form-control js-select" property="evalType" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}">
                        <html:option value="${AO_CRITERIA_CLICKRATE}">
                            <bean:message key="campaign.autoopt.evaltype.clicks" />
                        </html:option>
                        <html:option value="${AO_CRITERIA_OPENRATE}">
                            <bean:message key="campaign.autoopt.evaltype.open" />
                        </html:option>
                        <emm:ShowByPermission token="stats.revenue">
                            <html:option value="${AO_CRITERIA_REVENUE}">
                                <bean:message key="statistic.revenue" />
                            </html:option>
                        </emm:ShowByPermission>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="mailing.autooptimization.threshold" /></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="thresholdString" readonly="${optimizationForm.status != STATUS_NOT_STARTED}" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label>
                            <html:checkbox property="doublechecking" disabled="${optimizationForm.status != STATUS_NOT_STARTED}"/>
                            <bean:message key="doublechecking.email" />
                        </label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="mailing.listsplit"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnSelect styleClass="form-control js-select" property="splitType" size="1" disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="splitType" data-field-vis="">
                        <logic:iterate id="splitType" collection="${splitTypes}">
                            <c:choose>
                                <c:when test="${not empty splitType[1]}">
                                    <agn:agnOption value="${splitType[0]}" key="${splitType[1]}" data-field-vis-hide=""/>
                                </c:when>
                                <c:otherwise>
                                    <agn:agnOption value="${splitType[0]}" data-field-vis-hide="">${splitType[2]}</agn:agnOption>
                                </c:otherwise>
                            </c:choose>
                        </logic:iterate>
                    </agn:agnSelect>
                </div>
            </div>
            <logic:equal name="optimizationForm" property="status" value="0">
                <div class="form-group" id="group1Div">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="campaign.autoopt.testgroup"/> 1</label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="group1" size="1"	disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="group1">
                            <html:option value="0">---</html:option>
                            <logic:iterate id="group" collection="${groups}">
                                <html:option value="${group.value}">${group.text}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                </div>
                <div class="form-group" id="group2Div">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="campaign.autoopt.testgroup"/> 2</label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="group2" size="1"	disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="group2">
                            <html:option value="0">---</html:option>
                            <logic:iterate id="group" collection="${groups}">
                                <html:option value="${group.value}">${group.text}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                </div>
                <div class="form-group" id="group3Div">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="campaign.autoopt.testgroup"/> 3</label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="group3" size="1"	disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="group3">
                            <html:option value="0">---</html:option>
                            <logic:iterate id="group" collection="${groups}">
                                <html:option value="${group.value}">${group.text}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                </div>
                <div class="form-group" id="group4Div">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="campaign.autoopt.testgroup"/> 4</label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="group4" size="1"	disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="group4">
                            <html:option value="0">---</html:option>
                            <logic:iterate id="group" collection="${groups}">
                                <html:option value="${group.value}">${group.text}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                </div>
                <div class="form-group" id="group5Div">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="campaign.autoopt.testgroup"/> 5</label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="group5" size="1"	disabled="${optimizationForm.status != STATUS_NOT_STARTED}" styleId="group5">
                            <html:option value="0">---</html:option>
                            <logic:iterate id="group" collection="${groups}">
                                <html:option value="${group.value}">${group.text}</html:option>
                            </logic:iterate>
                        </html:select>
                    </div>
                </div>
            </logic:equal>
            <logic:notEqual name="optimizationForm" property="status" value="0">
                <html:hidden property="group1" />
                <html:hidden property="group2" />
                <html:hidden property="group3" />
                <html:hidden property="group4" />
                <html:hidden property="group5" />

                <html:hidden property="mailinglistID" />
                <html:hidden property="splitType" />
                <html:hidden property="targetID" />
                <html:hidden property="evalType" />
            </logic:notEqual>
            <c:if test="${optimizationForm.optimizationID != 0}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="default.status" /></label>
                    </div>
                    <div class="col-sm-8">
                        <p class="form-control-static">
                            <c:choose>
                                <c:when test="${optimizationForm.status == STATUS_NOT_STARTED}">
                                    <bean:message key="mailing.autooptimization.status.not_started" />
                                </c:when>
                                <c:when	test="${optimizationForm.status ==  STATUS_TEST_SEND || optimizationForm.status == STATUS_EVAL_IN_PROGRESS}">
                                    <bean:message key="optimization.status.running" />
                                </c:when>
                                <c:when test="${optimizationForm.status == STATUS_FINISHED}">
                                    <bean:message key="mailing.autooptimization.status.finished" />
                                </c:when>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-sm-8 col-sm-push-4">
                    <div class="well block"><bean:message key="mailing.autooptimization.info"/></div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="testMailingsSendDate"><bean:message key="mailing.autooptimization.testmailingssenddate"/>:</label>
                </div>
                <fmt:parseDate var="sendDateTime" value="${optimizationForm.testMailingsSendDateAsString}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                <input type="hidden" name="testMailingsSendDateAsString" value="${optimizationForm.testMailingsSendDateAsString}"/>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendDate" value="${sendDateTime}" pattern="${adminDateFormatPattern}" timeZone="${adminTimeZone}"/>
                            <input id="testMailingsSendDate" type="text" class="form-control datepicker-input js-datepicker" value="${sendDate}"
                                   data-datepicker-options="format: '${fn:toLowerCase(adminDateFormatPattern)}', min: true"/>
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
                    <label class="control-label" for="resultSendDate"><bean:message key="mailing.autooptimization.resultsenddate"/>:</label>
                </div>
                <fmt:parseDate var="sendDateTime" value="${optimizationForm.resultSendDateAsString}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                <input type="hidden" name="resultSendDateAsString" value="${optimizationForm.resultSendDateAsString}"/>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <fmt:formatDate var="sendDate" value="${sendDateTime}" pattern="${adminDateFormatPattern}" timeZone="${adminTimeZone}"/>
                            <input id="resultSendDate" type="text" class="form-control datepicker-input js-datepicker" value="${sendDate}"
                                   data-datepicker-options="format: '${fn:toLowerCase(adminDateFormatPattern)}', min: true"/>
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
                <h2 class="headline"><bean:message key="default.status"/></h2>
            </div>
            <div class="tile-content">
                <c:url var="optimizationStatusViewLink" value="/optimize_ajax.do">
                    <c:param name="optimizationID" value="${optimizationForm.optimizationID}"/>
                    <c:param name="companyID" value="${optimizationForm.companyID}"/>
                    <c:param name="method" value="getStats"/>
                    <c:param name="mailtracking" value="${mailtracking}"/>
                </c:url>

                <div class="table-wrapper" data-load="${optimizationStatusViewLink}" data-load-interval="15000"></div>
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
</agn:agnForm>

<script data-initializer="campaign-autooptimization-view" type="application/json">
    {
        "datePattern": "${fn:toLowerCase(adminDateFormat)}"
    }
</script>
