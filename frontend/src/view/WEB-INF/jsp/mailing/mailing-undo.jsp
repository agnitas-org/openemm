<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="shortname" type="java.lang.String"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i
                        aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <c:if test="${not isTemplate}">
                        <mvc:message code="Mailing"/>:&nbsp;${shortname}
                    </c:if>
                    <c:if test="${isTemplate}">
                        <mvc:message code="Template"/>:&nbsp;${shortname}
                    </c:if>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/${mailingId}/undo.action" method="GET">
                <div class="modal-body">
                    <c:choose>
                        <c:when test="${isTemplate}">
                            <mvc:message code="mailing.Undo_Template_Question"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="mailing.MailingUndoQuestion"/>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative"
                                data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive"
                                data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Undo"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
