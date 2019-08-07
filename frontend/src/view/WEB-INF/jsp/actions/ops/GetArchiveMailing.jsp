<%@ page language="java" import="org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="localeDatePattern" value="dd.mm.yyyy"/>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>
<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="action.op.GetArchiveMailing.expireDate"/></label>
        </div>
        <div class="col-sm-8">
            <div class="input-group">
                <div class="input-group-controls">
                    <input type="text" class="form-control datepicker-input js-datepicker" name='<%= "actions[" + index + "].expireDay" %>' size="2" data-datepicker-options="format: '${localeDatePattern}'"/>
                </div>
                <div class="input-group-btn">
                    <button class="btn btn-regular btn-toggle js-open-datepicker" type="button">
                        <i class="icon icon-calendar-o"></i>
                    </button>
                </div>
            </div>
            <p class="help-block"><bean:message key="default.date.format.DD.MM.YYYY"/></p>
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
