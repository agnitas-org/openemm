<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ page import="org.agnitas.util.DbColumnType" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="recipientBulkForm" type="com.agnitas.emm.core.recipient.forms.RecipientBulkForm"--%>
<%--@elvariable id="mailingLists" type="java.util.List"--%>
<%--@elvariable id="targetGroups" type="java.util.List"--%>
<%--@elvariable id="hasAnyDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="calculatedRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="column" type="com.agnitas.beans.ProfileField"--%>

<c:set var="SIMPLE_DATE_TYPE" value="<%= DbColumnType.SimpleDataType.Date %>"/>
<c:set var="DATETIME_TYPE" value="<%= DbColumnType.SimpleDataType.DateTime %>"/>
<c:set var="localeDateHint" value="(${localeDatePattern})"/>
<c:set var="localeDateTimeHint" value="(${localeDateTimePattern})"/>

<mvc:form servletRelativeAction="/recipient/bulkView.action"
          modelAttribute="recipientBulkForm"
          id="recipientBulkForm"
          data-form="resource">

    <div class="tile">
        <div class="tile-header">
			<a href="#" class="headline" data-toggle-tile="#tile-changes-common-settings">
				<i class="tile-toggle icon icon-angle-up"></i>
				<mvc:message code="report.mailing.filter" />
			</a>
		</div>

        <div id="tile-changes-common-settings" class="tile-content tile-content-forms">
			<div class="form-group">
				<div class="col-sm-2">
					<label class="control-label"><mvc:message code="Mailinglist" /></label>
				</div>
                <div class="col-sm-10">
                    <mvc:select path="mailinglistId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                        <c:if test="${not hasAnyDisabledMailingLists}">
                            <mvc:option value="-1"><mvc:message code="No_Mailinglist"/></mvc:option>
                        </c:if>
                        <c:forEach items="${mailingLists}" var="mailinglist">
                            <mvc:option value="${mailinglist.id}">${mailinglist.shortname} (${mailinglist.id})</mvc:option>
                        </c:forEach>
                    </mvc:select>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-2">
					<label class="control-label"><mvc:message code="Target" /></label>
				</div>
				<div class="col-sm-10">
                    <mvc:select path="targetId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <c:forEach items="${targetGroups}" var="targetGroup">
                            <mvc:option value="${targetGroup.id}">${targetGroup.targetName} (${targetGroup.id})</mvc:option>
                        </c:forEach>
                    </mvc:select>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-2">
					<label class="control-label"><mvc:message code="report.numberRecipients"/></label>
				</div>
				<div class="col-sm-10">
					<label id="calculateRecipientsLabel" class="inline-tile inline-tile-footer">${calculatedRecipients}</label>
					<c:url var="calculateUrl" value="/recipient/calculate.action"/>
					<button type="button" class="btn btn-regular"
							data-action="calculateRecipients"
							data-form-url="${calculateUrl}"
							data-target-form="#recipientBulkForm">
						<span><mvc:message code="button.Calculate"/></span>
					</button>
				</div>
			</div>
		</div>
    </div>

	<div class="tile">
		<div class="tile-header">
			<a href="#" class="headline" data-toggle-tile="#tile-recipient-fields-changes">
				<i class="tile-toggle icon icon-angle-up"></i>
				<mvc:message code="Values" />
			</a>
		</div>
		<div id="tile-recipient-fields-changes" class="tile-content tile-content-forms">
			<div class="table-wrapper">
                <display:table class="table table-bordered table-striped js-table"
							   id="column" name="recipientColumns"
							   sort="page"
							   partialList="false"
							   excludedParams="*">

					 <%-- Prevent table controls/headers collapsing when the table is empty --%>
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

					<display:column headerClass="js-table-sort" class="field_shortname"
                                    titleKey="settings.FieldName"
                                    sortable="true" sortProperty="shortname">
						${column.column}
					</display:column>

					<display:column headerClass="js-table-sort" class="field_type"
                                    titleKey="default.Type"
                                    sortable="false">
						<c:set var="columnDataType" value="${column.simpleDataType}"/>
						<%--@elvariable id="columnDataType" type="org.agnitas.util.DbColumnType.SimpleDataType"--%>
                    	<mvc:message code="${columnDataType.messageKey}"/> ${columnDataType == SIMPLE_DATE_TYPE ? localeDateHint: columnDataType == DATETIME_TYPE ? localeDateTimeHint : ''}
                    </display:column>

					<display:column headerClass="js-table-sort" class="js-checkable field_newValue"
                                    titleKey="recipient.history.newvalue"
                                    sortable="false">
						<mvc:hidden path="recipientFieldChanges[${column.column}].shortname" value="${column.column}"/>
						<mvc:hidden path="recipientFieldChanges[${column.column}].type" value="${column.simpleDataType}"/>
                        <c:choose>
                            <c:when test="${not empty column.allowedValues}">
                                <mvc:select path="recipientFieldChanges[${column.column}].newValue" cssClass="form-control">
                                    <c:if test="${column.nullable}">
                                        <mvc:option value="">NULL</mvc:option>
                                    </c:if>
                                    <c:if test="${not empty column.defaultValue}">
                                        <mvc:option value="${column.defaultValue}">${column.defaultValue}</mvc:option>
                                    </c:if>
                                    <c:forEach items="${column.allowedValues}" var="fixedValue">
                                        <mvc:option value="${fixedValue}">${fixedValue}</mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </c:when>
                            <c:otherwise>
                                <mvc:text path="recipientFieldChanges[${column.column}].newValue" cssClass="form-control"/>
                            </c:otherwise>
                        </c:choose>
					</display:column>

					<display:column headerClass="js-table-sort" class="js-checkable field_isClear"
                                    titleKey="EmptyField"
                                    sortable="false">
						<label class="toggle">
							<mvc:checkbox path="recipientFieldChanges[${column.column}].clear" value="on"/>
							<div class="toggle-control"></div>
						</label>
					</display:column>

				</display:table>
            </div>
		</div>
	</div>

	<script type="text/javascript">
		AGN.Lib.Action.new({'click': '[data-action="calculateRecipients"]'}, function() {
          var label = $("#calculateRecipientsLabel");
          var btn = this.el;
          var form = AGN.Lib.Form.get($(btn.data('target-form')));

			$.ajax({
				url: btn.data('form-url'),
				data: {
				  "targetId": form.getValue("targetId"),
				  "mailinglistId": form.getValue("mailinglistId")
				}
			}).always(function(data) {
				var value = data.count || 0;
				label.text(value);
			}).fail(function(){
				label.text('?');
				AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
			})
		});
	</script>
</mvc:form>
