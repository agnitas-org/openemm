<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>

<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="recipientForm" type="com.agnitas.web.ComRecipientForm"--%>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map"--%>
<%--@elvariable id="hasAnyDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="recipientList" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<div id="tab-basicSearch" class="hidden">
    <div class="tile-content-forms" style="padding-bottom: 0">
        <div class="row">
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_mailinglist">
                            <bean:message key="Mailinglist"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_mailinglist" styleClass="form-control js-select"
                                       data-action="change-ml-field" property="listID"
                                       data-form-change="0" titleKey="default.All">
                            <html:option value="0" key="default.All"/>
                            <c:if test="${hasAnyDisabledMailingLists == false}">
                                <agn:agnOption value="-1"><bean:message key="No_Mailinglist"/></agn:agnOption>
                            </c:if>
                            <c:forEach var="mailinglist" items="${mailinglists}">
                                <html:option
                                        value="${mailinglist.id}">${mailinglist.shortname}</html:option>
                            </c:forEach>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_targetgroup">
                            <bean:message key="Target"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_targetgroup" styleClass="form-control js-select"
                                       data-action="change-target-group" property="targetID"
                                       data-form-change="0" titleKey="default.All">
                            <html:option value="0" key="default.All"/>
                            <c:forEach var="target" items="${targets}">
                                <html:option value="${target.id}">
                                    ${target.targetName}
                                </html:option>
                            </c:forEach>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_recipient_type">
                            <bean:message key="recipient.RecipientType"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_recipient_type"
                                       styleClass="form-control js-select"
                                       data-action="change-recipient-type" property="user_type"
                                       data-form-change="0" titleKey="default.All"
                                       disabled="${recipientForm.listID == -1}">
                            <html:option value="" key="default.All"/>
                            <html:option value="A" key="recipient.Administrator"/>
                            <html:option value="T" key="TestSubscriber"/>
                            <%@include file="/WEB-INF/jsp/recipient/recipient-novip-test.jspf" %>
                            <html:option value="W" key="NormalSubscriber"/>
                            <%@include file="/WEB-INF/jsp/recipient/recipient-novip-normal.jspf" %>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_recipient_state">
                            <bean:message key="recipient.RecipientStatus"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_recipient_state" styleClass="form-control"
                                       property="user_status" data-action="change-user-status"
                                       data-form-change="0" titleKey="default.All"
                                       disabled="${recipientForm.listID == -1}">
                            <html:option value="0" key="default.All"/>
                            <html:option value="1" key="recipient.MailingState1"/>
                            <html:option value="2" key="recipient.MailingState2"/>
                            <html:option value="3" key="recipient.OptOutAdmin"/>
                            <html:option value="4" key="recipient.OptOutUser"/>
                            <html:option value="5" key="recipient.MailingState5"/>
                            <emm:ShowByPermission token="blacklist">
                                <html:option value="6" key="recipient.MailingState6"/>
                            </emm:ShowByPermission>
                            <html:option value="7" key="recipient.MailingState7"/>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_first_name" class="control-label">
                            <bean:message key="Firstname"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <html:text styleId="search_first_name" styleClass="form-control"
                                   property="searchFirstName"/>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_name" class="control-label">
                            <bean:message key="Lastname"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <html:text styleId="search_name" styleClass="form-control"
                                   property="searchLastName"/>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="form-group">
                    <div class="col-md-12">
                        <label for="search_email" class="control-label">
                            <bean:message key="mailing.MediaType.0"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <html:text styleId="search_email" styleClass="form-control"
                                   property="searchEmail"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-12">
                <div class="form-group">
                    <div class="col-sm-12">
                        <button type="button" tabindex="-1" class="btn btn-regular"
                                data-help="help_${helplanguage}/recipient/SearchMsg.xml">
                            <i class="icon icon-question-circle"></i>
                            <bean:message key="help"/>
                        </button>

                        <div class="btn-group pull-right">
                            <button type="button" class="btn btn-regular"
                                    data-form-set="resetSearch:true" data-action="reset-search">
                                <bean:message key="button.search.reset"/>
                            </button>

                            <c:if test="${recipientList.getFullListSize() <= 0}">
                                <c:set var="isCreateRecipientButtonShown" value="false"/>

                                <c:url var="createNewRecipientUrl" value="/recipient.do">
                                    <c:param name="action" value="${ACTION_VIEW}"/>
                                    <c:param name="trgt_clear" value="1"/>

                                    <c:if test="${not empty fn:trim(recipientForm.searchFirstName)}">
                                        <c:param name="firstname" value="${recipientForm.searchFirstName}"/>
                                        <c:set var="isCreateRecipientButtonShown" value="true"/>
                                    </c:if>
                                    <c:if test="${not empty fn:trim(recipientForm.searchLastName)}">
                                        <c:param name="lastname" value="${recipientForm.searchLastName}"/>
                                        <c:set var="isCreateRecipientButtonShown" value="true"/>
                                    </c:if>
                                    <c:if test="${not empty fn:trim(recipientForm.searchEmail)}">
                                        <c:param name="email" value="${recipientForm.searchEmail}"/>
                                        <c:set var="isCreateRecipientButtonShown" value="true"/>
                                    </c:if>
                                </c:url>

                                <c:if test="${isCreateRecipientButtonShown}">
                                    <a href="${createNewRecipientUrl}" class="btn btn-primary btn-regular">
                                        <i class="icon icon-plus"></i>
                                        <span class="text"><bean:message key="button.create.recipient"/></span>
                                    </a>
                                </c:if>
                            </c:if>

                            <button class="btn btn-primary btn-regular pull-right" type="button"
                                    data-action="refresh-basic-search" data-form-persist="page: '1'">
                                <i class="icon icon-search"></i>
                                <span class="text"><bean:message key="Search"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <hr>
</div>
