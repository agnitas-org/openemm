<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="org.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ACTION_TYPE" value="<%= MailingWizardAction.ACTION_TYPE %>"/>
<c:set var="ACTION_TYPE_PREVIOUS" value="<%= MailingWizardAction.ACTION_TYPE_PREVIOUS %>"/>

<agn:agnForm action="/mwType" id="wizard-step-3" data-form-focus="mailing.mailingType" data-form="resource">
    <html:hidden property="action" value="${ACTION_TYPE}"/>

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
                            <li class="active"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>
                                <a href="#" data-form-action="${ACTION_TYPE}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.mailingtype.choose"/>
                <button type="button" data-help="help_${helplanguage}/mailingwizard/step_03/MailingTypeDescBefore.xml" class="icon icon-help"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <div class="radio">
                            <label>
                                <html:radio property="mailing.mailingType" value="<%= Integer.toString(Mailing.TYPE_NORMAL) %>"/>
                                <bean:message key="Normal_Mailing"/>
                            </label>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <div class="radio">
                            <label>
                                <html:radio property="mailing.mailingType" value="<%= Integer.toString(Mailing.TYPE_ACTIONBASED) %>"/>
                                <bean:message key="mailing.action.based.mailing"/>
                            </label>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <div class="radio">
                            <label>
                                <html:radio property="mailing.mailingType" value="<%= Integer.toString(Mailing.TYPE_DATEBASED) %>"/>
                                <bean:message key="mailing.Rulebased_Mailing"/>
                            </label>
                        </div>
                    </div>
                </div>
                <%@include file="mailing-wizard-type-followup.jspf" %>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <a href="#" class="btn btn-large btn-primary pull-right" data-form-action="${ACTION_TYPE}">
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
