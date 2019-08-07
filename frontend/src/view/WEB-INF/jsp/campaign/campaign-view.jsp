<%--checked --%>
<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.CampaignAction"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_NEW" value="<%= CampaignAction.ACTION_NEW %>"/>
<c:set var="ACTION_VIEW" value="<%= CampaignAction.ACTION_VIEW %>"/>
<c:set var="ACTION_LIST" value="<%= CampaignAction.ACTION_LIST %>"/>
<c:set var="ACTION_NEW_MALING" value="<%= CampaignAction.ACTION_NEW_MAILING %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= CampaignAction.ACTION_CONFIRM_DELETE %>"/>

<c:choose>
    <c:when test="${campaignForm.campaignID != 0}">
        <c:set var="headline" scope="page">
            <bean:message key="campaign.Edit"/>
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="headline" scope="page">
            <bean:message key="campaign.NewCampaign"/>
        </c:set>
    </c:otherwise>
</c:choose>
<agn:agnForm action="/campaign.do" data-form="resource" id="campaignForm">
    <html:hidden property="action"/>
    <html:hidden property="campaignID"/>


    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                ${headline}
            </h2>
        </div>
        <div class="tile-content tile-content-forms">

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-name" class="control-label">
                        <bean:message key="default.Name"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" styleId="archive-name" property="shortname" maxlength="99" />
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-description" class="control-label">
                        <bean:message key="default.description"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:textarea styleClass="form-control" styleId="archive-description" property="description" rows="5" />
                </div>
            </div>


        <input type="hidden" id="save" name="save" value=""/>

        </div>

    </div>


    <c:if test="${campaignForm.campaignID != 0}">

        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><bean:message key="Mailings"/></h2>

                <emm:ShowByPermission token="mailing.change">
                    <ul class="tile-header-actions">
                        <li>
                            <%--if there is a problem with mailingbase.do link, perhaps you will need to use this link
                                <html:link page="/campaign.do?action=${ACTION_NEW_MALING}&mailingID=0&campaignID=${campaignForm.campaignID}"><span><bean:message key="mailing.New_Mailing"/></span></html:link>
                            --%>
                            <html:link styleClass="btn btn-regular btn-secondary" page="/mailingbase.do?action=${ACTION_NEW}&mailingID=0&campaignID=${campaignForm.campaignID}&isTemplate=false"><i class="icon icon-plus"></i><span class="text"><bean:message key="mailing.New_Mailing"/></span></html:link>

                        </li>
                    </ul>
                </emm:ShowByPermission>
            </div>
            <div class="tile-content">
                <logic:iterate collection="${campaignForm.columnwidthsList}" indexId="i" id="width">
                    <html:hidden property="columnwidthsList[${i}]"/>
                </logic:iterate>

                <div class="table-wrapper">

                    <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="archive_mailing"
                        name="mailinglist"
                        pagesize="${campaignForm.numberOfRows}"
                        requestURI="/campaign.do?action=${ACTION_VIEW}&campaignID=${campaignForm.campaignID}&__fromdisplaytag=true&numberOfRows=${campaignForm.numberOfRows}"
                        excludedParams="*">

                        <display:column headerClass="js-table-sort" titleKey="Mailing" sortable="true" property="shortname" />
                        <display:column headerClass="js-table-sort" titleKey="default.description" sortable="true" property="description" />
                        <display:column headerClass="js-table-sort" titleKey="Mailinglist" sortable="true" property="mailinglist.shortname" />


                        <display:column  headerClass="js-table-sort" titleKey="mailing.senddate" sortable="true" format="{0,date,${localeTablePattern}}" property="senddate"/>
                        <display:column class="table-actions">
                            <html:link styleClass="hidden js-row-show" titleKey="mailing.MailingEdit"
                                       page="/mailingbase.do?action=${ACTION_VIEW}&mailingID=${archive_mailing.id}&isTemplate=false">
                            </html:link>
                            <emm:ShowByPermission token="mailing.delete">

                                <c:set var="mailingDeleteMessage" scope="page">
                                    <bean:message key="mailing.MailingDelete"/>
                                </c:set>
                                <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                    data-tooltip="${mailingDeleteMessage}"
                                    page="/campaign.do?action=${ACTION_CONFIRM_DELETE}&campaignID=${campaignForm.campaignID}&previousAction=${ACTION_VIEW}&mailingID=${archive_mailing.id}">
                                    <i class="icon icon-trash-o"></i>
                                </agn:agnLink>

                            </emm:ShowByPermission>
                        </display:column>
                    </display:table>

                </div>
            </div>
        </div>

    </c:if>
</agn:agnForm>
