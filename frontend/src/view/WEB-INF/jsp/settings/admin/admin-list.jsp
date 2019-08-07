<%@ page import="com.agnitas.web.ComAdminAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="adminForm" type="com.agnitas.web.ComAdminForm"--%>

<c:set var="ACTION_LIST" value="<%= ComAdminAction.ACTION_LIST%>"/>

<html:form action="/admin.do" styleClass="form-vertical">
    <html:hidden property="action" value="${ACTION_LIST}"/>
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="export_action" styleId="export_action" value=""/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "admin-overview": {
                "rows-count": ${adminForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-basicSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Search"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-search"></i>
                        <bean:message key="Search"/>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-basicSearch" class="tile-content tile-content-forms">
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchFirstName" class="control-label"><bean:message key="Firstname"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:text styleClass="form-control" property="searchFirstName" styleId="searchFirstName"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchLastName" class="control-label"><bean:message key="Lastname"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:text styleClass="form-control" property="searchLastName" styleId="searchLastName"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchEmail" class="control-label"><bean:message key="mailing.MediaType.0"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:text styleClass="form-control" property="searchEmail" styleId="searchEmail"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="filterCompanyId" class="control-label"><bean:message key="settings.Company"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:select styleId="filterCompanyId" property="filterCompanyId" size="1" styleClass="form-control js-select">
                                <html:option value="" key="default.All"/>
                                <c:forEach var="company" items="${adminForm.companies}">
                                    <html:option value="${company.id}">
                                        ${company.shortname}
                                    </html:option>
                                </c:forEach>
                            </html:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="filterMailinglistId" class="control-label"><bean:message key="Mailinglist"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:select styleId="filterMailinglistId" property="filterMailinglistId" size="1" styleClass="form-control js-select">
                                <html:option value="" key="default.All"/>
                                <c:forEach var="mailinglist" items="${adminForm.mailinglists}">
                                    <html:option value="${mailinglist.id}">
                                        ${mailinglist.shortname}
                                    </html:option>
                                </c:forEach>
                            </html:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label"><bean:message key="settings.Usergroup"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:select property="filterAdminGroupId" styleClass="form-control js-select">
                                <html:option value="" key="default.All"/>
                                <c:forEach var="adminGroup" items="${adminForm.adminGroups}">
                                    <html:option value="${adminGroup.groupID}">
                                        ${adminGroup.shortname}
                                    </html:option>
                                </c:forEach>
                            </html:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="language" class="control-label"><bean:message key="Language"/></label>
                        </div>
                        <div class="col-md-12">
                            <html:select property="filterLanguage" size="1" styleId="language" styleClass="form-control">
                                <html:option value="" key="default.All"/>
                                <html:option value="de"><bean:message key="settings.German"/></html:option>
                                <html:option value="en"><bean:message key="settings.English"/></html:option>
                                <html:option value="fr"><bean:message key="settings.French"/></html:option>
                                <html:option value="es"><bean:message key="settings.Spanish"/></html:option>
                                <html:option value="pt"><bean:message key="settings.Portuguese"/></html:option>
                                <html:option value="nl"><bean:message key="settings.Dutch"/></html:option>
                                <html:option value="it"><bean:message key="settings.Italian"/></html:option>
                            </html:select>
                        </div>
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
                        <span class="text"><bean:message key="default.View"/></span>
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
                            <logic:iterate collection="${adminForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><bean:message key="export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="statistics.exportFormats"/></li>
                        <li>
                            <a href="#" tabindex="-1" data-form-set="export_action:export_csv" data-form-submit-static>
                                <i class="icon icon-file-excel-o"></i>
                                <bean:message key="user.export.csv"/>
                            </a>
                            <a href="#" tabindex="-1" data-form-set="export_action:export_pdf" data-form-submit-static>
                                <i class="icon icon-file-pdf-o"></i>
                                <bean:message key="user.export.pdf"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content" ><%--data-form-content--%>
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               pagesize="${adminForm.numberOfRows}"
                               id="admin"
                               name="adminEntries"
                               requestURI="/admin.do?action=${ACTION_LIST}&numberOfRows=${adminForm.numberOfRows}&__fromdisplaytag=true"
                               excludedParams="*"
                               size="${adminEntries.fullListSize}"
                               partialList="true">
                    <display:column titleKey="logon.username" sortProperty="username" sortable="true" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.username} </html:link>
                    </display:column>
                    <display:column sortProperty="firstname" titleKey="recipient.Firstname" sortable="true" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.firstname} </html:link>
                    </display:column>
                    <display:column titleKey="recipient.Lastname" sortable="true" sortProperty="fullname" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.fullname} </html:link>
                    </display:column>
                    <display:column titleKey="mailing.MediaType.0" sortable="true" property="email"  sortProperty="email" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.email} </html:link>
                    </display:column>
                    <display:column titleKey="default.creationDate" sortable="true" format="{0,date,yyyy-MM-dd}" property="creationDate" sortProperty="creation_date" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.creationDate} </html:link>
                    </display:column>
                    <display:column titleKey="default.changeDate" sortable="true" format="{0,date,yyyy-MM-dd}" property="changeDate" sortProperty="timestamp" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.changeDate} </html:link>
                    </display:column>
                    <display:column titleKey="admin.login.last" sortable="true" format="{0,date,yyyy-MM-dd}" property="loginDate" sortProperty="last_login" headerClass="js-table-sort">
                        <html:link page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}">${admin.loginDate} </html:link>
                    </display:column>
                    <display:column class="table-actions">
                        <html:link styleClass="js-row-show hidden" titleKey="settings.admin.edit" page="/admin.do?action=${ACTION_VIEW}&adminID=${admin.id}"></html:link>
						<emm:ShowByPermission token="admin.delete">                       
                        	<c:choose>
                        		<c:when test="${admin.passwordExpired}">
                        			<c:set var="adminBanMessage" scope="page">
                                		<bean:message key="settings.admin.ban.tooltip" />
                            		</c:set>
                        			<agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete" data-tooltip="${adminBanMessage}" page="/admin.do?action=${ACTION_CONFIRM_DELETE}&adminID=${admin.id}">
                                		<i class="icon icon-ban"></i>
                        			</agn:agnLink>
                        		</c:when>
                        	</c:choose>    
                        
                            <c:set var="adminDeleteMessage" scope="page">
                                <bean:message key="settings.admin.delete" />
                            </c:set>
                            <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete" data-tooltip="${adminDeleteMessage}" page="/admin.do?action=${ACTION_CONFIRM_DELETE}&adminID=${admin.id}">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</html:form>
