<%@page import="com.agnitas.messages.Message"%>
<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.util.importvalues.ImportMode" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="mailinglists" type="java.util.Map<java.lang.String, com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="importError" type="java.lang.String"--%>
<%--@elvariable id="resultMLAdded" type="java.util.Map"--%>

<c:set var="mode" value="${importWizardSteps.helper.mode}"/>

<div class="tiles-container">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <c:choose>
                <c:when test="${empty importError}">
                    <c:if test="${importWizardSteps.helper.dbInsertStatus > 100}">
                        <c:forEach var="messageAndParameter" items="${importWizardSteps.dbInsertStatusMessagesAndParameters}">
                            <div class="panel panel--success">
                                <h2>
                                    <mvc:message code='<%= ((Message) pageContext.getAttribute("messageAndParameter")).getCode() %>' arguments='<%= ((Message) pageContext.getAttribute("messageAndParameter")).getArguments().length > 0 ? ((String) ((Message) pageContext.getAttribute("messageAndParameter")).getArguments()[0]) : "" %>'/>
                                </h2>
                            </div>
                        </c:forEach>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="panel panel--alert">
                        <h2>${importError}</h2>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${importWizardSteps.helper.dbInsertStatus >= 1000}">
                <div class="input-groups">
                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_email"/></span>
                        <input type="text" value="${status.getError('email')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_blacklist"/></span>
                        <input type="text" value="${status.getError('blacklist')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_double"/></span>
                        <input type="text" value="${status.getError('keyDouble')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_numeric"/></span>
                        <input type="text" value="${status.getError('numeric')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_mailtype"/></span>
                        <input type="text" value="${status.getError('mailtype')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_gender"/></span>
                        <input type="text" value="${status.getError('gender')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_date"/></span>
                        <input type="text" value="${status.getError('date')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="csv_errors_linestructure"/></span>
                        <input type="text" value="${status.getError('structure')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.csv_errors_invalidNullValues"/></span>
                        <input type="text" value="${status.invalidNullValues}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="error.import.value.large"/></span>
                        <input type="text" value="${status.getError('valueTooLarge')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="error.import.number.large"/></span>
                        <input type="text" value="${status.getError('numberTooLarge')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="error.import.invalidFormat"/></span>
                        <input type="text" value="${status.getError('invalidFormat')}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="error.import.missingMandatory"/></span>
                        <input type="text" value="${status.getError('missingMandatory')}" class="form-control" readonly>
                    </div>

                    <c:if test="${status.errorColumns.size() > 0}">
                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="error.import.errorColumns"/></span>
                            <input type="text" value="${fn:join(status.errorColumns.toArray(), ", ")}" class="form-control" readonly>
                        </div>
                    </c:if>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.result.filedataitems"/></span>
                        <input type="text" value="${status.csvLines}" class="form-control" readonly>
                    </div>

                    <div class="input-group">
                        <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.RecipientsAllreadyinDB"/></span>
                        <input type="text" value="${status.alreadyInDb}" class="form-control" readonly>
                    </div>

                    <c:if test="${mode == ImportMode.ADD.intValue || mode == ImportMode.ADD_AND_UPDATE.intValue}">
                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.result.imported"/></span>
                            <input type="text" value="${status.inserted}" class="form-control" readonly>
                        </div>
                    </c:if>
                    <c:if test="${mode == ImportMode.UPDATE.intValue || mode == ImportMode.ADD_AND_UPDATE.intValue}">
                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.result.updated"/></span>
                            <input type="text" value="${status.updated}" class="form-control" readonly>
                        </div>
                    </c:if>
                    <c:if test="${mode == ImportMode.TO_BLACKLIST.intValue}">
                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.result.blacklisted"/></span>
                            <input type="text" value="${status.blacklisted}" class="form-control" readonly>
                        </div>
                    </c:if>

                    <c:forEach var="entry" items="${mailinglists}">
                        <c:set var="mailinglistID" value="${entry.key}"/>
                        <c:set var="mailinglist" value="${entry.value}"/>

                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned">
                                ${mailinglist.shortname}
                                <c:choose>
                                    <c:when test="${ mode == ImportMode.ADD.intValue or mode == ImportMode.ADD_AND_UPDATE.intValue or mode == ImportMode.UPDATE.intValue}">
                                        <mvc:message code="import.result.subscribersAdded"/>
                                    </c:when>
                                    <c:when test="${mode == ImportMode.MARK_OPT_OUT.intValue}">
                                        <mvc:message code="import.result.subscribersUnsubscribed"/>
                                    </c:when>
                                    <c:when test="${mode == ImportMode.MARK_BOUNCED.intValue}">
                                        <mvc:message code="import.result.subscribersBounced"/>
                                    </c:when>
                                    <c:when test="${mode == ImportMode.MARK_SUSPENDED.intValue}">
                                        <mvc:message code="import.result.bindingsRemoved"/>
                                    </c:when>
                                    <c:when test="${mode == ImportMode.REACTIVATE_SUSPENDED.intValue}">
                                        <mvc:message code="import.result.subscribersReactivated"/>
                                    </c:when>
                                </c:choose>
                            </span>
                            <input type="text" value="${resultMLAdded[mailinglistID]}" class="form-control" readonly>
                        </div>
                    </c:forEach>

                    <c:if test="${mode == ImportMode.ADD.intValue or mode == ImportMode.ADD_AND_UPDATE.intValue}">
                        <div class="input-group">
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="import.result.datasourceId"/></span>
                            <input type="text" value="${status.datasourceID}" class="form-control" readonly>
                        </div>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</div>
