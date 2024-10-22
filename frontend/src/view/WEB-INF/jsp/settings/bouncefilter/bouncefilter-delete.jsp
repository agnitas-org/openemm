<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/administration/bounce/delete.action" modelAttribute="bounceFilterForm">
                <mvc:hidden path="id"/>
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only">
                            <mvc:message code="button.Cancel"/>
                        </span>
                    </button>
                    <h4 class="modal-title">
                        <mvc:message code="settings.Mailloop"/> ${bounceFilterForm.shortName}
                    </h4>
                </div>
                <div class="modal-body">
                    <c:choose>
                        <c:when test="${containsReplies}">
                            <mvc:message code="mailloop.delete.inbox.question" arguments="${bounceFilterForm.shortName}"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="settings.mailloop.delete"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <mvc:message code="button.Cancel"/>
                            </span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                <mvc:message code="button.Delete"/>
                            </span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
