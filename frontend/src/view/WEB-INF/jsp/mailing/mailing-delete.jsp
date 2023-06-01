<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<emm:CheckLogon/>

<c:set var="isTemplate" value="0" />
<c:set var="tmpShortname" value="" />
<c:set var="isUsedInActiveWorkflow" value="${mailingBaseForm.usedInActiveWorkflow}"/>
<c:if test="${mailingBaseForm ne null}">
    <c:set var="tmpShortname" value="${mailingBaseForm.shortname}" />
    <c:if test="${mailingBaseForm.isTemplate}">
        <c:set var="isTemplate" value="1" />
    </c:if>
</c:if>

<c:if test="${isTemplate eq 0}">
    <emm:Permission token="mailing.delete"/>
</c:if>
<c:if test="${isTemplate eq 1}">
    <emm:Permission token="template.delete"/>
</c:if>
<c:if test="${!isUsedInActiveWorkflow}">
    <div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <c:if test="${isTemplate eq 0}">
                        <bean:message key="Mailing"/>:&nbsp;${tmpShortname}
                    </c:if>
                    <c:if test="${isTemplate eq 1}">
                        <bean:message key="Template"/>:&nbsp;${tmpShortname}
                    </c:if>
                </h4>
            </div>

            <agn:agnForm action="/mailingbase">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <html:hidden property="isTemplate"/>
                <html:hidden property="fromCalendarPage"/>
                <html:hidden property="previousAction"/>
                <c:set var="tmpMailing" value="${tmpMailing}"/>

                <logic:notEqual name="mailingBaseForm" property="fromCalendarPage" value="1">
                    <c:set var="cancelUrl" value="/mailingbase.do?action=${mailingBaseForm.previousAction}&mailingID=${tmpMailing}"/>
                </logic:notEqual>

                <logic:equal name="mailingBaseForm" property="fromCalendarPage" value="1">
                    <c:set var="cancelUrl" value="/calendar.action"/>
                </logic:equal>


                <input type="hidden" id="delete" name="delete" value=""/>

                <c:set var="deleteJS" value="document.getElementById('delete').value='delete'; document.mailingBaseForm.submit(); return false;"/>
                <c:set var="buttonClass" value="big_button"/>
                <logic:equal name="mailingBaseForm" property="usedInActiveWorkflow" value="true">
                    <c:set var="deleteJS" value=""/>
                    <c:set var="buttonClass" value="big_button_disabled"/>
                </logic:equal>

                <div class="modal-body">
                    <c:if test="${isTemplate eq 0}">
                        <bean:message key="mailing.MailingDeleteQuestion"/>
                    </c:if>
                    <c:if test="${isTemplate eq 1}">
                        <bean:message key="mailing.Delete_Template_Question"/>
                    </c:if>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>

            </agn:agnForm>

        </div>
    </div>
</div>
</c:if>
