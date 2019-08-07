<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>
<%--@elvariable id="excludeDialog" type="java.lang.Boolean"--%>
<%--@elvariable id="bulkDeleteForm" type="org.agnitas.web.forms.BulkDeleteForm"--%>


<c:if test="${not excludeDialog}">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <c:choose>
                    <c:when test="${empty bulkDeleteForm}">
                        <c:set var="deleteAction" value="/mailinglist/${mailinglistForm.id}/delete.action"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="deleteAction" value="/mailinglist/bulkDelete.action"/>
                    </c:otherwise>
                </c:choose>

                <mvc:form servletRelativeAction="${deleteAction}" method="DELETE">
                    <c:choose>
                        <c:when test="${empty bulkDeleteForm}">
                            <div class="modal-header">
                                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                                    <span class="sr-only">
                                    <mvc:message code="button.Cancel"/>
                                </span>
                                </button>

                                <h4 class="modal-title">
                                    <mvc:message code="mailinglist" />:&nbsp;${mailinglistForm.shortname}
                                </h4>
                            </div>

                            <div class="modal-body">
                                <mvc:message code="mailinglist.delete.question"/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="deleteMailinglistId" items="${bulkDeleteForm.bulkIds}">
                                <input type="hidden" name="bulkIds" value="${deleteMailinglistId}"/>
                            </c:forEach>

                            <div class="modal-header">
                                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                                    <span class="sr-only">
                                    <mvc:message code="button.Cancel"/>
                                </span>
                                </button>

                                <h4 class="modal-title">
                                    <mvc:message code="mailinglist" />:&nbsp;${fn:length(bulkDeleteForm.bulkIds)}
                                </h4>
                            </div>

                            <div class="modal-body">
                                <mvc:message code="mailinglist.delete.question" />
                            </div>
                        </c:otherwise>
                    </c:choose>

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
                                <span class="text"><mvc:message code="button.Delete"/></span>
                            </button>
                        </div>
                    </div>
                </mvc:form>
            </div>
        </div>
    </div>
</c:if>
