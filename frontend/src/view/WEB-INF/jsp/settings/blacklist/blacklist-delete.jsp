<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="blacklistDeleteForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistDeleteForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="Recipient"/> ${blacklistDeleteForm.email}
                </h4>
            </div>

            <mvc:form servletRelativeAction="/recipients/blacklist/delete.action"
                      modelAttribute="blacklistDeleteForm"
                      id="blacklistDeleteView">

                <input type="hidden" name="emails" value="${blacklistDeleteForm.email}">

                <div class="modal-body">
                    <mvc:message code="recipient.blacklist.delete"/>

                    <c:if test="${not empty mailinglists}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <mvc:message code="blacklist.mailinglists"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <ul class="list-group">
                                    <c:forEach var="mailinglist" items="${mailinglists}">
                                        <li class="list-group-item">
                                            <label class="checkbox-inline">
                                                <mvc:checkbox path="mailingListIds" value="${mailinglist.id}" />
                                                    ${mailinglist.shortname}
                                            </label>
                                        </li>
                                    </c:forEach>
                                </ul>
                                <p class="help-block"><mvc:message code="blacklist.mailinglists.hint"/></p>
                            </div>
                        </div>
                    </c:if>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative"
                                data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive"
                                data-dismiss="modal"
                                data-action="blacklist-delete">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
