<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>

<%@ page import="com.agnitas.beans.MediatypeEmail" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label" for="emailOnepixel">
            <bean:message key="openrate.measure"/>
        </label>
    </div>
    <div class="col-sm-8">
        <html:select styleId="emailOnepixel" property="emailOnepixel" size="1" styleClass="form-control js-select" disabled="${param.isEmailSettingsDisabled}">
            <html:option value="<%= MediatypeEmail.ONEPIXEL_TOP %>">
                <bean:message key="mailing.openrate.top"/>
            </html:option>
            <html:option value="<%= MediatypeEmail.ONEPIXEL_BOTTOM %>">
                <bean:message key="mailing.openrate.bottom"/>
            </html:option>
            <html:option value="<%= MediatypeEmail.ONEPIXEL_NONE %>">
                <bean:message key="openrate.none"/>
            </html:option>
        </html:select>
    </div>
</div>
