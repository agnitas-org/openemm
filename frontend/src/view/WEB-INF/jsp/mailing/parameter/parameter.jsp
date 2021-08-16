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

<div class="tile" data-action="scroll-to">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingParameters">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="MailingParameter"/>
        </a>
    </div>

    <div id="tile-mailingParameters" class="tile-content tile-content-forms">
        <div class="table-responsive" data-controller="mailing-parameters">
            <c:set var="maxIndex" value="${fn:length(mailingBaseForm.parameterMap)}"/>
            <script data-initializer="mailing-parameters" type="application/json">
                {
                  "maxIndex": ${maxIndex}
                }
            </script>
            <table class="table table-bordered table-striped" id="mailingParamsTable">
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
                                <html:text property="parameter[${parameter.key}].name" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>
                            <td>
                                <html:text property="parameter[${parameter.key}].value" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>
                            <td>
                                <html:text property="parameter[${parameter.key}].description" styleClass="form-control" disabled="${not isChangable}"/>
                            </td>

                            <c:if test="${isChangable}">
                            <td class="table-actions">
                                <a href="#" class="btn btn-regular btn-alert" data-action="remove-mailing-parameter" id="removeMailingParameterBtn" data-tooltip="<bean:message key="button.Delete"/>">
                                    <i class="icon icon-trash-o"></i>
                                </a>
                            </td>
                            </c:if>
                        </tr>
                    </c:forEach>

                    <c:if test="${isChangable}">
                        <c:if test="${maxIndex > 0}">
                            <th colspan="4"><bean:message key="mailing.newParameter"/></th>
                        </c:if>

                        <tr>
                            <td>
                                <html:hidden property="parameter[${maxIndex}].mailingInfoID" value="0"/>
                                <html:text property="parameter[${maxIndex}].name" value="" styleClass="form-control"/>
                            </td>
                            <td>
                                <html:text property="parameter[${maxIndex}].value" value="" styleClass="form-control"/>
                            </td>
                            <td>
                                <html:text property="parameter[${maxIndex}].description" value="" styleClass="form-control"/>
                            </td>

                            <c:if test="${isChangable}">
                            <td class="table-actions">
                                <a href="#" class="btn btn-regular btn-primary" data-action="add-mailing-parameter"
                                   id="newMailingParameterBtn" data-tooltip="<bean:message key="button.Add"/>">
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

    <script id="mailing-param-row-new" type="text/x-mustache-template">
        <tr>
            <td>
                <input name="parameter[{{= key }}].name" class="form-control"/>
            </td>
            <td>
                <input name="parameter[{{= key }}].value" class="form-control"/>
            </td>
            <td>
                <input name="parameter[{{= key }}].description" class="form-control"/>
            </td>

            <td class="table-actions">
                <a href="#" class="btn btn-regular btn-primary" data-action="add-mailing-parameter"
                   id="newMailingParameterBtn"><i class="icon icon-plus"></i></a>
            </td>
        </tr>
    </script>
</div>
