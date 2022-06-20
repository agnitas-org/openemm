<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.beans.MediatypeEmail" %>
<%@ page import="com.agnitas.web.MailingWizardAction" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingWizardForm" type="org.agnitas.web.MailingWizardForm"--%>
<%--@elvariable id="targetComplexities" type="Map<java.lang.Integer, com.agnitas.emm.core.target.beans.TargetComplexityGrade>"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="altgId" type="java.lang.Integer"--%>
<%--@elvariable id="adminAltgIds" type="java.util.Set<java.lang.Integer>"--%>
<%--@elvariable id="complexTargetExpression" type="java.lang.Boolean"--%>

<c:set var="ACTION_TARGET" value="<%=MailingWizardAction.ACTION_TARGET%>" />
<c:set var="ACTION_NEW_TARGET" value="<%=MailingWizardAction.ACTION_NEW_TARGET%>"/>
<c:set var="ACTION_MAILTYPE" value="<%=MailingWizardAction.ACTION_MAILTYPE%>"/>

<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR%>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND%>"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>

<c:url var="previous" value="/mwMailtype.do?action=${ACTION_MAILTYPE}"/>

<agn:agnForm action="/mwTarget" id="wizard-step-7" data-form-focus="" data-form="resource" data-controller="mailing-wizard">
    <html:hidden property="action" value="${ACTION_TARGET}"/>

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
                                <a href="${previous}">
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
                            <li class="active"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>
                                <a href="#" data-form-action="${ACTION_TARGET}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.wizard.MlistTargetMsg"/>
                <button type="button" data-help="help_${helplanguage}/mailingwizard/step_06/MailingSubjectMsg.xml" class="icon icon-help"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="mailing.mailinglistID"><bean:message key="Mailinglist"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <agn:agnSelect styleId="mailing.mailinglistID" property="mailing.mailinglistID" size="1"
                                               styleClass="form-control js-select" disabled="${isWorkflowDriven}">
                                    <c:forEach var="mailinglist" items="${mailinglists}">
                                        <html:option value="${mailinglist.id}">
                                            ${mailinglist.shortname}
                                        </html:option>
                                    </c:forEach>
                                </agn:agnSelect>
                             </div>
                            <c:if test="${workflowDriven}">
                                <div class="input-group-btn">
                                    <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                        <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}"/>
                                    </c:url>
                                    <a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                        <i class="icon icon-linkage-campaignmanager"></i>
                                        <strong><bean:message key="campaign.manager.icon"/></strong>
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </div>


                </div>
                <emm:ShowByPermission token="campaign.show">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="mailing.campaignID"><bean:message key="mailing.archive"/></label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <html:select styleId="mailing.campaignID" styleClass="form-control js-select"
                                                 property="mailing.campaignID" disabled="${workflowDriven}">
                                        <html:option value="0"><bean:message key="mailing.NoCampaign"/></html:option>
                                        <c:forEach var="campaign" items="${campaigns}">
                                            <html:option value="${campaign.id}">
                                                ${campaign.shortname}
                                            </html:option>
                                        </c:forEach>
                                    </html:select>
                                 </div>
                                <c:if test="${workflowDriven}">
                                    <div class="input-group-btn">
                                        <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                            <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}"/>
                                        </c:url>
                                        <a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                            <i class="icon icon-linkage-campaignmanager"></i>
                                            <strong><bean:message key="campaign.manager.icon"/></strong>
                                        </a>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </emm:ShowByPermission>

                <jsp:include page="../media/email-onepixel.jsp">
                    <jsp:param name="isEmailSettingsDisabled" value="${false}"/>
                </jsp:include>

                <html:hidden property="__STRUTS_CHECKBOX_targetGroupIds" value="[]"/>
                <%@include file="../fragments/mailing-targets-select.jspf" %>

                <c:if test="${not (complexTargetExpression or (isWorkflowDriven and fn:length(mailingWizardForm.mailing.targetGroups) <= 1))}">
                    <div class="form-group" style="${fn:length(mailingWizardForm.mailing.targetGroups) <= 1 ? 'display: none;' : ''}">
                        <div class="col-sm-offset-4 col-sm-8">
                            <html:hidden property="__STRUTS_CHECKBOX_mailing.targetMode" value="${TARGET_MODE_OR}"/>
                            <div class="checkbox">
                                <label>
                                    <html:checkbox styleId="targetModeCheck" property="mailing.targetMode" value="${TARGET_MODE_AND}" disabled="${isWorkflowDriven or (not empty adminAltgIds and adminAltgIds.size() > 0) or (not empty altgId and altgId > 0)}" />
                                    <bean:message key="mailing.targetmode.and"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </c:if>                    

                <c:if test="${not isWorkflowDriven}">
                    <div class="form-group">
                        <div class="col-sm-offset-4 col-sm-8">
                            <a href="#" class="btn btn-regular btn-primary" data-form-action="${ACTION_NEW_TARGET}">
                                <bean:message key="target.NewTarget"/>
                            </a>
                        </div>
                    </div>
                </c:if>
            </div>
            <div class="tile-footer">
                <a href="${previous}" class="btn btn-large pull-left">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <a href="#" class="btn btn-large btn-primary pull-right" data-form-action="${ACTION_TARGET}">
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
