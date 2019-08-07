<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>

<c:set var="isChangable" value="${false}"/>

<emm:ShowByPermission token="mailing.parameter.change">
    <c:set var="isChangable" value="${true}"/>
</emm:ShowByPermission>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingParameters">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="MailingParameter"/>
        </a>
    </div>

    <div id="tile-mailingParameters" class="tile-content tile-content-forms">
        <div class="table-responsive">
            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <th><bean:message key="default.Name"/></th>
                        <th><bean:message key="Value"/></th>
                        <th><bean:message key="default.description"/></th>
                        <c:if test="${isChangable}">
                            <th></th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="parameter" items="${mailingBaseForm.parameterMap}" varStatus="status">
                        <tr>
                            <td>
                                <c:if test="${isChangable}">
                                    <html:hidden property="parameter[${status.index + 1}].mailingInfoID" value="${parameter.value.mailingInfoID}"/>
                                </c:if>
                                <html:text property="parameter[${status.index + 1}].name" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>
                            <td>
                                <html:text property="parameter[${status.index + 1}].value" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>
                            <td>
                                <html:text property="parameter[${status.index + 1}].description" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>

                            <c:if test="${isChangable}">
                            <td class="table-actions">
                                <button type="button" class="btn btn-regular btn-alert" data-action="deleteMailingParameter" data-controls-group="save" data-tooltip="<bean:message key="button.Delete"/>">
                                    <i class="icon icon-trash-o"></i>
                                </button>
                            </td>
                            </c:if>
                        </tr>
                    </c:forEach>

                    <c:if test="${isChangable}">
                        <c:if test="${fn:length(mailingBaseForm.parameterMap) > 0}">
                            <th colspan="4"><bean:message key="mailing.newParameter"/></th>
                        </c:if>

                        <tr>
                            <td>
                                <html:hidden property="parameter[0].mailingInfoID" value="0"/>
                                <html:text property="parameter[0].name" value="" styleClass="form-control"/>
                            </td>
                            <td>
                                <html:text property="parameter[0].value" value="" styleClass="form-control"/>
                            </td>
                            <td>
                                <html:text property="parameter[0].description" value="" styleClass="form-control"/>
                            </td>

                            <c:if test="${isChangable}">
                            <td class="table-actions">
                                <a href="#" class="btn btn-regular btn-primary" data-action="createMailingParameter" data-controls-group="save" data-tooltip="<bean:message key="button.Add"/>">
                                    <i class="icon icon-plus"></i>
                                </a>
                            </td>
                            </c:if>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>
