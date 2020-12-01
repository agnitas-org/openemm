<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.beans.MediatypeEmail" %>
<%@ page import="com.agnitas.web.ComMailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingWizardForm" type="org.agnitas.web.MailingWizardForm"--%>

<c:set var="ACTION_TARGET" value="<%= ComMailingWizardAction.ACTION_TARGET %>" />
<c:set var="ACTION_ADD_TARGET" value="<%= ComMailingWizardAction.ACTION_ADD_TARGET %>"/>
<c:set var="ACTION_NEW_TARGET" value="<%= ComMailingWizardAction.ACTION_NEW_TARGET %>"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>

<agn:agnForm action="/mwTarget" id="wizard-step-7" data-form-focus="" data-form="resource">
    <html:hidden property="action" value="${ACTION_TARGET}"/>
    <input type="hidden" name="removeTargetID" value="0">

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
                                               styleClass="form-control js-select" disabled="${workflowId gt 0}">
                                    <c:forEach var="mailinglist" items="${mailinglists}">
                                        <html:option value="${mailinglist.id}">
                                            ${mailinglist.shortname}
                                        </html:option>
                                    </c:forEach>
                                </agn:agnSelect>
                             </div>
                            <c:if test="${workflowId ne null or workflowId gt 0}">
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
                                                 property="mailing.campaignID" disabled="${workflowId gt 0}">
                                        <html:option value="0"><bean:message key="mailing.NoCampaign"/></html:option>
                                        <c:forEach var="campaign" items="${campaigns}">
                                            <html:option value="${campaign.id}">
                                                ${campaign.shortname}
                                            </html:option>
                                        </c:forEach>
                                    </html:select>
                                 </div>
                                <c:if test="${workflowId ne null or workflowId gt 0}">
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
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="emailOnepixel"><bean:message key="openrate.measure"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleId="emailOnepixel" styleClass="form-control js-select" property="emailOnepixel" size="1">
                            <html:option value="<%= MediatypeEmail.ONEPIXEL_TOP %>"><bean:message key="mailing.openrate.top"/></html:option>
                            <html:option value="<%= MediatypeEmail.ONEPIXEL_BOTTOM %>"><bean:message
                                    key="mailing.openrate.bottom"/></html:option>
                            <html:option value="<%= MediatypeEmail.ONEPIXEL_NONE %>"><bean:message
                                    key="openrate.none"/></html:option>
                        </html:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="assistant_step7_targetgroups_select"><bean:message key="Targets"/>:</label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <select id="assistant_step7_targetgroups_select" name="targetID" size="1" class="form-control js-select" ${workflowId gt 0 ? 'disabled="disabled"' : ''}>
                                    <option value="0" selected>---</option>
                                    <c:forEach var="target" items="${targets}">
                                        <c:if test="${not emm:contains(mailingWizardForm.mailing.targetGroups, target.id)}">
                                            <option title="${fn:escapeXml(target.targetName)}" value="${target.id}">${fn:escapeXml(target.targetName)} (${target.id})</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>

                            <c:choose>
                                <c:when test="${workflowId ne null or workflowId gt 0}">
                                    <div class="input-group-btn">
                                        <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                            <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingBaseForm.mailingID}"/>
                                        </c:url>
                                        <a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                            <i class="icon icon-linkage-campaignmanager"></i>
                                            <strong><bean:message key="campaign.manager.icon"/></strong>
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="input-group-btn">
                                        <button type="button" class="btn btn-regular" data-form-action="${ACTION_ADD_TARGET}">
                                            <i class="icon icon-plus"></i>
                                        </button>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <c:choose>
                            <c:when test="${fn:length(mailingWizardForm.mailing.targetGroups) gt 0}">
                                <c:forEach var="target" items="${targets}">
                                    <c:if test="${emm:contains(mailingWizardForm.mailing.targetGroups, target.id)}">
                                        <div class="form-group">
                                            <div class="col-sm-5">
                                                <label class="control-label">${fn:escapeXml(target.targetName)} (${target.id})</label>
                                            </div>
                                            <c:if test="${mailingWizardForm.altgId != target.id}">
                                                <div class="col-sm-2">
                                                    <button class="btn btn-regular btn-alert" type="button" data-form-set="removeTargetID:${target.id}, targetID:0" data-form-action="${ACTION_TARGET}">
                                                        <i class="icon icon-trash-o"></i>
                                                        <bean:message key="button.Delete"/>
                                                    </button>
                                                </div>
                                            </c:if>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <bean:message key="statistic.all_subscribers"/><br>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <c:choose>
                            <c:when test="${fn:length(mailingWizardForm.mailing.targetGroups) > 1}">
                                <input type="hidden" name="__STRUTS_CHECKBOX_mailing.targetMode" value="0"/>
                                <div class="checkbox">
                                    <label>
                                        <html:checkbox property="mailing.targetMode" value="1" styleId="assistant_step7_reciepient_in_all_targetgroups"/>
                                        <bean:message key="mailing.targetmode.and"/>
                                    </label>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <html:hidden property="mailing.targetMode"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <a href="#" class="btn btn-regular btn-primary" data-form-action="${ACTION_NEW_TARGET}">
                            <bean:message key="target.NewTarget"/>
                        </a>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
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
