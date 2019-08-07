<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_MEASURELINK" value="<%= MailingWizardAction.ACTION_MEASURELINK %>" />
<c:set var="ACTION_FINISH" value="<%= MailingWizardAction.ACTION_FINISH %>" />
<c:set var="ACTION_TO_ATTACHMENT" value="<%= MailingWizardAction.ACTION_TO_ATTACHMENT %>" />

<agn:agnForm action="/mwLink" id="wizard-step-9" data-form="resource" enctype="application/x-www-form-urlencoded">
    <html:hidden property="action" value="${ACTION_MEASURELINK}"/>
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
                            <li class="active"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>
                                <a href="#" data-form-action="link">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.wizard.ChooseThenPressSave"/>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="inline-tile">
                    <div class="inline-tile-content">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="linkUrl"><bean:message key="mailing.URL"/></label>
                            </div>
                            <div class="col-sm-8">
                                <input type="text" readonly="true" value="<bean:write name="mailingWizardForm" property="linkUrl"/>" id="linkUrl" class="form-control"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="linkName"><bean:message key="default.description"/></label>
                            </div>
                            <div class="col-sm-8">
                                <html:text property="linkName" size="52" maxlength="99" styleId="linkName" styleClass="form-control"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="trackable"><bean:message key="mailing.Trackable"/></label>
                            </div>
                            <div class="col-sm-8">
                                <html:select property="trackable" styleId="trackable" styleClass="form-control js-select">
                                    <html:option value="0"><bean:message key="mailing.Not_Trackable"/></html:option>
                                    <html:option value="1"><bean:message key="mailing.Only_Text_Version"/></html:option>
                                    <html:option value="2"><bean:message key="mailing.Only_HTML_Version"/></html:option>
                                    <html:option value="3"><bean:message key="mailing.Text_and_HTML_Version"/></html:option>
                                </html:select>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="linkAction"><bean:message key="action.Action"/></label>
                            </div>
                            <div class="col-sm-8">
                                <html:select property="linkAction" size="1" styleId="linkAction" styleClass="form-control js-select">
                                    <html:option value="0"><bean:message key="settings.No_Action"/></html:option>
                                    <c:forEach var="action" items="${linkActions}">
                                        <html:option value="${action.id}">
                                            ${action.shortname}
                                        </html:option>
                                    </c:forEach>
                                </html:select>
                            </div>
                        </div>
                    </div>
                    <div class="inline-tile-footer">
                        <a class="btn btn-primary btn-regular" href="#" data-form-action="link_save_only">
                            <span class="text"><bean:message key="button.Save"/></span>
                        </a>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <div class="btn-group pull-right">
                    <a href="#" class="btn btn-large btn-primary" data-form-action="${ACTION_MEASURELINK}">
                        <span class="text"><bean:message key="button.Proceed"/></span>
                        <i class="icon icon-angle-right"></i>
                    </a>
                    <a href="#" class="btn btn-large btn-primary" data-form-action="${ACTION_TO_ATTACHMENT}">
                        <span class="text"><bean:message key="button.Skip"/></span>
                    </a>
                    <a href="#" class="btn btn-large btn-primary" data-form-action="${ACTION_FINISH}">
                        <span class="text"><bean:message key="button.Finish"/></span>
                    </a>
                </div>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
