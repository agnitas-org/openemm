<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb"  errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="forTemplates" type="java.lang.Boolean"--%>

<emm:CheckLogon/>

<c:choose>
    <c:when test="${forTemplates}">
        <emm:Permission token="template.delete"/>
    </c:when>
    <c:otherwise>
        <emm:Permission token="mailing.delete"/>
    </c:otherwise>
</c:choose>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <c:choose>
                        <c:when test="${forTemplates}">
                            <mvc:message code="bulkAction.delete.template"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="bulkAction.delete.mailing"/>
                        </c:otherwise>
                    </c:choose>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/bulkDelete.action?forTemplates=${forTemplates}" modelAttribute="bulkActionForm" method="POST">
                <mvc:hidden path="bulkIds"/>
                
                <div class="modal-body">
                    <c:choose>
                        <c:when test="${forTemplates}">
                            <mvc:message code="bulkAction.delete.template.question"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="bulkAction.delete.mailing.question"/>
                        </c:otherwise>
                    </c:choose>
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
