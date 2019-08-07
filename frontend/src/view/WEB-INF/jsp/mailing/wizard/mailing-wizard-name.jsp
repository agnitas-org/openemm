<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="isEnableTrackingVeto" type="java.lang.Boolean"--%>
<%--@elvariable id="ACTION_NAME" type="java.lang.Boolean"--%>

<c:set var="ACTION_NAME" value="<%= MailingWizardAction.ACTION_NAME %>"/>

<agn:agnForm action="/mwName" id="wizard-step-1" data-form-focus="mailing.shortname" data-form="resource">
    <html:hidden property="action" value="${ACTION_NAME}"/>

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
                            <li class="active"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>
                                <a href="#" data-form-action="${ACTION_NAME}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.wizard.NameDescriptionMsg"/>
                <button type="button" data-help="help_${helplanguage}/mailingwizard/step_01/MailingNameDescription.xml" class="icon icon-help"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="mailing.shortname">
                            <bean:message key="default.Name"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text styleId="mailing.shortname" styleClass="form-control" property="mailing.shortname" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="mailing.description">
                            <bean:message key="default.description"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:textarea styleId="mailing.description" styleClass="form-control v-resizable" property="mailing.description"/>
                    </div>
                </div>
            </div>

			<c:if test="${isEnableTrackingVeto}"> 
	        	<div class="form-group">
	            	<div class="col-sm-4">
	                	<label class="control-label" for="mailingContentTypeAdvertising">
	                    	<bean:message key="mailing.contentType.advertising"/>
	                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/AdvertisingMsg.xml" tabindex="-1" type="button"></button>
	                 	</label>
	              	</div>
	              	<div class="col-sm-8">
		           		<html:hidden property="__STRUTS_CHECKBOX_mailingContentTypeAdvertising" value="false" />
		  				<label class="toggle">
		  					<html:checkbox property="mailingContentTypeAdvertising" />
		                  	<div class="toggle-control"></div>
		  				</label>
	           		</div>
	    		</div>
 			</c:if>

            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <a href="#" class="btn btn-large btn-primary pull-right" data-form-action="${ACTION_NAME}">
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
