<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>
<%--@elvariable id="webserviceUserListForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserListForm"--%>
<%--@elvariable id="companyList" type="java.util.List<com.agnitas.emm.core.company.bean.CompanyEntry>"--%>

<emm:ShowByPermission token="webservice.user.show">

    <mvc:form servletRelativeAction="/administration/wsmanager/user/new.action" id="createWsmanagerUserForm" modelAttribute="webserviceUserForm"
              data-form-focus="userName" data-form="resource" >
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline">
                    <bean:message key="webserviceuser.create" />
                </h2>

                <ul class="tile-header-actions">
                    <li>
                        <button type="button" class="btn btn-regular btn-primary" data-form-set="isShowStatistic: true" data-form-submit-static>
                            <i class="icon icon-plus"></i>
                            <span class="text"><bean:message key="button.Create"/></span>
                        </button>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="wsUserName" class="control-label">
                            <bean:message key="logon.username" />
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="userName" cssClass="form-control" id="wsUserName"/>
                    </div>
                </div>
                <logic:messagesPresent property="userName">
                    <div class="form-group">
                        <div class="col-sm-4">&nbsp;</div>
                        <div class="col-sm-8">
                            <html:messages id="msg" message="false" property="userName">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br/>
                            </html:messages>
                        </div>
                    </div>
                </logic:messagesPresent>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="email">
                            <bean:message key="settings.Admin.email"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="email" id="email" cssClass="form-control" size="52" maxlength="99"/>
                    </div>
                </div>
                <logic:messagesPresent property="email">
                    <div class="form-group">
                        <div class="col-sm-4">&nbsp;</div>
                        <div class="col-sm-8">
                            <html:messages id="msg" message="false" property="email">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br />
                            </html:messages>
                        </div>
                    </div>
                </logic:messagesPresent>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="password" class="control-label">
                            <bean:message key="password" />
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:password path="password" cssClass="form-control" id="password"/>
                    </div>
                </div>
                <logic:messagesPresent property="password">
                    <div class="form-group">
                        <div class="col-sm-4">&nbsp;</div>
                        <div class="col-sm-8">
                            <html:messages id="msg" message="false" property="password">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br/>
                            </html:messages>

                        </div>
                    </div>
                </logic:messagesPresent>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="wsUserPasswordRepeat" class="control-label">
                            <bean:message key="password.repeat" />
                        </label>
                    </div>
                    <div class="col-sm-8">

                        <mvc:password path="passwordRepeat" cssClass="form-control" id="wsUserPasswordRepeat"/>
                    </div>
                </div>
                <logic:messagesPresent property="passwordRepeat">
                    <div class="form-group">
                        <div class="col-sm-4">&nbsp;</div>
                        <div class="col-sm-8">
                            <html:messages id="msg" message="false" property="passwordRepeat">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br/>
                            </html:messages>
                        </div>
                    </div>
                </logic:messagesPresent>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="wsUserCompanyId" class="control-label">
                            <bean:message key="webserviceuser.company" />
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="companyId" cssClass="form-control js-select" id="wsUserCompanyId">
                            <mvc:option value="-1"><bean:message key="select.company"/></mvc:option>
                            <mvc:options items="${companyList}" itemValue="companyId" itemLabel="shortname"/>
                        </mvc:select>
                    </div>
                </div>
                <logic:messagesPresent property="companyId">
                    <div class="form-group">
                        <div class="col-sm-4">&nbsp;</div>
                        <div class="col-sm-8">
                            <html:messages id="msg" message="false" property="companyId">
                                <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br/>
                            </html:messages>

                        </div>
                    </div>
                </logic:messagesPresent>

            </div>

        </div>
    </mvc:form>

</emm:ShowByPermission>


<mvc:form servletRelativeAction="/administration/wsmanager/users.action" modelAttribute="webserviceUserListForm">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "ws-manager-overview": {
                "rows-count": ${webserviceUserListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.Overview" />
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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="allowedChange" value="false"/>
                <emm:ShowByPermission token="webservice.user.change">
                    <c:set var="allowedChange" value="true"/>
                </emm:ShowByPermission>

                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="wsUser"
                               name="webserviceUserList"
                               requestURI="/administration/wsmanager/users.action"
                               pagesize="${webserviceUserListForm.numberOfRows}"
                               excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column headerClass="js-table-sort"
                                    property="userName" titleKey="logon.username"
                                    sortable="true" sortProperty="username"/>

                    <display:column headerClass="js-table-sort"
                                    titleKey="default.status.active"
                                    sortable="true" sortProperty="active">
                        <c:set var="activeMsgKey" value="${wsUser.active ? 'default.status.active' : 'webserviceuser.not_active'}"/>
                        <bean:message key="${activeMsgKey}"/>
                    </display:column>

                    <display:column headerClass="js-table-sort"
                                    property="companyId" titleKey="webserviceuser.company"
                                    sortable="true" sortProperty="company_id"/>

                    <display:column headerClass="js-table-sort"
                                    property="dataSourceId" titleKey="webserviceuser.default_datasource_id"
                                    sortable="true" sortProperty="default_data_source_id"/>


                    <display:column class="table-actions ${allowedChange ? '' : 'hidden'}" headerClass="${allowedChange ? '' : 'hidden'}" sortable="false">
                        <c:url var="viewWsUserLink" value="/administration/wsmanager/user/${wsUser.userName}/view.action"/>

                        <a href="${viewWsUserLink}" class="hidden js-row-show"></a>

                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
