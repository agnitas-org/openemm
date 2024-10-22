<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.archive.forms.MailingArchiveForm"--%>

<c:set var="isWorkflowDriven" value="${not empty workflowId and workflowId > 0}" />

<c:choose>
    <c:when test="${form.id gt 0}">
        <mvc:message var="headline" code="campaign.Edit"/>
    </c:when>
    <c:otherwise>
        <mvc:message var="headline" code="campaign.NewCampaign"/>
    </c:otherwise>
</c:choose>

<mvc:form servletRelativeAction="/mailing/archive/save.action" id="archiveForm" modelAttribute="form" data-form="${isWorkflowDriven ? 'static' : 'resource'}">
    <mvc:hidden path="id"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">${headline}</h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-name" class="control-label">
                        <mvc:message var="nameMsg" code="default.Name"/>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" cssClass="form-control" id="archive-name" maxlength="99" placeholder="${nameMsg}"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="archive-description" class="control-label">
                        <mvc:message var="descriptionMsg" code="default.description"/>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="archive-description" cssClass="form-control" rows="5" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<c:if test="${form.id gt 0}">
    <mvc:form cssClass="tile" servletRelativeAction="/mailing/archive/${form.id}/view.action" method="GET" modelAttribute="form">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "archive-mailings-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h2 class="headline"><mvc:message code="Mailings"/></h2>

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
            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="archive_mailing"
                        pagesize="${mailingsList.pageSize}"
                        sort="external"
                        name="mailingsList"
                        requestURI="/mailing/archive/${form.id}/view.action"
                        partialList="true"
                        size="${mailingsList.fullListSize}"
                        excludedParams="*">

                    <display:column headerClass="js-table-sort" titleKey="Mailing" sortable="true"
                                    property="shortname" sortProperty="mailing_name"/>
                    <display:column headerClass="js-table-sort" titleKey="default.description" sortable="true"
                                    property="description" sortProperty="mailing_description"/>
                    <display:column headerClass="js-table-sort" titleKey="Mailinglist" sortable="true"
                                    property="mailinglist.shortname" sortProperty="listname"/>

                    <display:column headerClass="js-table-sort" titleKey="mailing.senddate" sortable="true"
                                    format="{0,date,${localeTablePattern}}" property="senddate" sortProperty="senddate"/>

                    <display:column class="table-actions">
                        <a href="<c:url value="/mailing/${archive_mailing.id}/settings.action"/>" class="hidden js-row-show"></a>

                        <emm:ShowByPermission token="mailing.delete">
                            <mvc:message var="mailingDeleteMessage" scope="page" code="mailing.MailingDelete"/>
                            <c:url var="deletionLink" value="/mailing/archive/${form.id}/mailing/${archive_mailing.id}/confirmDelete.action"/>

                            <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${mailingDeleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </mvc:form>
</c:if>
