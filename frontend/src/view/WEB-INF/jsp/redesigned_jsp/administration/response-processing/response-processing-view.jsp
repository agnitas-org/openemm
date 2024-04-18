<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>
<%--@elvariable id="userFormList" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="mailingLists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailing>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="filterEmailAddressDefault" type="java.lang.String"--%>
<%--@elvariable id="isAllowedMailloopDomain" type="java.lang.Boolean"--%>

<c:set var="isNew" value="${bounceFilterForm.id <= 0}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/administration/bounce/save.action"
                  id="bounceFilterForm" modelAttribute="bounceFilterForm"
                  data-form="resource"
                  data-disable-controls="save">
            <input type="hidden" name="forAddress" value="${forAddress}"/>

            <div class="modal-header">
                <h1 class="modal-title">
                <c:choose>
                    <c:when test="${isNew}">
                        <mvc:message code="settings.NewMailloop"/>
                    </c:when>
                    <c:otherwise>
                        <mvc:message code="settings.EditMailloop"/>
                    </c:otherwise>
                </c:choose>
                </h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body row g-3 pt-2">
                <mvc:hidden path="id"/>

                <div class="col-12">
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="shortName">${nameMsg}*</label>
                    <mvc:text path="shortName" id="shortName" maxlength="99" size="32" cssClass="form-control" data-field="required" placeholder="${nameMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description">${descriptionMsg}</label>
                    <mvc:textarea path="description" id="description" rows="3" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
                <div class="col-12">
                    <c:choose>
                        <c:when test="${isAllowedMailloopDomain and not empty filterEmailAddressDefault}">
                            <div class="switchable-input">
                                 <div class="switchable-input__header">
                                     <div>
                                         <label class="switchable-input__label" for="filterEmailOwn"><mvc:message code="response.address.individual"/></label>
                                         <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/settings/BounceFilterAddress.xml"></a>
                                     </div>
                                     <div class="switchable-input__switch">
                                         <input class="form-check-input" name="ownForwardEmailSelected" type="checkbox" role="switch" ${bounceFilterForm.ownForwardEmailSelected ? 'checked' : ''}>
                                     </div>
                                 </div>
                                 <div class="switchable-input__body">
                                     <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}" data-show-on-switch=""/>
                                     <input id="filterEmailOwn" type="text" class="form-control" value="${filterEmailAddressDefault}" data-hide-on-switch readonly/>
                                 </div>
                             </div>
                        </c:when>
                        <c:otherwise>
                            <label class="form-label" for="filterEmailOwn"><mvc:message code="response.address.individual"/></label>
                            <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="col-12">
                    <div class="switchable-input">
                        <div class="switchable-input__header">
                            <label class="switchable-input__label" for="forwardEmail"><mvc:message code="settings.mailloop.forward"/></label>
                            <div class="switchable-input__switch">
                                <mvc:checkbox path="doForward" cssClass="form-check-input" role="switch"/>
                            </div>
                        </div>
                        <div class="switchable-input__body">
                            <mvc:text path="forwardEmail" id="forwardEmail" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <div class="switchable-input">
                         <div class="switchable-input__header">
                             <label class="switchable-input__label" for="arMailingId"><mvc:message code="mailloop.autoresponder.mailing"/></label>
                             <div class="switchable-input__switch">
                                 <mvc:checkbox path="doAutoRespond" cssClass="form-check-input" role="switch"/>
                             </div>
                         </div>
                         <div class="switchable-input__body">
                             <mvc:select path="arMailingId" id="arMailingId" cssClass="form-control js-select" size="1">
                                 <c:forEach items="${actionBasedMailings}" var="mailing">
                                     <mvc:option value="${mailing.mailingID}">${mailing.shortname}</mvc:option>
                                 </c:forEach>
                             </mvc:select>
                         </div>
                     </div>
                </div>
            </div>
            <emm:ShowByPermission token="mailloop.change">
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary flex-grow-1" data-controls-group="save" data-form-submit>
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </emm:ShowByPermission>
        </mvc:form>
    </div>
</div>
