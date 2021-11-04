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

<div id="tab-advancedSearch" class="hidden" ${forceShowAdvancedSearchTab ? 'data-tab-show="true"' : ''}>
    <div class="tile-content-forms" style="padding-bottom: 0">
        <div class="row">
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_mailinglist_advanced">
                            <bean:message key="Mailinglist"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_mailinglist_advanced" styleClass="form-control js-select"
                                       property="listID" titleKey="default.All"
                                       data-action="change-fields-to-search-advanced"
                                       data-form-change="0">
                            <html:option value="0" key="default.All"/>
                            <c:if test="${not hasAnyDisabledMailingLists}">
                                <agn:agnOption value="-1"><bean:message key="No_Mailinglist"/></agn:agnOption>
                            </c:if>
                            <c:forEach var="mailinglist" items="${mailinglists}">
                                <html:option value="${mailinglist.id}">${mailinglist.shortname}</html:option>
                            </c:forEach>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_targetgroup_advanced">
                            <bean:message key="Target"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_targetgroup_advanced"
                                       styleClass="form-control js-select" property="targetID"
                                       data-action="change-target-group-advanced" data-form-change="0"
                                       titleKey="default.All">
                            <html:option value="0" key="default.All"/>
                            <c:forEach var="target" items="${targets}">
                                <html:option value="${target.id}">${target.targetName}</html:option>
                            </c:forEach>
                        </agn:agnSelect>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label" for="search_recipient_type_advanced">
                            <bean:message key="recipient.RecipientType"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_recipient_type_advanced"
                                       styleClass="form-control js-select" property="user_type"
                                       data-action="change-recipient-type-advanced" data-form-change="0"
                                       titleKey="default.All"
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
                        <label class="control-label" for="search_recipient_state_advanced">
                            <bean:message key="recipient.RecipientStatus"/>
                        </label>
                    </div>
                    <div class="col-md-12">
                        <agn:agnSelect styleId="search_recipient_state_advanced"
                                       styleClass="form-control" property="user_status"
                                       data-action="change-user-status-advanced" data-form-change="0"
                                       titleKey="default.All"
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
            <div class="col-md-12">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label">
                        </label>
                    </div>
                    <div class="col-md-12">
                        <c:set var="FORM_NAME" value="recipientForm" scope="page"/>
                        <c:set var="HIDE_SPECIAL_TARGET_FEATURES" value="true" scope="page"/>
                        <%@include file="/WEB-INF/jsp/rules/rule_add.jsp" %>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
        <div class="row">
            <div class="col-md-12">
                <div class="form-group">
                    <div class="col-md-12">
                        <label class="control-label">
                        </label>
                    </div>
                    <div class="col-md-12">
                        </c:if>
                        <%@include file="/WEB-INF/jsp/rules/rules_list.jsp" %>
                        <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
                    </div>
                </div>
            </div>
        </div>
        </c:if>

        <div class="row">
            <div class="col-sm-12">
                <div class="form-group">
                    <div class="col-sm-12">
                        <button type="button" tabindex="-1" class="btn btn-regular"
                                data-help="help_${helplanguage}/recipient/AdvancedSearchMsg.xml">
                            <i class="icon icon-question-circle"></i>
                            <bean:message key="help"/>
                        </button>

                        <div class="btn-group pull-right">
                            <button type="button" class="btn btn-regular"
                                    data-form-set="resetSearch:true" data-action="reset-search">
                                <bean:message key="button.search.reset"/></button>
                            <!-- hide if no queries are present BEGIN -->
                            <c:if test="${fn:length(recipientForm.allColumnsAndTypes) > 0}">
                                <button type="button" tabindex="-1" data-modal="target-group-save"
                                        class="btn btn-regular">
                                    <bean:message key="recipient.saveSearch"/>
                                </button>
                            </c:if>
                            <!-- hide if no queries are present END -->

                            <button id="refresh-button"
                                    class="btn btn-primary btn-regular" type="button"
                                    data-form-set="advancedSearch:true"
                                    data-form-submit>
                                <i class="icon icon-refresh"></i>
                                <span class="text"><bean:message key="button.Refresh"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <hr>
</div>
