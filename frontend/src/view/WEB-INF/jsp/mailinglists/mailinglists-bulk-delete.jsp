<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="excludeDialog" type="java.lang.Boolean"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailinglist/bulkDelete.action">
                <logic:iterate id="mailingListId" name="bulkDeleteForm" property="bulkIds">
                    <input type="hidden" name="bulkIds" value="${mailingListId}"/>
                </logic:iterate>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title"><mvc:message code="mailinglist" /></h4>
                </div>

                <div class="modal-body">
                    <mvc:message code="bulkAction.delete.mailinglist.question"/>
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
