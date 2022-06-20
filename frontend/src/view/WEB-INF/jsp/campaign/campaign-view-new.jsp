<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.StrutsActionBase" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.campaign.forms.CampaignForm"--%>

<c:set var="ACTION_VIEW" value="<%= StrutsActionBase.ACTION_VIEW %>"/>

<c:choose>
    <c:when test="${form.id gt 0}">
        <mvc:message var="headline" scope="page" code="campaign.Edit"/>
    </c:when>
    <c:otherwise>
        <mvc:message var="headline" scope="page" code="campaign.NewCampaign"/>
    </c:otherwise>
</c:choose>

<mvc:form servletRelativeAction="/campaign/save.action" id="campaignForm" modelAttribute="form" data-form="resource">
    <mvc:hidden path="id"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">${headline}</h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-name" class="control-label">
                        <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
                            ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" cssClass="form-control" id="archive-name" maxlength="99"
                              placeholder="${nameMsg}"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-description" class="control-label">
                        <c:set var="descriptionMsg"><mvc:message code="default.description"/></c:set>
                            ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="archive-description" cssClass="form-control" rows="5"
                                  placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<c:if test="${form.id gt 0}">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="Mailings"/></h2>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="archive_mailing"
                        name="mailinglist"
                        pagesize="${form.numberOfRows}"
                        requestURI="/campaign/${form.id}/view.action"
                        excludedParams="*">

                    <display:column headerClass="js-table-sort" titleKey="Mailing" sortable="true"
                                    property="shortname"/>
                    <display:column headerClass="js-table-sort" titleKey="default.description" sortable="true"
                                    property="description"/>
                    <display:column headerClass="js-table-sort" titleKey="Mailinglist" sortable="true"
                                    property="mailinglist.shortname"/>


                    <display:column headerClass="js-table-sort" titleKey="mailing.senddate" sortable="true"
                                    format="{0,date,${localeTablePattern}}" property="senddate"/>

                    <display:column class="table-actions">
                        <c:url var="viewMailingLink" value="/mailingbase.do?action=${ACTION_VIEW}&mailingID=${archive_mailing.id}&isTemplate=false"/>
                        <a href="${viewMailingLink}" class="hidden js-row-show"></a>

                        <emm:ShowByPermission token="mailing.delete">
                            <mvc:message var="mailingDeleteMessage" scope="page" code="mailing.MailingDelete"/>
                            <c:url var="deletionLink" value="/campaign/mailing/${archive_mailing.id}/${form.id}/confirmDelete.action"/>

                            <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete"
                               data-tooltip="${mailingDeleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</c:if>
