<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingParameterForm" type="com.agnitas.emm.core.mailing.forms.ComMailingParameterForm"--%>

<c:url var="searchLink" value="/mailingParameter.do">
    <c:param name="action" value="search"/>
</c:url>

<c:url var="viewLink" value="/mailingParameter.do">
    <c:param name="action" value="view"/>
</c:url>


<agn:agnForm action="/mailingParameter.do" data-form="search"
             data-initializer="mailing-parameter-list-search-initializer"
             data-config="searchLink:${searchLink},
             viewLink:${viewLink}&mailingInfoID={mailing-info-id},
             mailingSearchQuery:${mailingParameterForm.mailingSearchQuery},
             parameterSearchQuery:${mailingParameterForm.parameterSearchQuery}">
    <html:hidden property="action"/>
    <html:hidden property="numberOfRowsChanged"/>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-mailingSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Search"/>
            </a>
        </div>

        <div class="tile-content tile-content-forms form-vertical" id="tile-mailingSearch">
            <div class="row">
                <div class="col-md-8">
                    <div class="form-group">
                        <label class="control-label">
                            <label for="parameter_search_field"><bean:message key="mailing.searchFor"/></label>
                        </label>
                        <input class="form-control" id="parameter_search_field" name="parameterSearchQuery" value="${mailingParameterForm.parameterSearchQuery}"/>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="form-group">
                        <label class="control-label">
                            <label for="mailing_search_field"><bean:message key="MailingId"/></label>
                        </label>
                        <input class="form-control" id="mailing_search_field" name="mailingSearchQuery" value="${mailingParameterForm.mailingSearchQuery}"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <div class="form-group btn-group pull-right">
                        <button type="button" class="btn btn-regular"
                                data-form-set="parameterSearchQuery:'', mailingSearchQuery:''"
                                data-form-submit-static>
                            <bean:message key="button.search.reset"/></button>
                        <button type="button" class="btn btn-primary btn-regular"
                                data-form-submit
                                data-form-persist>
                            <i class="icon icon-search"></i>
                            <span class="text"><bean:message key="Search"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Overview"/></h2>
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
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content" data-form-content>
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "mailing-parameter-overview": {
                        "rows-count": ${mailingParameterForm.numberOfRows}
                    }
                }
            </script>

            <div class="table-wrapper">
                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    pagesize="${mailingParameterForm.numberOfRows}"
                    id="parameter"
                    name="mailingParameter"
                    requestURI="/mailingParameter.do?action=list&__fromdisplaytag=true&numberOfRows=${mailingParameterForm.numberOfRows}&parameterSearchQuery=${mailingParameterForm.parameterSearchQuery}&mailingSearchQuery=${mailingParameterForm.mailingSearchQuery}"
                    excludedParams="*">

                    <display:column titleKey="MailingParameter" headerClass="parameter_name_head header" class="mailing" sortProperty="name"
                        sortable="true">
                        ${parameter.name}
                    </display:column>

                    <display:column headerClass="parameter_description_head header" class="mailing" property="description" sortProperty="description"
                        titleKey="Description" sortable="true">
                        ${parameter.description}
                    </display:column>

                    <display:column headerClass="parameter_for_mailing_head header" class="mailing" property="mailingID" sortProperty="mailingID"
                        titleKey="mailing.MailingParameter.forMailing" sortable="true">
                        ${parameter.mailingID}
                    </display:column>

                    <display:column headerClass="parameter_change_date_head header" class="senddate" titleKey="default.changeDate" sortable="true"
                        format="{0, date, ${adminDateFormat}}" property="changeDate" sortProperty="changeDate">
                        ${parameter.changeDate}
                    </display:column>

                    <display:column class="edit table-actions">
                        <emm:ShowByPermission token="mailing.parameter.change">

                            <c:set var="mailingParameterDeleteMessage" scope="page">
                                <bean:message key="button.Delete"/>
                            </c:set>
                            <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                data-tooltip="${mailingParameterDeleteMessage}"
                                page="/mailingParameter.do?action=confirmDelete&mailingInfoID=${parameter.mailingInfoID}&fromListPage=true">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>

                        </emm:ShowByPermission>
                        <html:link styleClass="hidden js-row-show"
                                   page="/mailingParameter.do?action=view&mailingInfoID=${parameter.mailingInfoID}">
                        </html:link>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
