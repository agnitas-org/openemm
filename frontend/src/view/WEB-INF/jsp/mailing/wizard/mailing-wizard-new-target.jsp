<%@ page language="java" import="com.agnitas.web.*" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>

<c:set var="ACTION_BACK_TO_MAILINGWIZARD" value="<%= ComTargetAction.ACTION_BACK_TO_MAILINGWIZARD %>" scope="page" />
<c:set var="ACTION_SAVE" value="<%= ComTargetAction.ACTION_SAVE %>" scope="page" />
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ComTargetAction.ACTION_CONFIRM_DELETE %>" scope="page" />
<c:set var="ACTION_ADD_TARGET" value="<%= ComMailingWizardAction.ACTION_ADD_TARGET %>"/>

<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>" />

<agn:agnForm action="/mwNewTarget" data-form-focus="mailing_name" id="wizard-step-7" data-form="resource">
	<html:hidden property="targetID"/>
    <html:hidden property="action" />
    <html:hidden property="eql"/>

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
                                <html:link page="/mwMailtype.do?action=mailtype">
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </html:link>
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
                                <html:link page="/mwTarget.do?action=targetView">
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
                        <label class="control-label" for="mailing_name"><bean:message key="Name"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:text styleId="mailing_name" styleClass="form-control" property="shortname" maxlength="99" size="42"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="mailing_name"><bean:message key="default.description"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:textarea styleId="mailing_description" styleClass="form-control" property="description" rows="5" cols="32"/>
                    </div>
                </div>
                <div class="tile-separator"></div>
                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2 class="headline"><bean:message key="target.TargetDefinition"/></h2>
                    </div>
                    <div class="inline-tile-content">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label"></label>
                                    </div>
                                    <div class="col-md-12">
                                        <c:set var="FORM_NAME" value="targetForm" scope="page"/>
                                        <%@include file="/WEB-INF/jsp/rules/rules_list.jsp" %>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <label class="control-label"></label>
                                    </div>
                                    <div class="col-md-12">
                                        <%@include file="/WEB-INF/jsp/rules/rule_add.jsp" %>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="inline-tile-footer">
                        <div class="btn-group">
                            <c:if test="${not empty targetForm.targetID and targetForm.targetID != 0}">
                                <input type="hidden" id="delete" name="delete" value=""/>
                                <button class="btn btn-regular btn-alert" type="button" data-form-set="delete:true" data-form-action="${ACTION_CONFIRM_DELETE}">
                                    <i class="icon icon-trash-o"></i>
                                    <bean:message key="button.Delete"/>
                                </button>
                            </c:if>
                            <input type="hidden" id="save" name="save" value=""/>
                            <button class="btn btn-regular btn-primary" type="button" data-form-set="save:true" data-form-action="${ACTION_SAVE}">
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
                    <c:when test="${targetForm.targetID eq 0}">
                        <html:link styleClass="btn btn-large btn-primary pull-right" page="/target.do?action=${ACTION_BACK_TO_MAILINGWIZARD}">
                            <bean:message key="button.Proceed"/>
                            <i class="icon icon-angle-right"></i>
                        </html:link>
                    </c:when>
                    <c:otherwise>
                        <html:link styleClass="btn btn-large btn-primary pull-right" page="/mwTarget.do?action=${ACTION_ADD_TARGET}&targetID=${targetForm.targetID}">
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
