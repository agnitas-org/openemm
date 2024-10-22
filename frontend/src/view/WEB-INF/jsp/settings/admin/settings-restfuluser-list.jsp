<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://displaytag.sf.net"               prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="adminListForm" type="com.agnitas.emm.core.admin.form.AdminListForm"--%>
<mvc:form servletRelativeAction="/restfulUser/list.action"
          modelAttribute="adminListForm"
          cssClass="form-vertical">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "admin-overview": {
                "rows-count": ${adminListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-basicSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <mvc:message code="Search"/>
            </a>
        </div>
        <div id="tile-basicSearch" class="tile-content tile-content-forms" style="padding-bottom: 0">
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchFirstName" class="control-label"><mvc:message code="Firstname"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text path="searchFirstName" id="searchFirstName" cssClass="form-control"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchLastName" class="control-label"><mvc:message code="Lastname"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text cssClass="form-control" path="searchLastName" id="searchLastName"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="searchEmail" class="control-label"><mvc:message code="mailing.MediaType.0"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text cssClass="form-control" path="searchEmail" id="searchEmail"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="filterCompanyId" class="control-label"><mvc:message code="settings.Company"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select id="filterCompanyId" path="filterCompanyId" size="1" cssClass="form-control js-select">
                                <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                                <mvc:options itemValue="id" itemLabel="shortname" items="${adminListForm.companies}"/>
                            </mvc:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label"><mvc:message code="settings.Usergroup"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="filterAdminGroupId" cssClass="form-control js-select">
                                <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                                <mvc:options itemValue="groupID" itemLabel="shortname" items="${adminListForm.adminGroups}"/>
                            </mvc:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="language" class="control-label"><mvc:message code="Language"/></label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="filterLanguage" size="1" id="language" cssClass="form-control">
                                <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                                <mvc:option value="de"><mvc:message code="settings.German"/></mvc:option>
                                <mvc:option value="en"><mvc:message code="settings.English"/></mvc:option>
                                <mvc:option value="fr"><mvc:message code="settings.French"/></mvc:option>
                                <mvc:option value="es"><mvc:message code="settings.Spanish"/></mvc:option>
                                <mvc:option value="pt"><mvc:message code="settings.Portuguese"/></mvc:option>
                                <mvc:option value="nl"><mvc:message code="settings.Dutch"/></mvc:option>
                                <mvc:option value="it"><mvc:message code="settings.Italian"/></mvc:option>
                            </mvc:select>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <div class="form-group">
                        <div class="col-sm-12">
                            <div class="btn-group pull-right">
                                <button class="btn btn-regular" type="button" data-form-set="resetSearchParams:true" data-form-submit>
                                    <mvc:message code="button.search.reset"/>
                                </button>
                                <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                                    <i class="icon icon-search"></i>
                                    <mvc:message code="Search"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="default.View"/></span>
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
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="statistics.exportFormats"/></li>
                        <li>
                            <c:url var="exportCsvUrl" value="/restfulUser/list/export/csv.action"/>
                            <a href="${exportCsvUrl}" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i>
                                <mvc:message code="user.export.csv"/>
                            </a>
                            <c:url var="exportPdfUrl" value="/restfulUser/list/export/pdf.action"/>
                            <a href="${exportPdfUrl}" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-pdf-o"></i>
                                <mvc:message code="user.export.pdf"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content" ><%--data-form-content--%>
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               pagesize="${adminListForm.numberOfRows}"
                               id="admin"
                               name="adminEntries"
                               requestURI="/restfulUser/list.action?numberOfRows=${adminListForm.numberOfRows}&__fromdisplaytag=true"
                               excludedParams="*"
                               size="${adminEntries.fullListSize}"
                               partialList="true">

                    <c:url var="restfulUserDeleteUrl" value="/restfulUser/${admin.id}/confirmDelete.action"/>
                    <c:url var="restfulUserViewUrl" value="/restfulUser/${admin.id}/view.action"/>
                    <c:url var="restfulUserSendUrl" value="/restfulUser/${admin.id}/welcome.action"/>

                    <display:column titleKey="logon.username" sortProperty="username" sortable="true"
                                    headerClass="js-table-sort" value="${admin.username}"/>

                    <display:column sortProperty="firstname" titleKey="recipient.Firstname" sortable="true"
                                    headerClass="js-table-sort" value="${admin.firstname}"/>

                    <display:column titleKey="recipient.Lastname" sortable="true" sortProperty="fullname"
                                    headerClass="js-table-sort" value="${admin.fullname}"/>

                    <display:column titleKey="mailing.MediaType.0" sortable="true" property="email" sortProperty="email"
                                    headerClass="js-table-sort" value="${admin.email}"/>

                    <display:column titleKey="default.creationDate" sortable="true" property="creationDate" sortProperty="creation_date"
                                    headerClass="js-table-sort" format="{0, date, ${adminDateFormat}}" value="${admin.creationDate}"/>

                    <display:column titleKey="default.changeDate" sortable="true" property="changeDate" sortProperty="timestamp"
                                    headerClass="js-table-sort" format="{0, date, ${adminDateFormat}}" value="${admin.changeDate}"/>

                    <display:column titleKey="admin.login.last" sortable="true" property="loginDate" sortProperty="last_login"
                                    headerClass="js-table-sort" format="{0, date, ${adminDateFormat}}" value="${admin.loginDate}"/>

                    <display:column class="table-actions">
                        <a href="${restfulUserViewUrl}" class="js-row-show hidden" title="<mvc:message code='settings.RestfulUser.edit'/> "></a>
							<emm:ShowByPermission token="admin.delete">
                            	<c:choose>
                                	<c:when test="${admin.passwordExpired}">
                                    	<mvc:message var="adminBanMessage" code="settings.RestfulUser.ban.tooltip" />
                                    	<a href="${restfulUserDeleteUrl}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${adminBanMessage}">
                                        	<i class="icon icon-ban"></i>
                                    	</a>
                                	</c:when>
                            	</c:choose>
                            </emm:ShowByPermission>
                        <emm:ShowByPermission token="admin.sendWelcome">
	                       	<mvc:message var="passwordSendMessage" code="password.send" />
    	                    <a href="${restfulUserSendUrl}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${passwordSendMessage}">
        	                    <i class="icon icon-envelope-o"></i>
            	            </a>
            	        </emm:ShowByPermission>
                        <emm:ShowByPermission token="admin.delete">
                            <mvc:message var="adminDeleteMessage" code="settings.RestfulUser.delete" />
                            <a href="${restfulUserDeleteUrl}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${adminDeleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
