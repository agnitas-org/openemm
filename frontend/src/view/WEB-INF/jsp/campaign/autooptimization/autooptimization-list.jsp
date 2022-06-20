<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline">
            <bean:message key="mailing.autooptimization"/>
        </h2>
        <ul class="tile-header-actions">
            <li>
                <emm:HideByPermission token="campaign.migration">
                    <a href="<html:rewrite page="/optimize.do?method=newOptimization&campaignID=${optimizationForm.campaignID}"/>" class="btn btn-regular btn-primary">
                        <i class="icon icon-plus"></i>
                        <span class="text"><bean:message key="button.New"/></span>
                    </a>
                </emm:HideByPermission>
                <emm:ShowByPermission token="campaign.migration">
                    <a href="<html:rewrite page="/optimize.do?method=newOptimization&campaignID=${optimizationForm.campaignID}&campaignName=${optimizationForm.campaignName}"/>" class="btn btn-regular btn-primary">
                        <i class="icon icon-plus"></i>
                        <span class="text"><bean:message key="button.New"/></span>
                    </a>
                </emm:ShowByPermission>
            </li>
        </ul>
    </div>
    <div class="tile-content">

        <div class="table-wrapper">
            <display:table
                class="table table-bordered table-striped table-hover js-table"
                id="optimization"
                name="optimizations">
                <display:column headerClass="js-table-sort" titleKey="mailing.autooptimization" property="shortname" />
                <display:column headerClass="js-table-sort" titleKey="default.description" property="description" />

                <display:column headerClass="squeeze-column">
                    <emm:ShowByPermission token="campaign.change">
                        <html:link styleClass="hidden js-row-show" titleKey="mailing.autooptimization.edit"
                               page="/optimize.do?method=view&optimizationID=${optimization.id}"/>
                    </emm:ShowByPermission>
                    <emm:ShowByPermission token="campaign.delete">

                        <c:set var="autoOptDeleteMessage" scope="page">
                            <bean:message key="campaign.autoopt.delete"/>
                        </c:set>
                        <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                            data-tooltip="${autoOptDeleteMessage}"
                            page="/optimize.do?method=confirmDelete&optimizationID=${optimization.id}&companyID=${optimization.companyID}">
                            <i class="icon icon-trash-o"></i>
                        </agn:agnLink>

                    </emm:ShowByPermission>

                </display:column>
            </display:table>
        </div>

    </div>
</div>
