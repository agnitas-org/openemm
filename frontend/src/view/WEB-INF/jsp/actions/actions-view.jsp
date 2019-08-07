<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.emm.core.action.operations.*, org.agnitas.actions.*, org.springframework.web.context.support.*, org.agnitas.beans.factory.ActionOperationFactory" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.EmmActionAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_ADD_MODULE" value="<%= EmmActionAction.ACTION_ADD_MODULE %>"/>

<c:if test="${requestScope.oplist != null}">

    <agn:agnForm action="/action" id="emmActionForm" data-form="resource">
        <html:hidden property="action"/>
        <html:hidden property="actionID"/>

        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><bean:message key="action.Edit_Action"/></h2>

                <ul class="tile-header-actions">
                    <li>
                        <label class="btn btn-regular btn-ghost toggle">
                            <html:hidden property="__STRUTS_CHECKBOX_isActive" value="false"/>
                            <span class="text">
                                <bean:message key="mailing.status.active"/>
                            </span>
                            <html:checkbox property="isActive"/>
                            <div class="toggle-control"></div>
                        </label>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="Name"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:text styleId="mailing_name" styleClass="form-control" property="shortname" maxlength="50" size="42"/>
                    </div>
                </div>
                <div class="form-group">
                	<div class="col-sm-4">
                    	<label class="control-label"><bean:message key="Usage"/></label>
                    </div>
                        <div class="col-sm-8">
                            <html:select property="type" size="1" styleClass="form-control js-select">
                                <html:option value="0"><bean:message key="actionType.link"/></html:option>
                                <html:option value="1"><bean:message key="actionType.form"/></html:option>
                                <html:option value="9"><bean:message key="actionType.all"/></html:option>
                            </html:select>
                        </div>
                    </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="default.description"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:textarea styleId="mailing_description" styleClass="form-control" property="description" rows="5" cols="32"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><bean:message key="Steps"/></h2>
            </div>
            <div class="tile-content">
                <div class="tile-content-forms">
                    <logic:present name="emmActionForm" property="actions">
                        <% int index=0;
                            String className = null;
                            org.springframework.context.ApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(application);
                            ActionOperationFactory factory = (ActionOperationFactory) wac.getBean("ActionOperationFactory"); %>
                        <logic:iterate id="op" name="emmActionForm" property="actions">
                            <%
                            	request.setAttribute("op", pageContext.getAttribute("op"));
                                                            request.setAttribute("opIndex", index);
                                                            className=factory.getType((ActionOperationParameters) pageContext.getAttribute("op"));
                                                            index++;
                            %>
                            <div class="inline-tile">
                                <div class="inline-tile-header">
                                    <h2 class="headline"><%= index %>.&nbsp;<bean:message key='<%= "action.op." + className %>'/></h2>
                                </div>
                                <html:errors property="<%= Integer.toString(index) %>"/>
                                <jsp:include page='<%= "ops/" + className + ".jsp" %>'/>
                            </div>
                            <div class="tile-separator"></div>
                        </logic:iterate>
                    </logic:present>
                    <div class="inline-tile">
                        <div class="inline-tile-header">
                            <h2 class="headline"><bean:message key="action.step.add"/></h2>
                        </div>
                        <div class="inline-tile-content">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label"><bean:message key="default.Type"/></label>
                                </div>
                                <div class="col-sm-8">
                                    <html:select property="newModule" size="1" styleClass="form-control js-select">
                                        <logic:iterate id="aop" name="oplist" scope="request">
                                            <html:option value="${aop.value}"><bean:message key="${aop.key}"/></html:option>
                                        </logic:iterate>
                                    </html:select>
                                </div>
                            </div>
                        </div>
                        <div class="inline-tile-footer">
                            <emm:ShowByPermission token="actions.change">
                                <button class="btn btn-primary btn-regular" type="button" data-form-set="action: ${ACTION_ADD_MODULE}" data-form-submit>
                                    <i class="icon icon-plus-circle"></i>
                                    <bean:message key="button.Add"/>
                                </button>
                            </emm:ShowByPermission>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </agn:agnForm>
    <script id="modal-editor" type="text/x-mustache-template">
        <div class="modal modal-editor">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close" data-dismiss="modal">
                            <i aria-hidden="true" class="icon icon-times-circle"></i>
                        </button>
                        <h4 class="modal-title">{{= title }}</h4>
                    </div>
                    <div class="modal-body">
                        <textarea id="{{= id }}" data-sync="\#{{= target}}" class="form-control js-editor{{- (typeof(type) == 'undefined') ? '' : '-' + type }}"></textarea>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><bean:message key="button.Cancel"/></span>
                            </button>
                            <emm:ShowByPermission token="actions.change">
                                <button type="button" class="btn btn-primary btn-large" data-sync-from="\#{{= id }}" data-sync-to="\#{{= target }}" data-dismiss="modal" data-form-target="#emmActionForm" data-form-submit="">
                                    <i class="icon icon-save"></i>
                                    <span class="text"><bean:message key="button.Save"/></span>
                                </button>
                            </emm:ShowByPermission>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>

</c:if>
