<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<emm:CheckLogon/>
<emm:Permission token="workflow.delete"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <c:set var="deleteAction" value="/workflow/${workflowForm.workflowId}/delete.action"/>
            <mvc:form servletRelativeAction="${deleteAction}" method="DELETE">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i
                            aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message
                            key="button.Cancel"/></span></button>
                    <h4 class="modal-title">
                        <bean:message key="workflow.single"/>:&nbsp;${workflowForm.shortname}
                    </h4>
                </div>


                <c:choose>
                    <c:when test="${empty bulkDeleteForm}">
                        <div class="modal-body">
                            <bean:message key="workflow.confirmDelete"/>
                        </div>

                        <div class="modal-footer">
                            <div class="btn-group">
                                <button type="button" class="btn btn-default btn-large js-confirm-negative"
                                        data-dismiss="modal">
                                    <i class="icon icon-times"></i>
                                    <span class="text"><bean:message key="button.Cancel"/></span>
                                </button>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive"
                                        data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><bean:message key="button.Delete"/></span>
                                </button>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>

                    </c:otherwise>
                </c:choose>

            </mvc:form>

        </div>
    </div>
</div>
