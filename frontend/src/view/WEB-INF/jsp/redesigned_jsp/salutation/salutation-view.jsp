<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.importvalues.Gender" %>
<%@ page import="org.agnitas.backend.AgnTag" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.salutation.form.SalutationForm"--%>
<%--@elvariable id="salutationCompanyId" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="recipients" type="java.util.List<com.agnitas.emm.core.recipient.dto.RecipientSalutationDto>"--%>
<%--@elvariable id="recipient" type="com.agnitas.emm.core.recipient.dto.RecipientSalutationDto"--%>

<c:set var="MALE_GENDER" value="<%= Gender.MALE.getStorageValue() %>"/>
<c:set var="FEMALE_GENDER" value="<%= Gender.FEMALE.getStorageValue() %>"/>
<c:set var="UNKNOWN_GENDER" value="<%= Gender.UNKNOWN.getStorageValue() %>"/>
<c:set var="PRAXIS_GENDER" value="<%= Gender.PRAXIS.getStorageValue() %>"/>
<c:set var="COMPANY_GENDER" value="<%= Gender.COMPANY.getStorageValue() %>"/>

<c:set var="AGN_TAG_TITLE" value="<%= AgnTag.TITLE.getName() %>"/>
<c:set var="AGN_TAG_TITLE_FIRST" value="<%= AgnTag.TITLE_FIRST.getName() %>"/>
<c:set var="AGN_TAG_TITLE_FULL" value="<%= AgnTag.TITLE_FULL.getName() %>"/>

<mvc:message var="genderLabelMsg" code="Gender"/>

<c:if test="${empty id}">
    <c:set var="id" value="0"/>
</c:if>

<c:set var="readOnly" value="${salutationCompanyId eq 0}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/salutation/${id}/save.action" id="form" modelAttribute="form"
                  data-form="resource"
                  data-disable-controls="save"
                  data-controller="salutation-view"
                  data-initializer="salutation-view">

            <script id="config:salutation-view" type="application/json">
                {
                    "id": "${id}",
                    "AGN_TAG_TITLE": "${AGN_TAG_TITLE}",
                    "AGN_TAG_TITLE_FIRST": "${AGN_TAG_TITLE_FIRST}",
                    "AGN_TAG_TITLE_FULL": "${AGN_TAG_TITLE_FULL}"
                }
            </script>

            <div class="modal-header">
                <h1 class="modal-title" style="min-width: fit-content"><mvc:message code="${empty id or id == 0 ? 'default.salutation.shortname' : 'settings.EditFormOfAddress'}"/></h1>
                <div class="input-group w-100">
                    <c:if test="${id ne 0}">
                        <span class="input-group-text input-group-text--disabled"><mvc:message code="MailinglistID"/></span>
                        <span class="input-group-text input-group-text--disabled">${id}</span>
                    </c:if>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body form-column-2">
                <div class="form-column">
                    <div>
                        <mvc:message var="nameMsg" code="Name"/>
                        <label class="form-label" for="salutation-description">${nameMsg}&nbsp;*</label>
                        <mvc:text path="description" readonly="${readOnly}" id="salutation-description" cssClass="form-control" placeholder="${nameMsg}" data-field="required"/>
                    </div>

                    <div>
                        <label for="salutation-male" class="form-label"><mvc:message code="Male"/> (${genderLabelMsg} ${MALE_GENDER})</label>
                        <mvc:text path="genderMapping[${MALE_GENDER}]" readonly="${readOnly}" id="salutation-male"
                                  cssClass="form-control" placeholder="Dear..." data-action="switch-salutation"/>
                    </div>

                    <div>
                        <label for="salutation-female" class="form-label"><mvc:message code="Female"/> (${genderLabelMsg} ${FEMALE_GENDER})</label>
                        <mvc:text path="genderMapping[${FEMALE_GENDER}]" readonly="${readOnly}" id="salutation-female"
                                  cssClass="form-control" placeholder="Dear..." data-action="switch-salutation"/>
                    </div>

                    <div>
                        <label for="salutation-unknown" class="form-label"><mvc:message code="Unknown"/> (${genderLabelMsg} ${UNKNOWN_GENDER})</label>
                        <mvc:text path="genderMapping[${UNKNOWN_GENDER}]" readonly="${readOnly}" id="salutation-unknown"
                                  cssClass="form-control" placeholder="Dear..." data-action="switch-salutation"/>
                    </div>

                    <emm:ShowByPermission token="recipient.gender.extended">
                        <div>
                            <label for="salutation-practice" class="form-label"><mvc:message code="PracticeShort"/> (${genderLabelMsg} ${PRAXIS_GENDER})</label>
                            <mvc:text path="genderMapping[${PRAXIS_GENDER}]" readonly="${readOnly}" id="salutation-practice"
                                      cssClass="form-control" placeholder="Dear..." data-action="switch-salutation"/>
                        </div>

                        <div>
                            <label for="salutation-company" class="form-label"><mvc:message code="recipient.gender.5.short"/> (${genderLabelMsg} ${COMPANY_GENDER})</label>
                            <mvc:text path="genderMapping[${COMPANY_GENDER}]" readonly="${readOnly}" id="salutation-company"
                                      cssClass="form-control" placeholder="Dear..." data-action="switch-salutation"/>
                        </div>
                    </emm:ShowByPermission>
                </div>
                <div class="d-flex flex-column">
                    <label class="form-label"><mvc:message code="Preview"/></label>
                    <div class="border rounded-2 p-2 d-flex flex-column gap-2 flex-grow-1">
                        <select id="recipient" class="form-control" data-action="switch-salutation">
                            <c:forEach var="recipient" items="${recipients}">
                                <option value="${recipient.email}"
                                        data-firstname="${recipient.firstname}"
                                        data-lastname="${recipient.lastname}"
                                        data-title="${recipient.title}"
                                        data-gender="${recipient.gender}">${recipient.firstname} ${recipient.lastname} (${recipient.email})</option>
                            </c:forEach>
                        </select>
                        <select id="agnTag" class="form-control" data-action="switch-salutation">
                            <option value='${AGN_TAG_TITLE}'>${AGN_TAG_TITLE}</option>
                            <option value='${AGN_TAG_TITLE_FULL}'>${AGN_TAG_TITLE_FULL}</option>
                            <option value='${AGN_TAG_TITLE_FIRST}'>${AGN_TAG_TITLE_FIRST}</option>
                        </select>
                        <div class="border rounded-2 p-2 flex-grow-1 d-flex flex-column gap-3">
                            <p id="salutation-preview"></p>
                            <p class="text-muted">
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ex magna lorem ex nibh eiusmod placerat nihil erat veniam voluptua cupiditat nostrud molestie.
                            </p>
                            <p class="text-muted" style="
                                display: -webkit-box;
                                -webkit-line-clamp: 5;
                                -webkit-box-orient: vertical;
                                overflow: hidden;">
                                Voluptate clita in nihil culpa aliquam congue congue quis culpa, elitr eum mazim placerat vel nibh ut ex tempor sadipscing ad doming nostrud nobis sint proident sea labore te. Diam dolores takimata deserunt aliqua duo, eiusmod id dignissim ea aliquyam eu aliquam liber voluptua cum takimata magna option laborum tempor obcaecat anim. Sanctus illum delenit euismod diam placerat est duo soluta culpa dolores feugiat obcaecat nibh amet ea anim sea quis elit iure adipiscing. Sea facilisi sunt.
                            </p>
                        </div>
                        <button id="copy-salutation" type="button" class="btn btn-info" data-copyable data-copyable-value="[agnTITLE type='${id}']">
                            <i class="icon icon-copy"></i>
                            <mvc:message code="button.mailing.agntag.copy"/>
                        </button>
                    </div>
                </div>
            </div>

            <emm:ShowByPermission token="salutation.change">
                <c:if test="${not readOnly}">
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" data-controls-group="save" data-form-submit>
                            <i class="icon icon-save"></i>
                            <mvc:message code="button.Save"/>
                        </button>
                    </div>
                </c:if>
            </emm:ShowByPermission>
        </mvc:form>
    </div>
</div>
