<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="components" type="java.util.List<org.agnitas.beans.MailingComponent>"--%>
<%--@elvariable id="isTagFailureInFromAddress" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInSubject" type="java.lang.Boolean"--%>

<c:set var="storedFieldsScope" value="${mailingSendForm.mailingID}"/>

<div id="mailing-preview-header" class="mailing-preview-header">
    <table class="table-list" data-controller="preview-header">
        <tr>
            <c:choose>
                <c:when test="${isTagFailureInFromAddress}">
                    <th style="color: red;" title="<bean:message key="error.template.dyntags"/>">
                        <bean:message key="ecs.From"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><bean:message key="ecs.From"/></th>
                </c:otherwise>
            </c:choose>
            <td><b><bean:write name="mailingSendForm" property="senderPreview"/></b></td>
        </tr>
        <tr>
            <th><bean:message key="To"/></th>
            <td>
                <ul>
                    <li>
                        <label for="preview_customer_ATID" class="radio-inline"></label>
                        <agn:agnRadio styleId="preview_customer_ATID" property="useCustomerEmail"  value="false" data-action="change-preview-customer-options" data-stored-field="${storedFieldsScope}"/>
                        <select id="selectPreviewCustomerATID" name="previewCustomerATID" class="js-select" data-form-submit>
                            <option value="0" <c:if test="${mailingSendForm.previewCustomerATID == 0}">selected="selected"</c:if>><bean:message key="default.select.email"/></option>
                            <c:forEach var="recipient" items="${previewRecipients}">
                                <option value="${recipient.key}" <c:if test="${mailingSendForm.previewCustomerATID == recipient.key || mailingSendForm.previewCustomerID == recipient.key}">selected="selected"</c:if>>${recipient.value}</option>
                            </c:forEach>
                        </select>
                    </li>
                    <li>
                        <label for="preview_customer_Email" class="radio-inline"></label>
                        <agn:agnRadio styleId="preview_customer_Email" property="useCustomerEmail" value="true" data-action="change-preview-customer-options" data-stored-field="${storedFieldsScope}"/>
                        <div class="input-group align-middle inline-block" style="max-width: 300px;">
                            <div class="input-group-controls">
                                <agn:agnText styleId="textPreviewCustomerEmail" property='previewCustomerEmail' styleClass="form-control" data-stored-field="${storedFieldsScope}"/>
                            </div>
                            <div class="input-group-btn">
                                <button id="buttonPreviewCustomerEmail" type="button" class="btn btn-regular" data-form-submit><bean:message key="default.enter.email"/></button>
                            </div>
                        </div>
                    </li>
                </ul>
            </td>
        </tr>
        <tr>
            <c:choose>
                <c:when test="${isTagFailureInSubject}">
                    <th style="color: red;" title="<bean:message key="error.template.dyntags"/>">
                        <bean:message key="mailing.Subject"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><bean:message key="mailing.Subject"/></th>
                </c:otherwise>
            </c:choose>
            <td><b><bean:write name="mailingSendForm" property="subjectPreview"/></b></td>
        </tr>

        <c:forEach var="component" items="${components}" varStatus="status">
            <emm:CustomerMatchTarget customerID="${mailingSendForm.previewCustomerID}" targetID="${component.targetID}">
                <tr>
                    <th class="align-top">
                        <c:if test="${status.first}">
                            <bean:message key="mailing.Attachments"/><br>
                        </c:if>
                    </th>
                    <td class="align-top">
                        <agn:agnLink
                            page="/sc?compID=${component.id}&mailingID=${mailingSendForm.mailingID}&customerID=${mailingSendForm.previewCustomerID}" data-prevent-load="">
                            ${component.componentName}
                            <span class="badge"><i class="icon icon-download"></i></span>
                        </agn:agnLink>
                    </td>
                </tr>
            </emm:CustomerMatchTarget>
        </c:forEach>
    </table>
</div>
