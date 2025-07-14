<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"%>

<%--@elvariable id="form" type="com.agnitas.web.forms.BulkActionForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="bulkAction.delete.target"/>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/target/bulk/delete.action" modelAttribute="form">
                <c:forEach var="targetId" items="${form.bulkIds}">
                    <input type="hidden" name="bulkIds" value="${targetId}"/>
                </c:forEach>

                <div class="modal-body">
                    <mvc:message code="bulkAction.delete.target.question"/>
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
