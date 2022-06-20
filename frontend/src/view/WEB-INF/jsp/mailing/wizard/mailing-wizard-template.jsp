<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_TEMPLATE" value="<%= MailingWizardAction.ACTION_TEMPLATE %>"/>

<c:set var="canEditGeneralSettings" value="${true}"/>
<%@include file="mailing-wizard-template-hide.jspf" %>

<agn:agnForm action="/mwTemplate" id="wizard-step-2" data-form-focus="mailTemplateID" data-form="resource">
    <html:hidden property="action" value="${ACTION_TEMPLATE}"/>

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
                            <li class="active"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li class="disabled ${canEditGeneralSettings ? 'hidden' : ''}" data-next-step-link-holder>
                                <span>
                                    <bean:message key="button.Proceed"/>
                                </span>
                            </li>
                            <li ${canEditGeneralSettings ? '' : 'class="hidden"'} data-next-step-link>
                                <a href="#" data-form-action="${ACTION_TEMPLATE}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.wizard.ChooseTemplateMsg"/>
                <button type="button" data-help="help_${helplanguage}/mailingwizard/step_02/ChooseTemplateMsg.xml" class="icon icon-help"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="mailTemplateID">
                            <bean:message key="Template"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select property="mailing.mailTemplateID" styleId="mailTemplateID" styleClass="form-control js-select js-templateSelector">
                            <html:option value="0"><bean:message key="mailing.No_Template"/></html:option>
                            <c:forEach var="template" items="${templates}">
                                <html:option value="${template.id}">
                                    ${template.shortname}
                                </html:option>
                            </c:forEach>
                        </html:select>
                    </div>
                </div>
            </div>

            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <div class="disabled btn btn-large btn-primary pull-right ${canEditGeneralSettings ? 'hidden' : ''}" data-next-step-link-holder>
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </div>
                <a href="#" class="btn btn-large btn-primary pull-right ${canEditGeneralSettings ? '' : 'hidden'}" data-form-action="${ACTION_TEMPLATE}" data-next-step-link>
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>

    <%@include file="mailing-wizard-template-hidescript.jspf" %>
</agn:agnForm>
