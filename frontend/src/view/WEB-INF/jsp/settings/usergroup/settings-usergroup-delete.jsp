<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>
<%--@elvariable id="excludeDialog" type="java.lang.Boolean"--%>

<c:if test="${empty excludeDialog or not excludeDialog}">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only">
                        <mvc:message code="button.Cancel"/>
                    </span>
                    </button>

                    <h4 class="modal-title">
                        <mvc:message code="default.Name" />:&nbsp;${userGroupForm.shortname}
                    </h4>
                </div>

                <mvc:form servletRelativeAction="/administration/usergroup/delete.action" modelAttribute="userGroupForm">
                    <mvc:hidden path="id"/>
                    <mvc:hidden path="companyId"/>
                    <mvc:hidden path="shortname"/>

                    <div class="modal-body">
                        <mvc:message code="settings.AdminGroupDeleteQuestion"/>
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
</c:if>
