<%@ page import="org.agnitas.util.AgnUtils" errorPage="/errorRedesigned.action" %>
<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="campaignList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="form" type="org.agnitas.web.forms.PaginationForm"--%>

<mvc:message var="deletionTooltip" code="campaign.Delete"/>

<div class="tiles-container">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/archive/list.action" cssClass="tile" modelAttribute="form">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "archive-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table" id="campaign" name="campaignList"
                                   size="${form.numberOfRows}" sort="external" requestURI="/mailing/archive/list.action"
                                   partialList="true" excludedParams="*">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <%--@elvariable id="campaign" type="com.agnitas.beans.Campaign"--%>

                        <display:column titleKey="mailing.archive" sortable="true" sortProperty="shortname"
                                        headerClass="js-table-sort" property="shortname"/>

                        <display:column titleKey="Description" sortable="true" sortProperty="description"
                                        headerClass="js-table-sort" property="description"/>

                        <display:column headerClass="fit-content">
                            <a href='<c:url value="/mailing/archive/${campaign.id}/view.action"/>' class="hidden" data-view-row></a>

                            <emm:ShowByPermission token="campaign.delete">
                                <a href='<c:url value="/mailing/archive/${campaign.id}/delete.action"/>' class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deletionTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:ShowByPermission>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
