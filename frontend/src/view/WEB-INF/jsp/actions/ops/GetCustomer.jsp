<%@ page language="java" import="org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue();
   String loadAlways = "actions[" + index + "].loadAlways";
%>

<div class="inline-tile-content">
    <html:hidden property='<%= "__STRUTS_CHECKBOX_"+loadAlways %>' value="false"/>
    <div class="form-group">
        <div class="col-sm-offset-4 col-sm-8">
            <div class="checkbox">
                <label>
                    <html:checkbox property="<%= loadAlways %>"/>
                    <bean:message key="action.getcustomer.loadalways"/>
                </label>
            </div>
        </div>
    </div>
</div>
<div class="inline-tile-footer">
    <emm:ShowByPermission token="actions.change">
        <a class="btn btn-regular" href="#" data-form-set="action: <%= EmmActionAction.ACTION_REMOVE_MODULE %>, deleteModule: <%= index %>" data-form-submit>
            <i class="icon icon-trash-o"></i>
            <span class="text"><bean:message key="button.Delete"/></span>
        </a>
    </emm:ShowByPermission>
</div>
