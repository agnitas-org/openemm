<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComTargetAction" %>
<%@ page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>

<c:set var="ACTION_LIST" value="<%= ComTargetAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= ComTargetAction.ACTION_VIEW %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ComTargetAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComTargetAction.ACTION_BULK_CONFIRM_DELETE %>"/>

<c:set var="COMPLEXITY_RED" value="<%= TargetComplexityGrade.RED %>" scope="page"/>
<c:set var="COMPLEXITY_YELLOW" value="<%= TargetComplexityGrade.YELLOW %>" scope="page"/>
<c:set var="COMPLEXITY_GREEN" value="<%= TargetComplexityGrade.GREEN %>" scope="page"/>

<agn:agnForm action="/target" data-form="search">
    <html:hidden property="action" value="${ACTION_LIST}"/>

    <logic:equal name="targetForm" property="searchEnabled" value="true">
        <html:hidden property="__STRUTS_CHECKBOX_searchNameChecked" value="false"/>
        <html:hidden property="__STRUTS_CHECKBOX_searchDescriptionChecked" value="false"/>
    </logic:equal>

    <logic:equal name="targetForm" property="searchEnabled" value="true">
        <div class="tile">
            <div class="tile-header">
                <a class="headline" href="#" data-toggle-tile="#tile-targetSearch">
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

            <%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>
            <div class="tile-content tile-content-forms form-vertical" id="tile-targetSearch">
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
                                            <bean:message key="target.searchName"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item checkbox">
                                        <label>
                                            <html:checkbox value="true" property="searchDescriptionChecked"/>
                                            <bean:message key="target.searchDescription"/>
                                        </label>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </logic:equal>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.Overview"/>
            </h2>
            <ul class="tile-header-nav">
            </ul>

            <ul class="tile-header-actions">

                <emm:ShowByPermission token="targets.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><bean:message key="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <a href="#" data-form-confirm="${ACTION_BULK_CONFIRM_DELETE}">
                                    <bean:message key="bulkAction.delete.target"/>
                                </a>
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

                        <li class="dropdown-header"><bean:message key="Targets"/></li>
                        <li>
                            <label class="label">
                                <html:checkbox property="showWorldDelivery" styleClass="js-form-change" />
                                <bean:message key="target.worldDelivery"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <html:checkbox property="showTestAndAdminDelivery" styleClass="js-form-change" />
                                <bean:message key="target.adminAndTestDelivery"/>
                            </label>
                        </li>
                        <li class="divider"></li>

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
                            <logic:iterate collection="${targetForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content" data-form-content="">
            <%--<html:hidden property="numberOfRows"/>--%>
            <%--<html:hidden property="showWorldDelivery"/>--%>
            <%--<html:hidden property="showTestAndAdminDelivery"/>--%>

            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "target-overview": {
                        "rows-count": ${targetForm.numberOfRows},
                        "show-world-targets": ${targetForm.showWorldDelivery},
                        "show-test-and-admin-targets": ${targetForm.showTestAndAdminDelivery}
                    }
                }
            </script>

            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="targettbl"
                        name="targetlist"
                        pagesize="${targetForm.numberOfRows}"
                        sort="list"
                        requestURI="/target.do?action=${targetForm.action}&__fromdisplaytag=true"
                        excludedParams="*">
                    <emm:ShowByPermission token="targets.delete">
                        <display:column class="js-checkable" sortable="false" headerClass="squeeze-column"
                                        title="<input type='checkbox' data-form-bulk='bulkID'/>">
                            <html:checkbox property="bulkID[${targettbl.id}]" ></html:checkbox>
                        </display:column>
                    </emm:ShowByPermission>

                    <display:column headerClass="js-table-sort squeeze-column" property="id" titleKey="MailinglistID"/>

                    <display:column headerClass="js-table-sort" titleKey="target.Target" sortable="true" sortProperty="targetName">
                        <c:choose>
                            <%-- Show icon for the target which is unavailable in dropdowns--%>
                            <c:when test="${targettbl.componentHide}">
                            	<emm:HideByPermission token="mailing.content.showExcludedTargetgroups">
                                	<i class="icon icon-exclamation-triangle text-state-alert" data-tooltip="<bean:message key="target.tooltip.not_available_in_components"/>"></i>
                                </emm:HideByPermission>
                                <emm:ShowByPermission token="mailing.content.showExcludedTargetgroups">
                                	<i class="icon icon-exclamation-triangle hidden"></i>
                                </emm:ShowByPermission>
                            </c:when>
                            <%-- Add hidden icon to keep the intendation--%>
                            <c:otherwise>
                                <i class="icon icon-exclamation-triangle hidden"></i>
                            </c:otherwise>
                        </c:choose>
                        <span class="multiline-xs-200 multiline-sm-250 multiline-md-max" style="display: inline">${targettbl.targetName}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.description" sortable="true" sortProperty="targetDescription">
                        <span class="multiline-xs-200 multiline-sm-250 multiline-md-max">${targettbl.targetDescription}</span>
                    </display:column>

                        <display:column class="align-center bold" titleKey="target.group.complexity" sortable="false" headerClass="squeeze-column">
                            <c:set var="complexityGrade" value="${targetForm.targetComplexities[targettbl.id]}"/>

                            <c:choose>
                                <c:when test="${complexityGrade eq COMPLEXITY_GREEN}">
                                    <div class="form-badge complexity-green">
                                        <bean:message key="target.group.complexity.low"/>
                                    </div>
                                </c:when>
                                <c:when test="${complexityGrade eq COMPLEXITY_YELLOW}">
                                    <div class="form-badge complexity-yellow" data-tooltip="<bean:message key="warning.target.group.performance.yellow"/>">
                                        <bean:message key="target.group.complexity.medium"/>
                                    </div>
                                </c:when>
                                <c:when test="${complexityGrade eq COMPLEXITY_RED}">
                                    <div class="form-badge complexity-red" data-tooltip="<bean:message key="warning.target.group.performance.red"/>">
                                        <bean:message key="target.group.complexity.high"/>
                                    </div>
                                </c:when>
                            </c:choose>
                        </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.creationDate" sortable="true"
                                    format="{0, date, ${adminDateFormat}}" property="creationDate"/>

                    <display:column headerClass="js-table-sort" titleKey="default.changeDate" sortable="true"
                                    format="{0, date, ${adminDateFormat}}" property="changeDate"/>

                    <display:column class="table-actions">

                        <html:link titleKey="target.Edit" styleClass="hidden js-row-show"
	                                   page="/targetQB.do?method=show&targetID=${targettbl.id}"/>

                        <emm:ShowByPermission token="targets.delete">
                            <c:set var="targetDeleteMessage" scope="page">
                                <bean:message key="target.Delete"/>
                            </c:set>
                            <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                         data-tooltip="${targetDeleteMessage}"
                                         page="/target.do?action=${ACTION_CONFIRM_DELETE}&targetID=${targettbl.id}&previousAction=${ACTION_LIST}">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>
                        </emm:ShowByPermission>

                    </display:column>
                </display:table>
            </div>

            <emm:instantiate var="appliedFilters" type="java.util.LinkedHashMap">
                <c:if test="${targetForm.showWorldDelivery}">
                    <c:set target="${appliedFilters}" property="${appliedFilters.size()}"><bean:message key="target.worldDelivery"/></c:set>
                </c:if>

                <c:if test="${targetForm.showTestAndAdminDelivery}">
                    <c:set target="${appliedFilters}" property="${appliedFilters.size()}"><bean:message key="target.adminAndTestDelivery"/></c:set>
                </c:if>
            </emm:instantiate>

            <script data-initializer="targetgroup-overview-filters" type="application/json">
                {
                    "filters": ${emm:toJson(appliedFilters.values())}
                }
            </script>

            <script id="targetgroup-overview-filters" type="text/x-mustache-template">
                <div class='well'>
                    <strong><bean:message key="mailing.showing"/></strong>
                    {{- filters.join(', ') }}
                </div>
            </script>

        </div>
    </div>
</agn:agnForm>
