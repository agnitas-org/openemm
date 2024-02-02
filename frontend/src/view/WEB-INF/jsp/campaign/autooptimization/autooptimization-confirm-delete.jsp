<%@ page errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%@ page contentType="text/html; charset=utf-8" buffer="64kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--@elvariable id="optimizationForm" type="com.agnitas.mailing.autooptimization.form.OptimizationForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    ${optimizationForm.shortname}
                </h4>
            </div>

            <mvc:form servletRelativeAction="/optimization/delete.action" modelAttribute="optimizationForm" method="DELETE">
                <mvc:hidden path="optimizationID" />
                <mvc:hidden path="companyID" />
                <mvc:hidden path="campaignID" />
                <mvc:hidden path="campaignName" />

                <div class="modal-body">
                    <mvc:message code="mailing.autooptimization.delete" arguments="${optimizationForm.shortname}"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal" data-form-set="method: cancelled">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="method: delete">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
