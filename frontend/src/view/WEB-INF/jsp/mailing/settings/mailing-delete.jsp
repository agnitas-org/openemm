<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="shortname" type="java.lang.String"--%>

<emm:CheckLogon/>

<c:choose>
    <c:when test="${isTemplate}">
        <emm:Permission token="template.delete"/>
        <c:set var="question"><mvc:message code="mailing.Delete_Template_Question"/></c:set>
        <c:set var="title"><mvc:message code="Template"/>:&nbsp;${shortname}</c:set>
    </c:when>
    <c:otherwise>
        <emm:Permission token="mailing.delete"/>
        <c:set var="question"><mvc:message code="mailing.MailingDeleteQuestion"/></c:set>
        <c:set var="title"><mvc:message code="Mailing"/>:&nbsp;${shortname}</c:set>
    </c:otherwise>
</c:choose>

    <div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">${title}</h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/${mailingId}/delete.action" method="POST">
                <div class="modal-body">
                    ${question}
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
