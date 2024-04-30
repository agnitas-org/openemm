<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="blacklists" type="java.util.List"--%>
<%--@elvariable id="blacklistDto" type="com.agnitas.emm.core.globalblacklist.beans.BlacklistDto"--%>
<%--@elvariable id="blacklistListForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistListForm"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<c:url var="saveActionUrl" value="/recipients/blacklist/save.action"/>

<mvc:form servletRelativeAction="/recipients/blacklist/list.action"
          modelAttribute="blacklistListForm"
          id="blacklistListView"
          data-controller="blacklist-list"
          data-form="resource">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "blacklist-overview": {
                "rows-count": ${blacklistListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile" data-initializer="blacklist-list-init">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="recipient.Blacklist"/>
            </h2>
        </div>
        <div class="tile-content">
            <div class="tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="new-entry-email" class="control-label">
                            <bean:message key="blacklist.add"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <input id="new-entry-email" class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="new-entry-reason" class="control-label">
                            <bean:message key="blacklist.reason"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <input id="new-entry-reason" class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-12">
                        <div class="btn-group pull-right">
                            <button type="button" class="btn btn-regular btn-primary" data-action="saveBlacklist" data-url="${saveActionUrl}">
                                <i class="icon icon-plus"></i>
                                <span class="text"><bean:message key="button.Add"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <ul class="pull-left">
                <li>
                    <agn:agnLink page="/recipients/blacklist/download.action"
                                 class="btn btn-regular btn-primary" data-prevent-load="">
                        <i class="icon icon-download"></i>
                        <span class="text"><bean:message key="BlacklistDownload"/></span>
                    </agn:agnLink>
                </li>
            </ul>

            <ul class="tile-header-actions">
                <li>
                    <div class="has-icon">
                        <mvc:message var="searchMessage" code="blacklist.search"/>
                        <mvc:text path="searchQuery" cssClass="form-control rounded" placeholder="${searchMessage}"/>
                        <i class="form-control-icon icon icon-search"></i>
                    </div>
                </li>

                <li>
                    <button class="btn btn-primary btn-regular pull-right" type="button" data-form-submit="" data-form-persist="page: 1">
                        <i class="icon icon-search"></i>
                        <span class="text"><bean:message key="Search"/></span>
                    </button>
                </li>

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
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><bean:message key="button.Show"/></span>
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
                        id="blacklistDto"
                        class="table table-bordered table-striped js-table"
                        requestURI="/recipients/blacklist/list.action"
                        list="${blacklists}"
                        pagesize="${blacklistListForm.numberOfRows gt 0 ? blacklistListForm.numberOfRows : 0}"
                        excludedParams="*">

                    <display:column property="email" titleKey="mailing.MediaType.0" sortable="true"
                                    headerClass="js-table-sort" sortProperty="email"/>

                    <display:column property="reason" titleKey="blacklist.reason" sortable="true"
                                    headerClass="js-table-sort" sortProperty="reason" escapeXml="true"/>

                    <display:column property="date" headerClass="js-table-sort" sortProperty="timestamp" titleKey="settings.fieldType.DATE" sortable="true">
                        <emm:formatDate value="${blacklistDto.date}" format="${dateTimeFormat}"/>
                    </display:column>

                    <emm:ShowByPermission token="recipient.change|recipient.delete">
                        <display:column class="table-actions">
                            <emm:ShowByPermission token="recipient.change">
                                <a href="#" class="btn btn-regular btn-secondary" data-tooltip="<bean:message key="button.Edit"/>"
                                        data-modal="modal-edit-blacklisted-recipient" data-modal-set="email: '${blacklistDto.email}', reason: '${blacklistDto.reason}'">
                                    <i class="icon icon-pencil"></i>
                                </a>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="recipient.delete">
                                <c:set var="blacklistDeleteMessage" scope="page">
                                    <bean:message key="blacklist.BlacklistDelete"/>
                                </c:set>

                                <c:url var="deleteUrl" value="/recipients/blacklist/confirmDelete.action">
                                    <c:param name="email" value="${blacklistDto.email}"/>
                                </c:url>
                                <a href="${deleteUrl}"
                                   class="btn btn-regular btn-alert js-row-delete"
                                   data-tooltip="${blacklistDeleteMessage}">
                                    <i class="icon icon-trash-o"></i>
                                </a>
                            </emm:ShowByPermission>
                        </display:column>
                    </emm:ShowByPermission>
                </display:table>
            </div>
        </div>
    </div>
    <emm:ShowByPermission token="recipient.change">
        <%@ include file="fragments/modal-edit-blacklisted-recipient.jspf" %>
    </emm:ShowByPermission>
</mvc:form>
