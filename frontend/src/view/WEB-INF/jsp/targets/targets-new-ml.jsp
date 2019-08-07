<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <c:url var="createMailinglistUrl" value="/mailinglist/create.action"/>
            <form action="${createMailinglistUrl}" method="GET">
                <input type="hidden" name="targetId" id="targetId" value="${targetForm.targetID}"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only"><bean:message key="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title"><bean:message key="target.MailingListFromTargetQuestion"/></h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                         <div class="col-sm-4">
                            <label class="control-label"><bean:message key="mediatype.mediatypes"/></label>
                        </div>
                        <div class="col-sm-8">
                            <div class="checkbox">
                                <label>
                                    <input type="checkbox" checked><bean:message key="mailing.MediaType.0"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.OK"/></span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
