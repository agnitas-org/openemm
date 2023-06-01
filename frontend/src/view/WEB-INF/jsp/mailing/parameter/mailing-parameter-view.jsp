<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<agn:agnForm action="/mailingParameter" data-form="resource" id="mailingParameterForm">
    <html:hidden property="action"/>
    <html:hidden property="previousAction"/>
    <html:hidden property="mailingInfoID"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <c:choose>
                    <c:when test="${mailingParameterForm.mailingInfoID != 0}">
                        <bean:message key="MailingParameter.edit"/>
                    </c:when>
                    <c:otherwise>
                        <bean:message key="MailingParameter.new"/>
                    </c:otherwise>
                </c:choose>
            </h2>
        </div>
        <div class="tile-content">
            <div class="tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="parameter-name" class="control-label">
                            <c:set var="nameMsg"><bean:message key="default.Name"/></c:set>
                            ${nameMsg}*
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <agn:agnText styleClass="form-control" styleId="parameter-name" property="parameterName" maxlength="99" size="32" placeholder="${nameMsg}"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="parameter-description" class="control-label">
                            <c:set var="descriptionMsg"><bean:message key="default.description"/></c:set>
                            ${descriptionMsg}
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <agn:agnTextarea property="description" styleId="parameter-description" styleClass="form-control" rows="5" cols="32" placeholder="${descriptionMsg}"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="parameter-value" class="control-label">
                            <bean:message key="mailing.MailingParameter.value"/>*
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text styleClass="form-control" styleId="parameter-value" property="value" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                      <label for="mailinglistID" class="control-label">
                          <bean:message key="mailing.MailingParameter.forMailing"/>
                      </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select property="mailingID" size="1" styleId="mailinglistID" styleClass="js-select form-control">
                            <html:option value="0"><bean:message key="NoMailing"/></html:option>
                             <c:forEach var="mailing" items="${mailings}">
                                <html:option value="${mailing.mailingID}">
                                    ${mailing.shortname}
                                </html:option>
                            </c:forEach>
                        </html:select>
                    </div>
                </div>
            </div>
        </div>
    </div>

</agn:agnForm>
