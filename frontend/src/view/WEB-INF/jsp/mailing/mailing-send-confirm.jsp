<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>

<c:set var="actionViewSend" value="<%= ComMailingSendActionBasic.ACTION_VIEW_SEND %>"/>

<c:choose>
    <c:when test="${mailingSendForm.approximateMaxSizeWithoutExternalImages <= mailingSendForm.sizeErrorThreshold}">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <agn:agnForm action="/mailingsend" data-form="resource">
                        <html:hidden property="mailingID"/>
                        <html:hidden property="action"/>

                        <html:hidden property="sendDate"/>
                        <html:hidden property="sendHour"/>
                        <html:hidden property="sendMinute"/>
                        <html:hidden property="followupFor"/>

                        <html:hidden property="step"/>
                        <html:hidden property="blocksize"/>
                        <html:hidden property="doublechecking"/>
                        <html:hidden property="skipempty"/>

                        <html:hidden property="maxRecipients"/>

                        <html:hidden property="reportSendAfter24h"/>
                        <html:hidden property="reportSendAfter48h"/>
                        <html:hidden property="reportSendAfter1Week"/>
                        <html:hidden property="reportSendEmail"/>

                        <html:hidden property="recipientReportSendSendingTime"/>
                        <html:hidden property="recipientReportSendAfter24h"/>
                        <html:hidden property="recipientReportSendAfter48h"/>
                        <html:hidden property="recipientReportSendAfter1Week"/>

                        <html:hidden property="sendStatText"/>
                        <html:hidden property="sendStatHtml"/>
                        <html:hidden property="sendStatOffline"/>

                        <html:hidden property="generationOptimization" />

                        <html:hidden property="autoImportId" />

                        <div class="modal-header">
                            <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                            <h4 class="modal-title"><bean:message key="Mailing"/>:&nbsp;${mailingSendForm.shortname}</h4>
                        </div>
                        <div class="modal-body">
                            <c:set var="approximateMaxSizeWithoutExternalImages" value="${emm:formatBytes(mailingSendForm.approximateMaxSizeWithoutExternalImages, 1, 'iec', emm:getLocale(pageContext.request))}"/>
                            <c:set var="approximateMaxSize" value="${emm:formatBytes(mailingSendForm.approximateMaxSize, 1, 'iec', emm:getLocale(pageContext.request))}"/>

                            <c:choose>
                                <%-- Compare rounded values, not the accurate values in bytes --%>
                                <c:when test="${approximateMaxSize eq approximateMaxSizeWithoutExternalImages}">
                                    <c:set var="sizeMessage">${approximateMaxSize}</c:set>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="sizeMessage">${approximateMaxSizeWithoutExternalImages} (${approximateMaxSize})</c:set>
                                </c:otherwise>
                            </c:choose>

                            <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                            <agn:agnMessage key="mailing.send.confirm.ml" escapeMode="none"
                                            arg0="${mailingSendForm.shortname}"
                                            arg1="${mailingSendForm.mailingSubject}"
                                            arg2="${num_recipients}"
                                            arg3="${potentialSendDate}"
                                            arg4="${potentialSendTime}"
                                            arg5="${sizeMessage}"
                                            arg6="${mailinglistShortname}"/>
                        </div>
                        <div class="modal-footer">
                            <div class="btn-group">
                                <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                    <i class="icon icon-times"></i>
                                    <span class="text"><bean:message key="button.Cancel"/></span>
                                </button>
                                <c:url value="/mailingsend.do" var="baseUrl">
                                    <c:param name="action" value="${actionViewSend}"/>
                                    <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                                </c:url>

                                <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-action="send-world" data-base-url="${baseUrl}">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><bean:message key="button.Send"/></span>
                                </button>
                            </div>
                        </div>
                    </agn:agnForm>
                </div>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                        <h4 class="modal-title text-state-alert">
                            <i class="icon icon-state-alert"></i>
                            <bean:message key="Error"/>
                        </h4>
                    </div>
                    <div class="modal-body">
                        <c:set var="mailingSizeErrorThreshold" value="${emm:formatBytes(mailingSendForm.sizeErrorThreshold, 1, 'iec', emm:getLocale(pageContext.request))}"/>
                        <p><bean:message key="error.mailing.size.large" arg0="${mailingSizeErrorThreshold}"/></p>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><bean:message key="button.OK"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
