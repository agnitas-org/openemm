<%@ page import="com.agnitas.util.AgnUtils" errorPage="/error.action" %>
<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="campaignList" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="form" type="com.agnitas.web.forms.PaginationForm"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>

<mvc:form servletRelativeAction="/mailing/archive/list.action" cssClass="form-vertical" id="archiveForm" modelAttribute="form" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.Overview"/>
            </h2>
            <ul class="tile-header-nav"></ul>

            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <script type="application/json" data-initializer="web-storage-persist">
            {
                "archive-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
            </script>

            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="campaign"
                        name="campaignList"
                        size="${form.numberOfRows}"
                        sort="external"
                        requestURI="/mailing/archive/list.action"
                        partialList="true"
                        excludedParams="*">

                    <%--@elvariable id="campaign" type="com.agnitas.beans.Campaign"--%>

                    <display:column titleKey="mailing.archive" sortable="true" sortProperty="shortname"
                                    headerClass="js-table-sort" property="shortname">
                        <span class="multiline-auto">${campaign.shortname}</span>
                    </display:column>

                    <display:column titleKey="Description" sortable="true" sortProperty="description"
                                    headerClass="js-table-sort" property="description">
                        <span class="multiline-auto">${campaign.description}</span>
                    </display:column>

                    <display:column class="table-actions">
                        <emm:ShowByPermission token="campaign.show">
                            <c:url var="viewLink" value="/mailing/archive/${campaign.id}/view.action"/>
                            <a href="${viewLink}" class="hidden js-row-show"></a>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="campaign.delete">
                            <c:url var="deletionLink" value="/mailing/archive/${campaign.id}/confirmDelete.action"/>
                            <mvc:message var="deletionTooltip" code="campaign.Delete"/>

                            <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deletionTooltip}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>

            </div>
        </div>
    </div>
</mvc:form>

