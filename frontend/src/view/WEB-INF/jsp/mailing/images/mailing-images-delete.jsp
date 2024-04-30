<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="simpleActionForm" type="org.agnitas.web.forms.SimpleActionForm"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.components.change"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/>
                </span></button>
                <h4 class="modal-title">
                    <mvc:message code="mailing.Graphics_Component"/>:&nbsp;${simpleActionForm.shortname}
                </h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/${mailingId}/images/${simpleActionForm.id}/delete.action" method="DELETE">
                
                <div class="modal-body">
                    <mvc:message code="mailing.Graphics_Component.deleteConfirmation"/>
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
