<%@page import="com.agnitas.messages.Message"%>
<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.importvalues.ImportMode" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>

<%@include file="/WEB-INF/jsp/messages.jsp" %>

<emm:Permission token="wizard.importclassic"/>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="mailinglists" type="java.util.Map<java.lang.String, org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="importIsDone" type="java.lang.Boolean"--%>
<%--@elvariable id="importError" type="java.lang.String"--%>
<%--@elvariable id="resultMLAdded" type="java.util.Map"--%>

<c:set var="status" value="${importWizardSteps.helper.status}"/>
<c:set var="mode" value="${importWizardSteps.helper.mode}"/>

<c:if test="${importIsDone}">
    <div class="hidden" data-load-stop></div>
</c:if>

<div id="import-result" class="inline-tile">
	<c:if test="${importIsDone}">
	    <div class="inline-tile-header">
	        <h2 class="headline"><mvc:message code="ResultMsg"/></h2>
	        <ul class="inline-tile-header-actions">
	            <li>
	                <p>
	                    <a href="<c:url value='/recipient/import/wizard/downloadCsv.action?downloadName=result'/>" class="btn btn-regular btn-primary">
	                        <i class="icon icon-download"></i>
	                        <mvc:message code="button.Download"/>
	                    </a>
	                </p>
	            </li>
	        </ul>
	    </div>
    </c:if>
    <div class="tile-separator"></div>
    <div class="inline-tile-content">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="import.csv_importing_data"/></label>
            </div>
            <c:if test="${not empty importError}">
	            <div class="col-sm-8">
					<ul class="list-group">
						<li class="list-group-item" style="background-color:#DF3939; color:#FFFFFF">${importError}</li>
					</ul>
	            </div>
            </c:if>
            <c:if test="${empty importError}">
	            <div class="col-sm-8">
	                <ul class="list-group">
	                    <c:if test="${importWizardSteps.helper.dbInsertStatus > 100}">
	                        <c:forEach var="messageAndParameter" items="${importWizardSteps.dbInsertStatusMessagesAndParameters}">
	                            <li class="list-group-item">
	                            	<mvc:message code='<%= ((Message) pageContext.getAttribute("messageAndParameter")).getCode() %>' arguments='<%= ((Message) pageContext.getAttribute("messageAndParameter")).getArguments().length > 0 ? ((String) ((Message) pageContext.getAttribute("messageAndParameter")).getArguments()[0]) : "" %>'/>
	                            </li>
	                        </c:forEach>
	                    </c:if>
	                </ul>
	            </div>
            </c:if>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <ul class="list-group">
                    <c:if test="${importWizardSteps.helper.dbInsertStatus >= 1000}">
                        <li class="list-group-item">
                            <span class="badge">${status.getError('email')}</span>
                            <mvc:message code="import.csv_errors_email"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('blacklist')}</span>
                            <mvc:message code="import.csv_errors_blacklist"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('keyDouble')}</span>
                            <mvc:message code="import.csv_errors_double"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('numeric')}</span>
                            <mvc:message code="import.csv_errors_numeric"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('mailtype')}</span>
                            <mvc:message code="import.csv_errors_mailtype"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('gender')}</span>
                            <mvc:message code="import.csv_errors_gender"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('date')}</span>
                            <mvc:message code="import.csv_errors_date"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.getError('structure')}</span>
                            <mvc:message code="csv_errors_linestructure"/>
                        </li>
    					<li class="list-group-item">
                            <span class="badge">${status.invalidNullValues}</span>
                            <mvc:message code="import.csv_errors_invalidNullValues"/>
                        </li>
    					<li class="list-group-item">
                            <span class="badge">${status.getError('valueTooLarge')}</span>
                            <mvc:message code="error.import.value.large"/>
                        </li>
    					<li class="list-group-item">
                            <span class="badge">${status.getError('numberTooLarge')}</span>
                            <mvc:message code="error.import.number.large"/>
                        </li>
    					<li class="list-group-item">
                            <span class="badge">${status.getError('invalidFormat')}</span>
                            <mvc:message code="error.import.invalidFormat"/>
                        </li>
    					<li class="list-group-item">
                            <span class="badge">${status.getError('missingMandatory')}</span>
                            <mvc:message code="error.import.missingMandatory"/>
                        </li>
                        <c:if test="${status.errorColumns.size() > 0}">
	                        <li class="list-group-item">
	                            <span class="badge">${fn:join(status.errorColumns.toArray(), ", ")}</span>
	                            <mvc:message code="error.import.errorColumns"/>
	                        </li>
                        </c:if>
                        <li class="list-group-item">
                            <span class="badge">${status.csvLines}</span>
                            <mvc:message code="import.result.filedataitems"/>
                        </li>
                        <li class="list-group-item">
                            <span class="badge">${status.alreadyInDb}</span>
                            <mvc:message code="import.RecipientsAllreadyinDB"/>
                        </li>
                        <c:if test="${mode == ImportMode.ADD.intValue || mode == ImportMode.ADD_AND_UPDATE.intValue}">
                            <li class="list-group-item">
                                <span class="badge">${status.inserted}</span>
                                <mvc:message code="import.result.imported"/>
                            </li>
                        </c:if>
                        <c:if test="${mode == ImportMode.UPDATE.intValue || mode == ImportMode.ADD_AND_UPDATE.intValue}">
                            <li class="list-group-item">
                                <span class="badge">${status.updated}</span>
                                <mvc:message code="import.result.updated"/>
                            </li>
                        </c:if>                         
                        <c:if test="${mode == ImportMode.TO_BLACKLIST.intValue}">
                            <li class="list-group-item">
                                <span class="badge">${status.blacklisted}</span>
                                <mvc:message code="import.result.blacklisted"/>
                            </li>
                        </c:if>
                                                
                        <c:forEach var="entry" items="${mailinglists}">
                            <c:set var="mailinglistID" value="${entry.key}"/>
                            <c:set var="mailinglist" value="${entry.value}"/>
                            <li class="list-group-item">
                                <span class="badge">${resultMLAdded[mailinglistID]}</span>
                                ${mailinglist.shortname}
                                <c:choose>
                                    <c:when test="
                                    ${ mode == ImportMode.ADD.intValue
                                    or mode == ImportMode.ADD_AND_UPDATE.intValue
                                    or mode == ImportMode.UPDATE.intValue}">
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
                            </li>
                        </c:forEach>
                        <c:if test="${mode == ImportMode.ADD.intValue or mode == ImportMode.ADD_AND_UPDATE.intValue}">
                            <li class="list-group-item">
                                <span class="badge">${status.datasourceID}</span>
                                <mvc:message code="import.result.datasourceId"/>
                            </li>
                        </c:if>
                    </c:if>
                </ul>
            </div>
        </div>
            
		<c:if test="${importIsDone}">
			<div class="tile-footer">
                <c:url var="recipientUrl" value="/recipient/list.action">
                    <c:param name="latestDataSourceId" value="${status.datasourceID}"/>
                </c:url>
                <a href="${recipientUrl}" class="btn btn-large btn-primary pull-right">
                    <span><mvc:message code="button.Finish"/></span>
                </a>
				<span class="clearfix"></span>
			</div>
		</c:if>
	</div>
</div>
