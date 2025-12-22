<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>
<%--@elvariable id="userFormList" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="mailingLists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List<com.agnitas.emm.core.mailing.bean.LightweightMailing>"--%>
<%--@elvariable id="filterEmailAddressDefault" type="java.lang.String"--%>
<%--@elvariable id="isAllowedMailloopDomain" type="java.lang.Boolean"--%>

<c:set var="isNew" value="${bounceFilterForm.id <= 0}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/administration/bounce/save.action"
                  id="bounceFilterForm" modelAttribute="bounceFilterForm" data-resource-selector="${not empty forAddress ? '.modal' : ''}"
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
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body vstack gap-3 js-scrollable">
                <mvc:hidden path="id"/>

                <div>
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="shortName">${nameMsg} *</label>
                    <mvc:text path="shortName" id="shortName" maxlength="99" size="32" cssClass="form-control" data-field="required" placeholder="${nameMsg}"/>
                </div>

                <div>
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description">${descriptionMsg}</label>
                    <mvc:textarea path="description" id="description" rows="1" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>

                <c:choose>
                    <c:when test="${isAllowedMailloopDomain and not empty filterEmailAddressDefault}">
                        <div class="tile tile--switchable">
                            <div class="tile-header">
                                <div class="form-check form-switch">
                                    <mvc:checkbox cssClass="form-check-input" path="ownForwardEmailSelected" role="switch"/>
                                </div>
                                <label class="form-label m-0" for="filterEmailOwn"><mvc:message code="response.address.individual"/></label>
                                <a href="#" class="icon icon-question-circle" data-help="settings/BounceFilterAddress.xml"></a>
                            </div>
                            <div class="tile-body">
                                <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}" data-show-on-switch=""/>
                                <input id="filterEmailOwn" type="text" class="form-control" value="${filterEmailAddressDefault}" data-hide-on-switch readonly/>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div>
                            <label class="form-label" for="filterEmailOwn"><mvc:message code="response.address.individual"/></label>
                            <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="tile tile--switchable">
                    <div class="tile-header">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="doForward" cssClass="form-check-input" role="switch"/>
                        </div>
                        <label class="form-label m-0" for="forwardEmail"><mvc:message code="settings.mailloop.forward"/></label>
                        <a href="#" class="icon icon-question-circle" data-help="settings/ForwardFilteredMails.xml"></a>
                    </div>
                    <div class="tile-body">
                        <mvc:text path="forwardEmail" id="forwardEmail" maxlength="99" size="42" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                    </div>
                </div>

                <div class="tile tile--switchable">
                    <div class="tile-header">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="doAutoRespond" cssClass="form-check-input" role="switch"/>
                        </div>
                        <label class="form-label m-0" for="arMailingId"><mvc:message code="mailloop.autoresponder.mailing"/></label>
                    </div>
                    <div class="tile-body">
                        <mvc:select path="arMailingId" id="arMailingId" cssClass="form-control" size="1">
                            <c:forEach items="${actionBasedMailings}" var="mailing">
                                <mvc:option value="${mailing.mailingID}">${mailing.shortname}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            </div>
            <emm:ShowByPermission token="mailloop.change">
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-controls-group="save" data-form-submit>
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </emm:ShowByPermission>
        </mvc:form>
    </div>
</div>
