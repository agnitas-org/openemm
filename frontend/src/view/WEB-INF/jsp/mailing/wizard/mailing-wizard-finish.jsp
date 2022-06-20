<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingWizardForm" type="org.agnitas.web.MailingWizardForm"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<c:set var="ACTION_PREVIEW_SELECT" value="<%= MailingSendAction.ACTION_PREVIEW_SELECT %>"/>
<c:set var="ACTION_SEND_ADMIN" value="<%= MailingSendAction.ACTION_SEND_ADMIN %>"/>
<c:set var="ACTION_SEND_TEST" value="<%= MailingSendAction.ACTION_SEND_TEST %>"/>
<c:set var="ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>

<c:set var="mailingId" value="0"/>
<c:if test="${not empty mailingWizardForm.mailing}">
    <c:set var="mailingId" value="${mailingWizardForm.mailing.id}"/>
</c:if>

<c:set var="keepForwardParam" value="${not empty workflowId and workflowId gt 0 ? true : false}"/>

<agn:agnForm action="/mwFinish" id="wizard-step-11" data-form="resource">
    <html:hidden property="action"/>
    <html:hidden property="keepForward" value="${not empty workflowId and workflowId gt 0 ? true : false}"/>

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline">
                    <i class="icon icon-file-o"></i>
                    <bean:message key="mailing.Wizard" />
                </h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-action="previous">
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="active"><span>11</span></li>
                            <li>
                                <html:link page='/mailingbase.do?action=${ACTION_VIEW}&mailingID=${mailingId}&keepForward=${keepForwardParam}'>
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </html:link>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.MailingWizardReadyMsg"/>!
                <bean:message key="mailing.TestAdminDeliveryMsg"/>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="mailing.SendPreviewMessage"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:link styleClass="btn btn-primary btn-regular" page='/mailingsend.do?action=${ACTION_PREVIEW_SELECT}&mailingID=${mailingId}'>
                            <i class="icon icon-eye"></i>
                            <span class="text"><bean:message key="default.Preview"/></span>
                        </html:link>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="adminMail"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:link styleClass="btn btn-primary btn-regular" page='/mailingsend.do?action=${ACTION_SEND_ADMIN}&mailingID=${mailingId}'>
                            <i class="icon icon-send-o"></i>
                            <span class="text"><bean:message key="button.Send"/></span>
                        </html:link>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="testMail"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:link styleClass="btn btn-primary btn-regular" page='/mailingsend.do?action=${ACTION_SEND_TEST}&mailingID=${mailingId}'>
                            <i class="icon icon-send-o"></i>
                            <span class="text"><bean:message key="button.Send"/></span>
                        </html:link>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <html:link styleClass="btn btn-large btn-primary pull-right" page='/mailingbase.do?action=${ACTION_VIEW}&mailingID=${mailingId}&keepForward=${keepForwardParam}'>
                    <span class="text"><bean:message key="button.Finish"/></span>
                    <i class="icon icon-angle-right"></i>
                </html:link>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
