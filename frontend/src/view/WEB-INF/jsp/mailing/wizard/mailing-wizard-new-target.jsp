<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.web.ComTargetAction" %>
<%@ page import="com.agnitas.web.ComMailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="editTargetForm" type="com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm"--%>
<%--@elvariable id="mailTrackingAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="queryBuilderFilters" type="java.lang.String"--%>

<c:set var="ACTION_BACK_TO_MAILINGWIZARD" value="<%= ComTargetAction.ACTION_BACK_TO_MAILINGWIZARD %>" scope="page" />
<c:set var="ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD" value="<%= ComTargetAction.ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD %>" scope="page" />
<c:set var="ACTION_ADD_TARGET" value="<%= ComMailingWizardAction.ACTION_ADD_TARGET %>"/>
<c:set var="ACTION_TARGET_VIEW" value="<%= ComMailingWizardAction.ACTION_TARGET_VIEW %>"/>
<c:set var="ACTION_MAILTYPE" value="<%= ComMailingWizardAction.ACTION_MAILTYPE %>"/>

<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>" />

<c:if test="${empty queryBuilderFilters}">
    <c:set var="queryBuilderFilters" value="${editTargetForm.queryBuilderFilters}" scope="request"/>
</c:if>

<c:set var="queryBuilderRules" value="${editTargetForm.queryBuilderRules}" scope="request"/>
<c:if test="${empty queryBuilderRules}">
    <c:set var="queryBuilderRules" value="[]" scope="request"/>
</c:if>

<c:url var="previous" value="/mwMailtype.do?action=${ACTION_MAILTYPE}"/>

<agn:agnForm action="/mwNewTarget.do" data-form-focus="target_name" id="wizard-step-7" data-form="resource" data-controller="target-group-view">
	<html:hidden property="targetID"/>
    <html:hidden property="format" value="qb"/>
    <html:hidden property="method" value="save"/>

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
                                <html:link page="/mwTarget.do?action=${ACTION_TARGET_VIEW}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </html:link>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="target_name"><bean:message key="Name"/></label>
                    </div>
                    <div class="col-sm-8">
					    <html:text styleId="target_name" styleClass="form-control" property="shortname" maxlength="99" size="42"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="target_description">
                            <bean:message key="default.description"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:textarea styleId="target_description" styleClass="form-control" property="description" rows="5" cols="32"/>
                    </div>
                </div>
                <div class="tile-separator"></div>
                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2 class="headline"><bean:message key="target.TargetDefinition"/></h2>
                    </div>
                    <div class="inline-tile-content">
                        <div class="row" data-initializer="target-group-query-builder">
                            <script id="config:target-group-query-builder" type="application/json">
                                {
                                    "mailTrackingAvailable": ${not empty mailTrackingAvailable ? mailTrackingAvailable : false},
                                    "helpLanguage": "${helplanguage}",
                                    "queryBuilderRules": ${emm:toJson(queryBuilderRules)},
                                    "queryBuilderFilters": ${queryBuilderFilters}
                                }
                            </script>

                            <div class="col-md-12">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label"></label>
                                    </div>
                                    <div class="col-md-12">
                                        <div id="targetgroup-querybuilder">
                                            <html:hidden property="queryBuilderRules" styleId="queryBuilderRules"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="inline-tile-footer">
                        <div class="btn-group">
                            <c:if test="${not empty editTargetForm.targetID and editTargetForm.targetID gt 0}">
                                <emm:ShowByPermission token="targets.migration">
                                    <c:url var="targetDeleteUrl" value="/target/${editTargetForm.targetID}/confirm/delete.action?isWizard=true"/>
                                </emm:ShowByPermission>
                                <emm:HideByPermission token="targets.migration">
                                    <c:url var="targetDeleteUrl" value="/target.do?action=${ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD}&targetID=${editTargetForm.targetID}"/>
                                </emm:HideByPermission>
                                <button class="btn btn-regular btn-alert" type="button" data-form-confirm="" data-form-url="${targetDeleteUrl}">
                                    <i class="icon icon-trash-o"></i>
                                    <bean:message key="button.Delete"/>
                                </button>
                            </c:if>
                            <button class="btn btn-regular btn-primary" type="button" data-action="save-wizard-target">
                                <bean:message key="button.Save"/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <c:url var="newProfileFieldUrl" value="/profiledb/newWizardField.action"/>
                <a href="${newProfileFieldUrl}" class="btn btn-large pull-left">
                    <bean:message key="import.profile.new.column"/>
                </a>

                <c:choose>
                    <c:when test="${editTargetForm.targetID eq 0}">
                        <emm:ShowByPermission token="targets.migration">
                            <a href="<c:url value='/mwSubject.do?action=subject'/>" class="btn btn-large btn-primary pull-right">
                                <bean:message key="button.Proceed"/>
                                <i class="icon icon-angle-right"></i>
                            </a>
                        </emm:ShowByPermission>

                        <emm:HideByPermission token="targets.migration">
                            <html:link styleClass="btn btn-large btn-primary pull-right" page="/target.do?action=${ACTION_BACK_TO_MAILINGWIZARD}">
                                <bean:message key="button.Proceed"/>
                                <i class="icon icon-angle-right"></i>
                            </html:link>
                        </emm:HideByPermission>
                    </c:when>
                    <c:otherwise>
                        <html:link styleClass="btn btn-large btn-primary pull-right" page="/mwTarget.do?action=${ACTION_ADD_TARGET}&addTargetID=${editTargetForm.targetID}">
                            <bean:message key="button.Proceed"/>
                            <i class="icon icon-angle-right"></i>
                        </html:link>
                    </c:otherwise>
                </c:choose>

                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
