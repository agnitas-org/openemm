<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:Permission token="recipient.delete"/>

<%--@elvariable id="targetIdToDeleteRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="numberOfRecipients" type="java.lang.Integer"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/target/${targetIdToDeleteRecipients}/delete/recipients.action"
                      method="POST">

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title"><mvc:message code="target.delete.recipients"/></h4>
                </div>
                <div class="modal-body">
                    <c:choose>
                        <c:when test="${numberOfRecipients > 0}">
                            <mvc:message code="target.delete.recipients.question" arguments="${numberOfRecipients}" />
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="target.delete.empty" />
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <c:choose>
                            <c:when test="${numberOfRecipients > 0}">
                                <button type="button" class="btn btn-default btn-large js-confirm-negative"
                                        data-dismiss="modal">
                                    <i class="icon icon-times"></i>
                                    <span class="text"><mvc:message code="button.Cancel"/></span>
                                </button>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive"
                                        data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.Delete"/></span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-primary btn-large js-confirm-negative"
                                                 data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.OK"/></span>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
