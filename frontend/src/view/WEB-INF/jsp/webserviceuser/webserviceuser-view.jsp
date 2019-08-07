<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>

<mvc:form servletRelativeAction="/administration/wsmanager/user/save.action"
          data-form-focus="password"
          id="editWebserviceUserForm"
          modelAttribute="webserviceUserForm"
          data-form="resource">

    <mvc:hidden path="userName"/>
    <mvc:hidden path="companyId"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="webserviceuser.edit" />
            </h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="userNameTitle" class="control-label">
                        <bean:message key="logon.username" />
                    </label>
                </div>
                <div class="col-sm-8">
                    <input type="text" value="${webserviceUserForm.userName}" disabled="disabled" id="userNameTitle" class="form-control">
                </div>
            </div>

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
                    <label for="contactInfo" class="control-label">
                        <bean:message key="webserviceuser.contact_info" />
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="contactInfo" cssClass="form-control" id="contactInfo"/>
                </div>
            </div>

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
                    <label for="passwordRepeat" class="control-label">
                        <bean:message key="password.repeat" />
                    </label>
                </div>
                <div class="col-sm-8">

                    <mvc:password path="passwordRepeat" cssClass="form-control" id="passwordRepeat"/>
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
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label>
                            <mvc:checkbox path="active" id="active"/>
                            <bean:message key="default.status.active" />
                        </label>
                    </div>
                </div>
            </div>

        </div>

    </div>
</mvc:form>
