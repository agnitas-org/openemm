<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<c:choose>
    <c:when test="${excludeDialog}">
        <logic:messagesPresent property="org.apache.struts.action.GLOBAL_MESSAGE" message="true">
            <script data-load=""></script>
        </logic:messagesPresent>
    </c:when>
    <c:otherwise>
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                        <h4 class="modal-title">
                            <c:if test="${not mailingBaseForm.isTemplate}">
                                <bean:message key="Mailing"/>:&nbsp;${mailingBaseForm.shortname}
                            </c:if>
                            <c:if test="${mailingBaseForm.isTemplate}">
                                <bean:message key="Template"/>:&nbsp;${mailingBaseForm.shortname}
                            </c:if>
                        </h4>
                    </div>

                    <html:form action="/mailingbase">
                        <html:hidden property="mailingID"/>
                        <html:hidden property="action"/>
                        <html:hidden property="isTemplate"/>
                        <html:hidden property="isMailingGrid"/>
                        <html:hidden property="gridTemplateId"/>

                        <div class="modal-body">
                            <c:choose>
                                <c:when test="${mailingBaseForm.isTemplate}">
                                    <bean:message key="mailing.Undo_Template_Question"/>
                                </c:when>
                                <c:otherwise>
                                    <bean:message key="mailing.MailingUndoQuestion"/>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="modal-footer">
                            <div class="btn-group">
                                <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                    <i class="icon icon-times"></i>
                                    <span class="text"><bean:message key="button.Cancel"/></span>
                                </button>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><bean:message key="button.Undo"/></span>
                                </button>
                            </div>
                        </div>
                    </html:form>

                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
