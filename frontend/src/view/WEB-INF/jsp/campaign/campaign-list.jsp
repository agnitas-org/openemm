<%@ page import="com.agnitas.web.ComCampaignAction"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="campaignForm" type="com.agnitas.web.ComCampaignForm"--%>

<c:set var="ACTION_LIST" value="<%=ComCampaignAction.ACTION_LIST%>" scope="page" />
<c:set var="ACTION_VIEW" value="<%=ComCampaignAction.ACTION_VIEW%>" scope="page" />
<c:set var="ACTION_CONFIRM_DELETE" value="<%=ComCampaignAction.ACTION_CONFIRM_DELETE%>" scope="page" />

<agn:agnForm action="/campaign" data-form="resource">
    <!-- Tile BEGIN -->
    <div class="tile">

        <!-- Tile Header BEGIN -->
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.Overview"/>
            </h2>
            <ul class="tile-header-nav">
            </ul>

            <!-- Tile Header Actions BEGIN -->
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
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
                            <logic:iterate collection="${campaignForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
            </ul>
            <!-- Tile Header Actions END -->
        </div>
        <!-- Tile Header END -->

        <!-- Tile Content BEGIN -->
        <div class="tile-content">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "archive-overview": {
                        "rows-count": ${campaignForm.numberOfRows}
                    }
                }
            </script>

            <!-- Table BEGIN -->
            <div class="table-wrapper">
                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    id="campaign"
                    name="campaignlist"
                    pagesize="${campaignForm.numberOfRows}"
                    sort="external"
                    requestURI="/campaign.do?action=${ACTION_LIST}&__fromdisplaytag=true"
                    excludedParams="*">

                    <display:column titleKey="mailing.archive" sortable="true" sortProperty="shortname"
                                    headerClass="js-table-sort" property="shortname">
                        <span class="multiline-auto">${campaign.shortname}</span>
                    </display:column>

                    <display:column titleKey="Description" sortable="true" sortProperty="description"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto">${campaign.description}</span>
                    </display:column>

                    <display:column class="table-actions">
                        <emm:ShowByPermission token="campaign.change">
                            <html:link titleKey="campaign.Edit" styleClass="hidden js-row-show"
                                       page="/campaign.do?action=${ACTION_VIEW}&campaignID=${campaign.id}">
                            </html:link>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="campaign.delete">

                            <c:set var="campaignDeleteMessage" scope="page">
                                <bean:message key="campaign.Delete"/>
                            </c:set>
                            <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                data-tooltip="${campaignDeleteMessage}"
                                page="/campaign.do?action=${ACTION_CONFIRM_DELETE}&previousAction=${ACTION_LIST}&campaignID=${campaign.id}">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>

                        </emm:ShowByPermission>
                    </display:column>
                </display:table>

            </div>
            <!-- Table END -->

        </div>
        <!-- Tile Content END -->
    </div>
    <!-- Tile END -->

</agn:agnForm>
