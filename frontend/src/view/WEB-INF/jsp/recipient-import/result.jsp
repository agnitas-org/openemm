<%@ page import="com.agnitas.emm.core.imports.beans.ImportResultFileType" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="reportEntries" type="java.util.List<org.agnitas.util.ImportReportEntry>"--%>
<%--@elvariable id="assignedMailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="mailinglistAssignStats" type="java.util.Map<com.agnitas.emm.core.mediatypes.common.MediaTypes, java.util.Map<java.lang.Integer, java.lang.Integer>>"--%>
<%--@elvariable id="mailinglistMessage" type="java.lang.String"--%>
<%--@elvariable id="fixedRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="duplicateRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="invalidRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="validRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="resultFile" type="java.lang.String"--%>

<c:set var="RESULT_FILE_TYPE" value="<%= ImportResultFileType.RESULT %>" scope="page" />
<c:set var="VALID_FILE_TYPE" value="<%= ImportResultFileType.VALID_RECIPIENTS %>" scope="page" />
<c:set var="INVALID_FILE_TYPE" value="<%= ImportResultFileType.INVALID_RECIPIENTS %>" scope="page" />
<c:set var="FIXED_FILE_TYPE" value="<%= ImportResultFileType.FIXED_BY_HAND_RECIPIENTS %>" scope="page" />
<c:set var="DUPLICATE_FILE_TYPE" value="<%= ImportResultFileType.DUPLICATED_RECIPIENTS %>" scope="page" />

<c:set var="headlineCls" value="icon-state-success"/>

<c:forEach var="reportEntry" items="${reportEntries}">
	<c:if test="${reportEntry.value.matches('[0-9]+')}">
	    <c:if test="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}">
	        <c:set var="headlineCls" value="icon-state-warning"/>
	    </c:if>
	</c:if>
</c:forEach>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline">
            <i class="icon ${headlineCls}"></i> <mvc:message code="import.csv_Finished"/>
        </h2>
    </div>
    <div class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <ul class="list-group">
                    <c:forEach var="reportEntry" items="${reportEntries}">
						<c:if test="${reportEntry.value.matches('.*[A-Za-z]+.*')}">
	                        <li class="list-group-item">
	                            <span class="badge
	                                <c:if test="${fn:contains(reportEntry.key, 'error')}"> badge-alert </c:if>
	                                <c:if test="${fn:contains(reportEntry.key, 'fatal')}"> badge-alert </c:if>">
	                                ${reportEntry.value}
	                            </span>
	                            <mvc:message code="${reportEntry.key}"/>
	                        </li>
						</c:if>
						
	                    <c:if test="${reportEntry.value.matches('[0-9]+')}">
	                        <li class="list-group-item">
	                            <span class="badge
	                                <c:if test="${reportEntry.value eq 0 && fn:contains(reportEntry.key, 'error')}"> badge-success </c:if>
	                                <c:if test="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}"> badge-warning </c:if>">
	                                ${reportEntry.value}
	                            </span>
	                            <mvc:message code="${reportEntry.key}"/>
	                        </li>
						</c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        <c:if test="${assignedMailinglists != null}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <ul class="list-group">
	                <c:forEach var="mailinglistAssignStatByMediaType" items="${mailinglistAssignStats}">
	                	<mvc:message code="mediatype"/> ${mailinglistAssignStatByMediaType.key}:
                    	<c:forEach var="mailinglist" items="${assignedMailinglists}">
	                        <li class="list-group-item">
	                            <span class="badge">${mailinglistAssignStatByMediaType.value[mailinglist.id]}</span>
	                            <mvc:message code="${mailinglistMessage}"/> ${mailinglist.shortname}
	                        </li>
	                    </c:forEach>
	                </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>
        <c:if test="${resultFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="ResultMsg"/></label>
                </div>
                <div class="col-sm-8">
                    <c:url var="downloadResultLink" value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${RESULT_FILE_TYPE}" />
                    </c:url>

                    <a href="${downloadResultLink}" class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        ${resultFile}
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${validRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="import.recipients.valid"/></label>
                </div>
                <div class="col-sm-8">
                    <c:url var="downloadValidLink" value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${VALID_FILE_TYPE}" />
                    </c:url>

                    <a href="${downloadValidLink}" class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        ${validRecipientsFile}
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${invalidRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="import.recipients.invalid"/></label>
                </div>
                <div class="col-sm-8">
                    <c:url var="downloadInvalidLink" value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${INVALID_FILE_TYPE}" />
                    </c:url>

                    <a href="${downloadInvalidLink}" class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        ${invalidRecipientsFile}
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${duplicateRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="import.recipients.duplicate"/></label>
                </div>
                <div class="col-sm-8">
                    <c:url var="downloadDuplicateLink" value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${DUPLICATE_FILE_TYPE}" />
                    </c:url>

                    <a href="${downloadDuplicateLink}" class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        ${duplicateRecipientsFile}
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${fixedRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="import.recipients.fixed"/></label>
                </div>
                <div class="col-sm-8">
                    <c:url var="downloadFixedLink" value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${FIXED_FILE_TYPE}" />
                    </c:url>

                    <a href="${downloadFixedLink}" class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        ${fixedRecipientsFile}
                    </a>
                </div>
            </div>
        </c:if>
    </div>
</div>
