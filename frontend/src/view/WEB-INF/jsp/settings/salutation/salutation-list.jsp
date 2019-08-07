<%@ page import="org.agnitas.web.SalutationAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="salutationForm" type="org.agnitas.web.SalutationForm"--%>

<c:set var="ACTION_LIST" 			value="<%= SalutationAction.ACTION_LIST %>"/>
<c:set var="ACTION_CONFIRM_DELETE" 	value="<%= SalutationAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_VIEW" 			value="<%= SalutationAction.ACTION_VIEW %>"/>

<agn:agnForm action="/salutation">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "salutation-overview": {
                "rows-count": ${salutationForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
			<h2 class="headline">
                <bean:message key="default.Overview"/>
            </h2>
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
                            <logic:iterate collection="${salutationForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
            </ul>

        </div>
        <div class="tile-content">
            <div class="table-wrapper">

                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    pagesize="${salutationForm.numberOfRows}"
                    id="salutation"
                    name="salutationEntries"
                    sort="external"
                    requestURI="/salutation.do?action=${ACTION_LIST}&__fromdisplaytag=true"
                    excludedParams="*"
                    size="${salutationEntries.fullListSize}">

                    <display:column property="titleId" titleKey="MailinglistID" headerClass="js-table-sort"
                                    sortProperty="title_id" sortable="true" />
                    <display:column class="description" headerClass="js-table-sort" property="description" sortProperty="description" titleKey="settings.FormOfAddress" sortable="true" />

                    <display:column class="table-actions">
                        <html:link styleClass="hidden js-row-show" titleKey="salutation.SalutationEdit"
                                   page="/salutation.do?action=${ACTION_VIEW}&salutationID=${salutation.titleId}"> </html:link>
						<c:if test="${salutation.companyID ne 0}">
							<emm:ShowByPermission token="salutation.delete">
                        		<c:set var="salutationDeleteMessage" scope="page">
                            		<bean:message key="salutation.SalutationDelete"/>
                        		</c:set>
                        		<agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                            		data-tooltip="${salutationDeleteMessage}"
                            		page="/salutation.do?action=${ACTION_CONFIRM_DELETE}&salutationID=${salutation.titleId}&shortname=${salutation.description}&previousAction=${salutationForm.previousAction}">
                            		<i class="icon icon-trash-o"></i>
                        		</agn:agnLink>
							</emm:ShowByPermission>
						</c:if>
                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
