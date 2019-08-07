<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.*, com.agnitas.web.forms.*" %>
<%@ page import="com.agnitas.web.ComMailingBaseAction" %>
<%@ page import="org.agnitas.emm.core.commons.util.Constants" %>
<%@ page import="com.agnitas.reporting.birt.web.ComMailingBIRTStatAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="mailinglist" type="org.agnitas.beans.impl.PaginatedListImpl"--%>

<c:set var="ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>
<c:set var="ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW_TABLE_ONLY" value="<%= MailingBaseAction.ACTION_VIEW_TABLE_ONLY %>"/>
<c:set var="ACTION_USED_ACTIONS" value="<%= MailingBaseAction.ACTION_USED_ACTIONS %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComMailingBaseAction.ACTION_BULK_CONFIRM_DELETE %>"/>
<c:set var="ACTION_MAILINGSTAT" value="<%= ComMailingBIRTStatAction.ACTION_MAILINGSTAT %>"/>

<c:set var="DATE_PATTERN" value="<%= Constants.DATE_PATTERN %>"/>

<c:choose>
    <c:when test="${mailingBaseForm.isTemplate}">
        <emm:Permission token="template.show"/>
    </c:when>
    <c:otherwise>
        <emm:Permission token="mailing.show"/>
    </c:otherwise>
</c:choose>

<agn:agnForm action="/mailingbase" data-form="search" id="mailingBaseForm">
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="action"/>
    <html:hidden property="actionList" value="${ACTION_LIST}"/>
    <html:hidden property="previousAction"/>

    <html:hidden property="__STRUTS_MULTIPLE_filterStatus" value=""/>
    <html:hidden property="__STRUTS_MULTIPLE_badgeFilters" value=""/>
    <html:hidden property="__STRUTS_MULTIPLE_filterMailingList" value=""/>

    <html:hidden property="__STRUTS_MULTIPLE_selectedFields" value=""/>
    <c:if test="${not mailingBaseForm.isTemplate}">

    <html:hidden property="__STRUTS_CHECKBOX_mailingTypeNormal" value="false"/>
    <html:hidden property="__STRUTS_CHECKBOX_mailingTypeEvent" value="false"/>
    <html:hidden property="__STRUTS_CHECKBOX_mailingTypeDate" value="false"/>
    <html:hidden property="__STRUTS_CHECKBOX_mailingTypeInterval" value="false"/>

    <logic:equal name="mailingBaseForm" property="searchEnabled" value="true">
        <html:hidden property="__STRUTS_CHECKBOX_searchNameChecked" value="false"/>
        <html:hidden property="__STRUTS_CHECKBOX_searchDescriptionChecked" value="false"/>
        <html:hidden property="__STRUTS_CHECKBOX_searchContentChecked" value="false"/>

        <div class="tile">
            <div class="tile-header">
                <a class="headline" href="#" data-toggle-tile="#tile-mailingSearch">
                    <i class="icon tile-toggle icon-angle-up"></i>
                    <bean:message key="Search"/>
                </a>
                <ul class="tile-header-actions">
                    <li>
                        <button class="btn btn-primary btn-regular" type="button" data-form-submit="">
                            <i class="icon icon-search"></i>
                            <span class="text"><bean:message key="Search"/></span>
                        </button>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms form-vertical" id="tile-mailingSearch">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <div class="col-md-12">
                                <label class="control-label">
                                    <label for="searchQueryText"><bean:message key="mailing.searchFor"/></label>
                                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/overview/SearchFor.xml" tabindex="-1" type="button"></button>
                                </label>
                            </div>
                            <div class="col-md-12">
                                <html:text styleClass="form-control" property="searchQueryText" styleId="searchQueryText"/>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="form-group">
                            <div class="col-md-12">
                                <label for="placesToSearch" class="control-label"><bean:message key="mailing.searchIn"/></label>
                            </div>
                            <div class="col-md-12">
                                <ul class="list-group" style="margin-bottom: 0px;" id="placesToSearch">
                                    <li class="list-group-item checkbox">
                                        <label>
                                            <html:checkbox value="true" property="searchNameChecked"/>
                                            <bean:message key="mailing.searchName"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item checkbox">
                                        <label>
                                            <html:checkbox value="true" property="searchDescriptionChecked"/>
                                            <bean:message key="mailing.searchDescription"/>
                                        </label>
                                    </li>
                                    <logic:equal name="mailingBaseForm" property="contentSearchEnabled" value="true">
                                        <li class="list-group-item checkbox">
                                            <label>
                                                <html:checkbox value="true" property="searchContentChecked"/>
                                                <bean:message key="mailing.searchContent"/>
                                            </label>
                                        </li>
                                    </logic:equal>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </logic:equal>

   </c:if>

    <!-- Tile BEGIN -->
    <div class="tile" data-form-content="">
        <c:choose>
            <c:when test="${mailingBaseForm.isTemplate}">
                <script type="application/json" data-initializer="web-storage-persist">
                    {
                        "mailing-overview": {
                            "rows-count": ${mailingBaseForm.numberOfRows}
                        }
                    }
                </script>
            </c:when>
            <c:otherwise>
                <script type="application/json" data-initializer="web-storage-persist">
                    {
                        "mailing-overview": {
                            "rows-count": ${mailingBaseForm.numberOfRows},
                            "fields": ${emm:toJson(mailingBaseForm.selectedFields)},
                            "mailing-type-normal": ${mailingBaseForm.mailingTypeNormal},
                            "mailing-type-date": ${mailingBaseForm.mailingTypeDate},
                            "mailing-type-event": ${mailingBaseForm.mailingTypeEvent},
                            "mailing-type-followup": ${mailingBaseForm.mailingTypeFollowup},
                            "mailing-type-interval": ${mailingBaseForm.mailingTypeInterval}
                        }
                    }
                </script>
            </c:otherwise>
        </c:choose>

        <!-- Tile Header BEGIN -->
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Overview"/></h2>

            <c:if test="${not mailingBaseForm.isTemplate}">
                <ul class="tile-header-nav">
                    <li class="active">
                        <a href="#" data-toggle-tab="#mailing"><bean:message key="default.list"/></a>
                    </li>
                    <li>
                        <a href="#" data-toggle-tab="#mailings-overviewPreview"><bean:message key="default.Preview"/></a>
                    </li>
                </ul>
            </c:if>

            <!-- Tile Header Actions BEGIN -->
            <ul class="tile-header-actions">
                <emm:ShowByPermission token="mailing.delete|template.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><bean:message key="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <c:if test="${!mailingBaseForm.isTemplate}">
                                	<emm:ShowByPermission token="mailing.delete">
                                	<a href="#" data-form-confirm="${ACTION_BULK_CONFIRM_DELETE}">
                                    	<bean:message key="bulkAction.delete.mailing"/>
                                	</a>
                                	</emm:ShowByPermission>
                                </c:if>
                                <c:if test="${mailingBaseForm.isTemplate}">
                                	<emm:ShowByPermission token="template.delete">
                                	<a href="#" data-form-confirm="${ACTION_BULK_CONFIRM_DELETE}">
                                    	<bean:message key="bulkAction.delete.template"/>
                                	</a>
                                	</emm:ShowByPermission>
                                </c:if>
                            </li>

                        </ul>

                    </li>
                </emm:ShowByPermission>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                      <i class="icon icon-eye"></i>
                      <span class="text"><bean:message key="button.Show"/></span>
                      <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <c:if test="${!mailingBaseForm.isTemplate}">
                        <li class="dropdown-header"><bean:message key="mailing.types"/></li>
                        <li>
                            <label class="label">
                                <html:checkbox property="mailingTypeNormal" styleClass="js-form-change" />
                                <bean:message key="Normal"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <html:checkbox property="mailingTypeEvent" styleClass="js-form-change" />
                                <bean:message key="mailing.event"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <html:checkbox property="mailingTypeDate" styleClass="js-form-change" />
                                <bean:message key="mailing.date" />
                            </label>
                        </li>
                        <%@include file="mailing-list-follow-form.jspf" %>
                        <%@include file="mailing-list-interval-form.jspf" %>
                        <li class="divider"></li>
                        </c:if>
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                            <logic:iterate collection="${mailingBaseForm.columnwidthsList}" indexId="i" id="width">
                              <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
				<c:if test="${!mailingBaseForm.isTemplate}"> 
                    <li class="dropdown">
                         <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                             <i class="icon icon-columns"></i>
                             <span class="text"><bean:message key="settings.fields"/></span>
                             <i class="icon icon-caret-down"></i>
                         </a>
                         <ul class="dropdown-menu">
                             <li class="dropdown-header"><bean:message key="settings.fields"/></li>
                             <li>
                                 <p>
                                     <select class="form-control js-select" tabindex="-1" multiple="multiple" name="selectedFields">
                                         <c:forEach var="field" items="${fieldsMap}">
                                             <c:set var="fieldSelected" value=""/>
                                             <c:forEach var="selectedField" items="${mailingBaseForm.selectedFields}" varStatus="rowCounter">
                                                 <c:if test="${field.sortColumn == selectedField}">
                                                     <c:set var="fieldSelected" value="selected"/>
                                                 </c:if>
                                             </c:forEach>

                                             <option title="${field.messageKey}" value="${field.sortColumn}" ${fieldSelected}>
                                                 <bean:message key="${field.messageKey}"/>
                                             </option>
                                         </c:forEach>
                   	                 </select>
                	            </p>
            	            </li>
        	                <li>
    	                        <p>
	                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    	<i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Refresh"/></span>
                                	</button>
                            	</p>
                        	</li>
                    	</ul>
                	</li>
                </c:if>  
            </ul>
            <!-- Tile Header Actions END -->
        </div>
        <!-- Tile Header END -->


        <!-- Tile Content BEGIN -->
        <div class="tile-content hidden" data-initializer="mailing-overview-table">
            <% String dyn_bgcolor = null;
                boolean bgColor = true;
                String types = "0,1,2,3";
                ComMailingBaseForm aForm = (ComMailingBaseForm) session.getAttribute("mailingBaseForm");
                if (aForm != null) {
                    types = aForm.getTypes();
                }
            %>

            <!-- Filters -->
            <div class="hidden">
                <!-- dropdown for status -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-status">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <c:set var="filterStatusSelectedNew" value=""/>
                    <c:set var="filterStatusSelectedEdit" value=""/>
                    <c:set var="filterStatusSelectedReady" value=""/>
                    <c:set var="filterStatusSelectedAdmin" value=""/>
                    <c:set var="filterStatusSelectedTest" value=""/>
                    <c:set var="filterStatusSelectedScheduled" value=""/>
                    <c:set var="filterStatusSelectedSent" value=""/>
                    <c:set var="filterStatusSelectedNorecipients" value=""/>
                    <c:set var="filterStatusSelectedCanceled" value=""/>
                    <c:set var="filterStatusSelectedSending" value=""/>
                    <c:set var="filterStatusSelectedActive" value=""/>
                    <c:set var="filterStatusSelectedDisable" value=""/>

                    <c:forEach var="filterStatusName" items="${mailingBaseForm.filterStatus}">
                        <c:choose>
                            <c:when test="${filterStatusName eq 'new'}">
                                <c:set var="filterStatusSelectedNew" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'edit'}">
                                <c:set var="filterStatusSelectedEdit" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'ready'}">
                                <c:set var="filterStatusSelectedReady" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'admin'}">
                                <c:set var="filterStatusSelectedAdmin" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'test'}">
                                <c:set var="filterStatusSelectedTest" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'scheduled'}">
                                <c:set var="filterStatusSelectedScheduled" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'sent'}">
                                <c:set var="filterStatusSelectedSent" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'norecipients'}">
                                <c:set var="filterStatusSelectedNorecipients" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'canceled'}">
                                <c:set var="filterStatusSelectedCanceled" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'sending'}">
                                <c:set var="filterStatusSelectedSending" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'active'}">
                                <c:set var="filterStatusSelectedActive" value="checked"/>
                            </c:when>
                            <c:when test="${filterStatusName eq 'disable'}">
                                <c:set var="filterStatusSelectedDisable" value="checked"/>
                            </c:when>
                        </c:choose>
                    </c:forEach>

                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <input type="checkbox" name="filterStatus" value="new" data-field-filter="" ${filterStatusSelectedNew}>
                                <bean:message key="mailing.status.new"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="filterStatus" value="edit" data-field-filter="" ${filterStatusSelectedEdit}>
                                <bean:message key="mailing.status.edit"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="filterStatus" value="ready" data-field-filter="" ${filterStatusSelectedReady}>
                                <bean:message key="mailing.status.ready"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="filterStatus" value="admin" data-field-filter="" ${filterStatusSelectedAdmin}>
                                <bean:message key="mailing.status.admin"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="filterStatus" value="test" data-field-filter="" ${filterStatusSelectedTest}>
                                <bean:message key="mailing.status.test"/>
                            </label>
                        </li>
                        <c:if test="${mailingBaseForm.mailingTypeNormal or mailingBaseForm.mailingTypeFollowup}">
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="scheduled" data-field-filter="" ${filterStatusSelectedScheduled}>
                                    <bean:message key="mailing.status.scheduled"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="sent" data-field-filter="" ${filterStatusSelectedSent}>
                                    <bean:message key="mailing.status.sent"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="norecipients" data-field-filter="" ${filterStatusSelectedNorecipients}>
                                    <bean:message key="mailing.status.norecipients"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="canceled" data-field-filter="" ${filterStatusSelectedCanceled}>
                                    <bean:message key="mailing.status.canceled"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="sending" data-field-filter="" ${filterStatusSelectedSending}>
                                    <bean:message key="mailing.status.sending"/>
                                </label>
                            </li>
                        </c:if>
                        <c:if test="${mailingBaseForm.mailingTypeEvent or mailingBaseForm.mailingTypeDate or mailingBaseForm.mailingTypeInterval}">
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="active" data-field-filter="" ${filterStatusSelectedActive}>
                                    <bean:message key="mailing.status.active"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <input type="checkbox" name="filterStatus" value="disable" data-field-filter="" ${filterStatusSelectedDisable}>
                                    <bean:message key="mailing.status.disable"/>
                                </label>
                            </li>
                        </c:if>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterStatus: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>

                    </ul>
                </div>

                <!-- dropdown for mailinglists -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-mailing-list">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <select class="form-control js-select" name="filterMailingList" multiple="multiple" data-field-filter="">
                                    <c:forEach var="availableMailingList" items="${mailingBaseForm.mailingLists}">
                                        <c:set var="filterMailingListSelected" value="false"/>
                                        <c:forEach var="filterMailingListId" items="${mailingBaseForm.filterMailingList}">
                                            <c:if test="${availableMailingList.id eq filterMailingListId}">
                                                <c:set var="filterMailingListSelected" value="true"/>
                                            </c:if>
                                        </c:forEach>

                                        <c:choose>
                                            <c:when test="${filterMailingListSelected}">
                                                <option value="${availableMailingList.id}" selected>
                                                    <c:out value="${availableMailingList.shortname}"/>
                                                </option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${availableMailingList.id}">
                                                    <c:out value="${availableMailingList.shortname}"/>
                                                </option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterMailingList: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>

                <!-- dropdown for send date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-send-date">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><bean:message key="From"/></label>
                                <input type="text" name="filterSendDateBegin" value="${mailingBaseForm.filterSendDateBegin}" data-filter-date-min="" class="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><bean:message key="To"/></label>
                                <input type="text" name="filterSendDateEnd" value="${mailingBaseForm.filterSendDateEnd}" data-filter-date-max="" class="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterSendDateBegin: '', filterSendDateEnd: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>

                <!-- dropdown for creation date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-creation-date">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><bean:message key="From"/></label>
                                <input type="text" name="filterCreationDateBegin" value="${mailingBaseForm.filterCreationDateBegin}" data-filter-date-min="" class="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><bean:message key="To"/></label>
                                <input type="text" name="filterCreationDateEnd" value="${mailingBaseForm.filterCreationDateEnd}" data-filter-date-max="" class="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterCreationDateBegin: '', filterCreationDateEnd: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>

                <!-- dropdown for badge -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-badge">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <c:set var="filterEmc" value=""/>
                    <c:set var="filterCampaignManager" value=""/>

                    <c:forEach var="badgeFilters" items="${mailingBaseForm.badgeFilters}">
                        <c:choose>
                            <c:when test="${badgeFilters.contains('isgrid')}">
                                <c:set var="filterEmc" value="checked"/>
                            </c:when>
                            <c:when test="${badgeFilters.contains('isCampaignManager')}">
                                <c:set var="filterCampaignManager" value="checked"/>
                            </c:when>
                        </c:choose>
                    </c:forEach>

                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <input type="checkbox" name="badgeFilters" value="isgrid"
                                       data-field-filter="" ${filterEmc}>
                                <span class="badge badge-info">
                                    <i class="icon-emc">
                                    </i>
                                    <b><bean:message key="mailing.grid.GridMailing"/></b>
                                </span>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="badgeFilters" value="isCampaignManager"
                                       data-field-filter="" ${filterCampaignManager}>
                                <button class="badge badge-linkage-campaignmanager" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml" tabindex="-1" type="button">
                                    <i class="icon icon-linkage-campaignmanager"></i> <strong><bean:message key="campaign.manager.icon"/></strong>
                                </button>
                            </label>
                        </li>

                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="badgeFilters: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message
                                        key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>

                    </ul>
                </div>

                <div class="card-panel hidden" id="mailings-overviewPreview">
                    <div class="row">
                        <c:forEach var="mailing" items="${mailinglist.list}">
                            <c:choose>
                                <c:when test="${mailing.workstatus eq 'mailing.status.sent' or mailing.workstatus eq 'mailing.status.norecipients'}">
                                    <emm:ShowByPermission token="stats.mailing">
                                        <c:url var="mailingViewLink" value="/mailing_stat.do">
                                            <c:param name="action" value="${ACTION_MAILINGSTAT}"/>
                                            <c:param name="mailingID" value="${mailing.mailingid}"/>
                                            <c:param name="init" value="true"/>
                                        </c:url>
                                    </emm:ShowByPermission>
                                    <emm:HideByPermission token="stats.mailing">
                                        <c:url var="mailingViewLink" value="/mailingbase.do">
                                            <c:param name="action" value="${ACTION_VIEW}"/>
                                            <c:param name="mailingID" value="${mailing.mailingid}"/>
                                            <c:param name="init" value="true"/>
                                        </c:url>
                                    </emm:HideByPermission>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="mailingViewLink" value="/mailingbase.do">
                                        <c:param name="action" value="${ACTION_VIEW}"/>
                                        <c:param name="mailingID" value="${mailing.mailingid}"/>
                                        <c:param name="init" value="true"/>
                                    </c:url>
                                </c:otherwise>
                            </c:choose>

                            <div class="col-xs-6 col-sm-4 col-md-3">
                                <a href="${mailingViewLink}" class="card">
                                    <c:choose>
                                        <c:when test="${mailing.preview_component eq 0}">
                                            <c:url var="previewImageSrc" value="assets/core/images/facelift/no_preview.svg"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="previewImageSrc" value="/sc">
                                                <c:param name="compID" value="${mailing.preview_component}"/>
                                                <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                                            </c:url>
                                        </c:otherwise>
                                    </c:choose>

                                    <img src="${previewImageSrc}" class="card-image" alt="${mailing.shortname}"/>

                                    <div class="card-body" style="overflow: hidden">
                                        <div class="col-sm-12">
                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <div class="block" style="width: 100%; position: absolute;">
                                                        <c:set var="workstatus" value=""/>
                                                        <c:if test="${not empty mailing.workstatus}">
                                                            <c:set var="workstatus">
                                                                <bean:message key="${mailing.workstatus}"/>
                                                            </c:set>
                                                        </c:if>

                                                        <strong class="headline">
                                                            <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${workstatus}"></span>
                                                            <span>${fn:escapeXml(mailing.shortname)}</span>
                                                        </strong>
                                                    </div>
                                                </div>
                                                <div class="input-group-btn">
                                                    <c:url var="deleteMailingLink" value="/mailingbase.do">
                                                        <c:param name="action" value="${ACTION_CONFIRM_DELETE}"/>
                                                        <c:param name="previousAction" value="${ACTION_LIST}"/>
                                                        <c:param name="mailingID" value="${mailing.mailingid}"/>
                                                    </c:url>

                                                    <c:set var="mailingDeleteMessage" scope="page">
                                                        <bean:message key="mailing.MailingDelete"/>
                                                    </c:set>

                                                    <c:choose>
                                                        <c:when test="${mailingBaseForm.isTemplate}">
                                                            <emm:ShowByPermission token="template.delete">
                                                                <c:set var="mailingDeleteMessage" scope="page">
                                                                    <bean:message key="template.delete"/>
                                                                </c:set>

                                                                <button type="button" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${mailingDeleteMessage}" data-url="${deleteMailingLink}">
                                                                    <i class="icon icon-trash-o"></i>
                                                                </button>
                                                            </emm:ShowByPermission>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <emm:ShowByPermission token="mailing.delete">
                                                                <c:set var="mailingDeleteMessage" scope="page">
                                                                    <bean:message key="mailing.MailingDelete"/>
                                                                </c:set>

                                                                <button type="button" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${mailingDeleteMessage}" data-url="${deleteMailingLink}">
                                                                    <i class="icon icon-trash-o"></i>
                                                                </button>
                                                            </emm:ShowByPermission>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-sm-12">
                                            <div class="block">
                                                <p class="description">
                                                    <c:if test="${mailing.usedInCM}">
                                                        <span class="badge badge-linkage-campaignmanager">
                                                            <i class="icon icon-linkage-campaignmanager"></i> <strong><bean:message key="campaign.manager.icon"/></strong>
                                                        </span>
                                                    </c:if>

                                                    <c:if test="${mailing.isgrid}">
                                                        <span class="badge badge-info">
                                                            <i class="icon-emc"></i>
                                                            <b><bean:message key="mailing.grid.GridMailing"/></b>
                                                        </span>
                                                    </c:if>

                                                    <span data-tooltip="<bean:message key='birt.mailinglist'/>">
                                                        <i class="icon icon-list-ul"></i>
                                                        <span class="text">${mailing.mailinglist}</span>
                                                    </span>
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </a>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>

            <!-- Table BEGIN -->
            <div class="table-wrapper table-overflow-visible">
                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    id="mailing"
                    name="mailinglist"
                    pagesize="${mailingBaseForm.numberOfRows}"
                    sort="external"
                    requestURI="/mailingbase.do?action=${ACTION_LIST}&isTemplate=${mailingBaseForm.isTemplate}&__fromdisplaytag=true"
                    excludedParams="*"
                    partialList="true"
                    size="${mailinglist.fullListSize}">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

					<display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <c:choose>
                        <c:when test="${mailing.workstatus eq 'mailing.status.sent' or mailing.workstatus eq 'mailing.status.norecipients'}">
                            <emm:ShowByPermission token="stats.mailing">
                                <c:url var="mailingViewLink" value="/mailing_stat.do">
                                    <c:param name="action" value="${ACTION_MAILINGSTAT}"/>
                                    <c:param name="mailingID" value="${mailing.mailingid}"/>
                                    <c:param name="init" value="true"/>
                                </c:url>
                            </emm:ShowByPermission>
                            <emm:HideByPermission token="stats.mailing">
                                <c:url var="mailingViewLink" value="/mailingbase.do">
                                    <c:param name="action" value="${ACTION_VIEW}"/>
                                    <c:param name="mailingID" value="${mailing.mailingid}"/>
                                    <c:param name="init" value="true"/>
                                </c:url>
                            </emm:HideByPermission>
                        </c:when>
                        <c:otherwise>
                            <c:url var="mailingViewLink" value="/mailingbase.do">
                                <c:param name="action" value="${ACTION_VIEW}"/>
                                <c:param name="mailingID" value="${mailing.mailingid}"/>
                                <c:param name="init" value="true"/>
                            </c:url>
                        </c:otherwise>
                    </c:choose>

                    <emm:ShowByPermission token="mailing.delete">
                        <display:column class="js-checkable" sortable="false" title="<input type='checkbox' data-form-bulk='bulkID'/>">
                            <html:checkbox property="bulkID[${mailing.mailingid}]"></html:checkbox>
                        </display:column>
                    </emm:ShowByPermission>

                    <logic:equal name="mailingBaseForm" property="isTemplate" value="false">
                        <display:column titleKey="Status" sortable="true" sortProperty="work_status" headerClass="js-table-sort js-filter-status" class="align-center">
                            <c:if test="${not empty mailing.workstatus}">
                                <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${workstatus}"></span>
                            </c:if>
                        </display:column>
                        <display:column class="table-actions" headerClass="js-table-sort js-filter-badge">
                            <%--icon for GRID--%>
                            <c:set var="badge" value=""/>
                            <c:if test="${mailing.isgrid}">
                            <c:set var="badge">
                                <span class="badge badge-info">
                                    <i class="icon-emc">
                                    </i>
                                    <b><bean:message key="mailing.grid.GridMailing"/></b>
                                </span>
                            </c:set>

                            </c:if>
                            ${badge}
                            <%--icon for "CM"--%>
                            <c:if test="${mailing.usedInCM}">
                            <button class="badge badge-linkage-campaignmanager" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml" tabindex="-1" type="button">
                            <i class="icon icon-linkage-campaignmanager"></i> <strong><bean:message key="campaign.manager.icon"/></strong>
                            </button>
                            </c:if>
                        </display:column>

                        <display:column titleKey="Mailing" sortable="true" sortProperty="shortname" headerClass="js-table-sort">
                            <span class="multiline-auto">${fn:escapeXml(mailing.shortname)}</span>
                        </display:column>

                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.description}</span>
                        </display:column>

                        <display:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort js-filter-mailing-list">
                            <span class="multiline-auto">${mailing.mailinglist}</span>
                        </display:column>

                        <display:column titleKey="mailing.senddate" format="{0,date,${localeTablePattern}}"
                            property="senddate" sortable="true" headerClass="js-table-sort js-filter-send-date" />

                        <c:forEach var="selectedField" items="${mailingBaseForm.selectedFields}" varStatus="rowCounter">
                            <c:forEach var="field" items="${fieldsMap}" varStatus="rowCounter">
                                <c:if test="${selectedField == field.sortColumn}">
                                    <c:choose>
                                        <c:when test="${field == 'RECIPIENTS_COUNT'}">
                                            <display:column property="recipientsCount"
                                                            titleKey="${field.messageKey}"
                                                            sortable="true"
                                                            sortProperty="${field.sortColumn}"
                                                            headerClass="js-table-sort"/>
                                        </c:when>
                                        <c:when test="${field == 'CREATION_DATE'}">
                                            <display:column titleKey="${field.messageKey}" format="{0,date,${localeTablePattern}}"
                                                 property="creationdate" sortable="true" sortProperty="creation_date" headerClass="js-table-sort js-filter-creation-date" />
                                        </c:when>

                                        <c:when test="${field == 'TEMPLATE'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="template_name" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.templateName}</span>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${field == 'SUBJECT'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="subject" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.subject}</span>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${field == 'TARGET_GROUPS'}">
                                            <display:column titleKey="Target-Groups" headerClass="js-table-sort">
                                                <logic:iterate name="mailing" property="targetgroups" id="targetgroup">
                                                    <html:link page="/target.do?action=${ACTION_VIEW}&targetID=${targetgroup.target_id}"><span class="multiline-auto">${targetgroup.target_name}</span></html:link>
                                                    <br/>
                                                </logic:iterate>
                                            </display:column>
                                        </c:when>
                                        <c:when test="${field == 'MAILING_ID'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="mailing_id" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.mailingid}</span>
                                            </display:column>
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </c:forEach>

                        <%-- Actions --%>
                        <display:column class="table-actions">
                            <script type="text/x-mustache-template" class="js-row-popover">
                                <c:choose>
                                    <c:when test="${mailing.preview_component eq 0}">
                                        <c:url var="previewImageSrc" value="assets/core/images/facelift/no_preview.svg"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="previewImageSrc" value="/sc">
                                            <c:param name="compID" value="${mailing.preview_component}"/>
                                            <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                                        </c:url>
                                    </c:otherwise>
                                </c:choose>
                                <img src="${previewImageSrc}" style="max-width: 200px" alt="${mailing.shortname}" border="0">
                            </script>

                            <c:if test="${mailing.hasActions}">
                                <c:set var="usedActionsMessage" scope="page">
                                    <bean:message key="action.action_link"/>
                                </c:set>

                                <agn:agnLink class="badge badge-linkage-action"
                                    data-tooltip="${usedActionsMessage}"
                                    page="/mailingbase.do?action=${ACTION_USED_ACTIONS}&mailingID=${mailing.mailingid}&previousAction=${ACTION_LIST}">
                                    <i class="icon icon-linkage-action"></i> <strong>A</strong>
                                </agn:agnLink>
                            </c:if>

                            <emm:ShowByPermission token="mailing.delete">
                                <c:set var="mailingDeleteMessage" scope="page">
                                    <bean:message key="mailing.MailingDelete"/>
                                </c:set>
                                <agn:agnLink class="btn btn-regular btn-alert js-row-delete" data-tooltip="${fn:trim(mailingDeleteMessage)}"
                                             page="/mailingbase.do?action=${ACTION_CONFIRM_DELETE}&previousAction=${ACTION_LIST}&mailingID=${mailing.mailingid}">
                                    <i class="icon icon-trash-o"></i>
                                </agn:agnLink>
                            </emm:ShowByPermission>

                            <a href="${mailingViewLink}" class="hidden js-row-show"></a>
                        </display:column>
                    </logic:equal>
                    <logic:equal name="mailingBaseForm" property="isTemplate" value="true">
                        <display:column titleKey="Template" sortable="true" sortProperty="shortname" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.shortname}</span>
                        </display:column>
                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.description}</span>
                        </display:column>
                        <display:column titleKey="Mailinglist" property="mailinglist"
                            sortable="true" sortProperty="mailinglist" headerClass="js-table-sort" />
                        <display:column titleKey="default.creationDate" sortable="true"
                            format="{0,date,yyyy-MM-dd}" property="creationdate"
                            sortProperty="creation_date" headerClass="js-table-sort">
                            ${mailing.creationdate}
                        </display:column>
                        <display:column titleKey="default.changeDate" sortable="true"
                            format="{0,date,yyyy-MM-dd}" property="changedate"
                            sortProperty="change_date" headerClass="js-table-sort">
                            ${mailing.changedate}
                        </display:column>
                        <display:column class="table-actions">
                            <script type="text/x-mustache-template" class="js-row-popover">
                                <c:choose>
                                    <c:when test="${mailing.preview_component eq 0}">
                                        <c:url var="previewImageSrc" value="assets/core/images/facelift/no_preview.svg"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="previewImageSrc" value="/sc">
                                            <c:param name="compID" value="${mailing.preview_component}"/>
                                            <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                                        </c:url>
                                    </c:otherwise>
                                </c:choose>
                                <img src="${previewImageSrc}" style="max-width: 200px" alt="${mailing.shortname}" border="0">
                            </script>

                            <emm:ShowByPermission token="template.delete">
                                <c:set var="templateDeleteMessage" scope="page">
                                    <bean:message key="template.delete" />
                                </c:set>
                                <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete" data-tooltip="${templateDeleteMessage}"
                                    page="/mailingbase.do?action=${ACTION_CONFIRM_DELETE}&previousAction=${ACTION_LIST}&mailingID=${mailing.mailingid}">
                                    <i class="icon icon-trash-o"></i>
                                </agn:agnLink>
                            </emm:ShowByPermission>

                            <a href="${mailingViewLink}" class="hidden js-row-show"></a>
                        </display:column>
                    </logic:equal>

                </display:table>
            </div>
            <!-- Table END -->

            <logic:equal name="mailingBaseForm" property="isTemplate" value="false">
            <script language="javascript" type="text/javascript">
                AGN.Initializers.ShowMailingListFilters = function($scope) {
                    if (!$scope) {
                        $scope = $(document);
                    }

                    var filtersDescription = "";

                    <c:if test="${mailingBaseForm.mailingTypeNormal}">
                    	filtersDescription += "<bean:message key="Normal"/>, ";
                    </c:if>
                    <c:if test="${mailingBaseForm.mailingTypeEvent}">
                    	filtersDescription += "<bean:message key="mailing.event"/>, ";
                    </c:if>
                    <c:if test="${mailingBaseForm.mailingTypeDate}">
                    	filtersDescription += "<bean:message key="mailing.date"/>, ";
                    </c:if>
                    <%@include file="mailing-list-follow-js.jspf" %>
                    <%@include file="mailing-list-interval-js.jspf" %>
					
                    if (filtersDescription.length > 0) {
                        filtersDescription = "<div class='well'><strong><bean:message key="mailing.showing"/></strong> " + filtersDescription;
                        filtersDescription = filtersDescription.substr(0, filtersDescription.length - 2) + "</div>";
                    }

                    $scope.find("#filtersDescription").html(filtersDescription);

                    $scope.find('#mailing')
                      .after($scope.find('#mailings-overviewPreview'));
                }
            </script>
            </logic:equal>

        </div>
        <!-- Tile Content END -->
    </div>
    <!-- Tile END -->
</agn:agnForm>
