<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>
<%--@elvariable id="userFormList" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="mailingLists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailing>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="filterEmailAddressDefault" type="java.lang.String"--%>
<%--@elvariable id="isAllowedMailloopDomain" type="java.lang.Boolean"--%>

<c:set var="isNew" value="${bounceFilterForm.id <= 0}"/>

<mvc:form servletRelativeAction="/administration/bounce/save.action" id="bounceFilterForm" modelAttribute="bounceFilterForm"
          data-form="resource">

    <mvc:hidden path="id"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.EditMailloop"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="shortName"><mvc:message code="Name"/>*</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortName" id="shortName" maxlength="99" size="32" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="description"><mvc:message code="Description"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="description" rows="5" cols="32" cssClass="form-control"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="report.mailing.filter" /></h2>
        </div>
        <div class="tile-content tile-content-forms" data-field="toggle-vis">
            <div class="hidden" data-field-vis-default="" data-field-vis-hide="#forward-field-container, #autoresponder-field-container"></div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="filterEmail">
                        <mvc:message code="mailloop.filter_adr"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/settings/BounceFilterAddress.xml"
                                tabindex="-1" type="button"></button>
                    </label>
                </div>
                <c:if test="${isAllowedMailloopDomain}">
                    <div class="col-sm-8">
                        <label class="radio-inline" for="radio-address-default">
                            <input type="radio" id="radio-address-default" name="addressType" data-field-vis="" data-field-vis-show="#filterEmailDefault"
                                   data-field-vis-hide="#filterEmailOwn, #ownForwardEmailSelected" ${bounceFilterForm.ownForwardEmailSelected ? '' : 'checked'} />
                            <mvc:message code="mailloop.address.default"/>
                        </label>
                        <label class="radio-inline" for="radio-address-individual">
                            <input type="radio" id="radio-address-individual" name="addressType" data-field-vis="" data-field-vis-show="#filterEmailOwn, #ownForwardEmailSelected"
                                   data-field-vis-hide="#filterEmailDefault" ${bounceFilterForm.ownForwardEmailSelected ? 'checked' : ''}/>
                            <mvc:message code="mailloop.address.individual"/>
                        </label>
                    </div>

            </div><%-- end this form-group and start the new--%>
            <div class="form-group">

                    <div class="col-sm-4"></div>
                </c:if>
                <div class="col-sm-8">
                    <c:if test="${not empty filterEmailAddressDefault}">
                        <input id="filterEmailDefault" type="text" class="form-control" value="${filterEmailAddressDefault}" readonly="true"/>
                    </c:if>
                    <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control"/>
                    <input id="ownForwardEmailSelected" name="ownForwardEmailSelected" class="hidden" value="true">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label checkbox-control-label" for="doForward"><mvc:message code="settings.mailloop.forward"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doForward" id="doForward" cssClass="tag-spring" data-field-vis=""
                                      data-field-vis-show="#forward-field-container"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div id="forward-field-container" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="forwardEmail"><mvc:message code="settings.mailloop.forward.address"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="forwardEmail" id="forwardEmail" maxlength="99" size="42" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label checkbox-control-label" for="doAutoRespond"> <mvc:message code="mailloop.autoresponder"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doAutoRespond" id="doAutoRespond" cssClass="tag-spring" data-field-vis=""
                                      data-field-vis-show="#autoresponder-field-container"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>

            <div id="autoresponder-field-container" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingListId"><mvc:message code="mailloop.autoresponder.mailing"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="arMailingId" id="arMailingId" cssClass="form-control js-select" size="1">
                        <c:forEach items="${actionBasedMailings}" var="mailing">
                            <mvc:option value="${mailing.mailingID}">${mailing.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
