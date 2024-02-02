<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.mailing.autooptimization.form.OptimizationForm"--%>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline">
            <mvc:message code="mailing.autooptimization"/>
        </h2>
        <ul class="tile-header-actions">
            <li>
                <c:url var="createNewLink" value="/optimization/create.action">
                    <c:param name="campaignID" value="${form.campaignID}"/>
                    <c:param name="campaignName" value="${form.campaignName}"/>
                </c:url>

                <a href="${createNewLink}" class="btn btn-regular btn-primary">
                    <i class="icon icon-plus"></i>
                    <span class="text"><mvc:message code="button.New"/></span>
                </a>
            </li>
        </ul>
    </div>
    <div class="tile-content">
        <div class="table-wrapper">
            <display:table class="table table-bordered table-striped table-hover js-table" id="optimization" name="optimizations" requestURI="/optimization/list.action">

                <%--@elvariable id="optimization" type="com.agnitas.mailing.autooptimization.beans.ComOptimization"--%>

                <display:column headerClass="js-table-sort" titleKey="mailing.autooptimization" property="shortname" />
                <display:column headerClass="js-table-sort" titleKey="default.description" property="description" />

                <display:column headerClass="squeeze-column">
                    <emm:ShowByPermission token="campaign.change">
                        <c:url var="viewLink" value="/optimization/${optimization.id}/view.action">
                            <c:param name="campaignID" value="${form.campaignID}"/>
                            <c:param name="campaignName" value="${form.campaignName}"/>
                        </c:url>

                        <a href="${viewLink}" class="hidden js-row-show"></a>
                    </emm:ShowByPermission>
                    <emm:ShowByPermission token="campaign.delete">
                        <c:url var="deletionLink" value="/optimization/${optimization.id}/confirmDelete.action">
                            <c:param name="campaignID" value="${form.campaignID}"/>
                            <c:param name="campaignName" value="${form.campaignName}"/>
                        </c:url>
                        <mvc:message var="deletionTooltip" code="campaign.autoopt.delete"/>

                        <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deletionTooltip}">
                            <i class="icon icon-trash-o"></i>
                        </a>
                    </emm:ShowByPermission>

                </display:column>
            </display:table>
        </div>
    </div>
</div>
