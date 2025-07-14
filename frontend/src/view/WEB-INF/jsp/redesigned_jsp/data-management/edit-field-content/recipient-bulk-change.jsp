<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.util.DbColumnType" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

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

<mvc:form id="recipientBulkForm" servletRelativeAction="/recipient/bulkView.action" modelAttribute="recipientBulkForm"
		  cssClass="tiles-container flex-column" data-form="resource" data-controller="edit-field-content">

	<div class="tile h-auto flex-none">
		<div class="tile-header">
			<h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
		</div>
		<div class="tile-body">
			<div class="row g-3">
				<div class="col">
					<label for="mailinglist" class="form-label"><mvc:message code="Mailinglist" /></label>
					<mvc:select id="mailinglist" path="mailinglistId" size="1" cssClass="form-control js-select">
						<mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
						<c:if test="${not hasAnyDisabledMailingLists}">
							<mvc:option value="-1"><mvc:message code="No_Mailinglist"/></mvc:option>
						</c:if>
						<c:forEach items="${mailingLists}" var="mailinglist">
							<mvc:option value="${mailinglist.id}">${mailinglist.shortname} (${mailinglist.id})</mvc:option>
						</c:forEach>
					</mvc:select>
				</div>

				<div class="col">
					<label for="target-group" class="form-label"><mvc:message code="Target" /></label>
					<mvc:select id="target-group" path="targetId" size="1" cssClass="form-control js-select">
						<mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
						<c:forEach items="${targetGroups}" var="targetGroup">
							<mvc:option value="${targetGroup.id}">${targetGroup.targetName} (${targetGroup.id})</mvc:option>
						</c:forEach>
					</mvc:select>
				</div>

				<div class="col">
					<label class="form-label"><mvc:message code="report.numberRecipients"/></label>
					<input id="recipients-count" type="text" value="${calculatedRecipients}" class="form-control" disabled>
				</div>

				<div class="col-auto d-flex align-items-end">
					<button type="button" class="btn btn-primary" data-action="calculateRecipients">
						<i class="icon icon-play-circle"></i>
						<span class="text"><mvc:message code="button.filter.recipients"/></span>
					</button>
				</div>
			</div>
		</div>
	</div>

	<div class="tile">
		<div class="tile-body">
			<div class="table-wrapper">
				<div class="table-wrapper__header">
					<h1 class="table-wrapper__title"><mvc:message code="Values" /></h1>
					<div class="table-wrapper__controls">
						<%@include file="../../common/table/toggle-truncation-btn.jspf" %>
						<jsp:include page="../../common/table/entries-label.jsp">
							<jsp:param name="totalEntries" value="${fn:length(recipientColumns)}"/>
						</jsp:include>
					</div>
				</div>

				<div class="table-wrapper__body">
					<emm:table var="column" modelAttribute="recipientColumns" cssClass="table table--borderless js-table">

						<emm:column titleKey="settings.FieldName" sortable="true" property="column" />

						<emm:column titleKey="default.Type">
							<c:set var="columnDataType" value="${column.simpleDataType}"/>
							<%--@elvariable id="columnDataType" type="com.agnitas.util.DbColumnType.SimpleDataType"--%>
							<span>
								<mvc:message code="${columnDataType.messageKey}"/> ${columnDataType == SIMPLE_DATE_TYPE ? localeDateHint: columnDataType == DATETIME_TYPE ? localeDateTimeHint : ''}
							</span>
						</emm:column>

						<emm:column titleKey="recipient.history.newvalue">
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
						</emm:column>

						<emm:column headerClass="fit-content" titleKey="EmptyField">
							<div class="form-check form-switch">
								<mvc:checkbox path="recipientFieldChanges[${column.column}].clear" cssClass="form-check-input" value="on" role="switch"/>
							</div>
						</emm:column>
					</emm:table>
				</div>

				<div class="table-wrapper__footer"></div>
			</div>
		</div>
	</div>
</mvc:form>
