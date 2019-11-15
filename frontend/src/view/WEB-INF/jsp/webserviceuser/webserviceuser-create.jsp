<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>
<%--@elvariable id="companyList" type="java.util.List<com.agnitas.emm.core.company.bean.CompanyEntry>"--%>

<emm:ShowByPermission token="webservice.user.show">

    <mvc:form servletRelativeAction="/administration/wsmanager/user/new.action" id="wsuser-creation-form"
               data-resource-selector="#wsuser-creation-form" modelAttribute="webserviceUserForm"
              data-form-focus="userName" data-form="resource">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline">
                    <mvc:message code="webserviceuser.create" />
                </h2>

                <ul class="tile-header-actions">
                    <li>
                        <button type="button" class="btn btn-regular btn-primary" data-form-submit>
                            <i class="icon icon-plus"></i>
                            <span class="text"><mvc:message code="button.Create"/></span>
                        </button>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="wsUserName" class="control-label">
                            <mvc:message code="logon.username" />
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="userName" cssClass="form-control" id="wsUserName"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="email">
                            <mvc:message code="settings.Admin.email"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="email" id="email" cssClass="form-control" size="52" maxlength="99"/>
                    </div>
                </div>

                <div data-field="password">
                    <%--Password--%>
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="password" class="control-label" >
                                <mvc:message code="password.new"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:password path="password" id="password" cssClass="form-control js-password-strength" size="52" maxlength="99" data-field-required=""/>
                                </div>
                                <div class="input-group-addon">
                                    <span class="addon js-password-strength-indicator hidden">
                                        <i class="icon icon-check"></i>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%--Password repeat--%>
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="passwordRepeat" class="control-label" ><mvc:message code="password.repeat"/></label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <input type="password" id="passwordRepeat" class="form-control js-password-match" size="52" maxlength="99"/>
                                </div>
                                <div class="input-group-addon">
                                    <span class="addon js-password-match-indicator hidden">
                                        <i class="icon icon-check"></i>
                                     </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

				<emm:ShowByPermission token="master.companies.show">
	                <div class="form-group">
	                    <div class="col-sm-4">
	                        <label for="wsUserCompanyId" class="control-label">
	                            <mvc:message code="webserviceuser.company" />
	                        </label>
	                    </div>
	                    <div class="col-sm-8">
	                        <mvc:select path="companyId" cssClass="form-control js-select" id="wsUserCompanyId">
	                            <mvc:option value="-1"><mvc:message code="select.company"/></mvc:option>
	                            <mvc:options items="${companyList}" itemValue="companyId" itemLabel="shortname"/>
	                        </mvc:select>
	                    </div>
	                </div>
				</emm:ShowByPermission>
            </div>

        </div>
    </mvc:form>
</emm:ShowByPermission>

