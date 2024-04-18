<%@ page language="java"  contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="birtReportDeleteForm" type="org.agnitas.web.forms.SimpleActionForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="Report"/>:&nbsp;${birtReportDeleteForm.shortname}
                </h4>
            </div>

            <mvc:form servletRelativeAction="/statistics/report/delete.action" modelAttribute="birtReportDeleteForm">
                <mvc:hidden path="id"/>

                <input type="hidden" id="kill" name="kill" value="true"/>
                <div class="modal-body">
                    <mvc:message code="report.delete.question"/>
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
