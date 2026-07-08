<%@ page import="com.agnitas.emm.core.response_inbox.enums.MailloopReplyContentType" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="replyEntry" type="com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry"--%>
<%--@elvariable id="sendersIds" type="java.util.List<java.lang.Integer>"--%>

<c:set var="sendersLength" value="${fn:length(sendersIds)}" />
<c:choose>
    <c:when test="${sendersLength eq 1}">
        <c:url var="goToRecipientUrl" value="/recipient/${sendersIds.iterator().next()}/view.action" />
    </c:when>
    <c:otherwise>
        <c:url var="goToRecipientUrl" value="/recipient/list.action?email=${replyEntry.senderEmail}" />
    </c:otherwise>
</c:choose>

<div class="modal" tabindex="-1" data-controller="mailloop-reply-view">
    <script type="application/json" data-initializer="mailloop-reply-view">
        {
          "id": ${replyEntry.id}
        }
    </script>

    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="mailloop.inbox.view"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body vstack gap-3 js-scrollable overflow-auto">
                <div>
                    <div class="row g-1">
                        <div class="col">
                            <label class="form-label"><mvc:message code="From" /></label>
                            <input type="text" class="form-control pe-none" value="${replyEntry.sender}">
                        </div>
                        <div class="col-auto d-flex">
                            <a href="${goToRecipientUrl}" type="button" class="btn btn-secondary align-self-end ${sendersLength eq 0 ? 'disabled' : ''}">
                                <i class="icon icon-external-link-alt"></i>
                                <span><mvc:message code="recipient.existing.switch" /></span>
                            </a>
                        </div>
                    </div>
                </div>

                <div>
                    <label class="form-label"><mvc:message code="mailing.Subject" /></label>
                    <input type="text" class="form-control pe-none" value="${replyEntry.subject}">
                </div>

                <div data-controller="iframe-progress" data-initializer="iframe-progress">
                    <label class="form-label"><mvc:message code="default.message" /></label>
                    <c:choose>
                        <c:when test="${replyEntry.contentType eq MailloopReplyContentType.TEXT_PLAIN}">
                            <textarea class="form-control pe-none" rows="1">${replyEntry.content}</textarea>
                        </c:when>
                        <c:otherwise>
                            <div id="preview-progress" class="progress loop w-100" style="display:none;"></div>

                            <div class="flex-center">
                                <div class="flex-center flex-grow-1">
                                    <iframe class="default-iframe js-simple-iframe w-100" src="<c:url value="/response-inbox/${replyEntry.id}/content/view.action"/>" data-max-width="100%" style="height: 0">
                                        Your Browser does not support IFRAMEs, please update!
                                    </iframe>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="modal-footer">
                <a href="mailto:${replyEntry.sender}?subject=${replyEntry.subject}" type="button" class="btn btn-secondary">
                    <i class="icon icon-external-link-alt"></i>
                    <span><mvc:message code="mailloop.inbox.respond" /></span>
                </a>
            </div>
        </div>
    </div>

    <script id="mailoop-reply-new-status-badge" type="text/x-mustache-template">
        <span class="status-badge mailloop.reply.status.${fn:toLowerCase(replyEntry.status)}" data-tooltip="<mvc:message code="${replyEntry.status.messageKey}" />"></span>
    </script>
</div>
