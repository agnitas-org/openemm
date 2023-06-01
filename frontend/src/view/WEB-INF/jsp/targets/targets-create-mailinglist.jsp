<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="targetIdToCreateMailingList" type="java.lang.Integer"--%>

<emm:Permission token="targets.show"/>

<c:set var="EMAIL_MEDIATYPE_CODE" value="<%= MediaTypes.EMAIL %>" scope="page"/>
<c:set var="POST_MEDIATYPE_CODE" value="<%= MediaTypes.POST %>" scope="page"/>
<c:set var="SMS_MEDIATYPE_CODE" value="<%= MediaTypes.SMS %>" scope="page"/>

<div class="modal" data-controller="target-create-mailinglist">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailinglist/create.action" method="POST">
                <input type="hidden" name="targetId" value="${targetIdToCreateMailingList}"/>
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title"><mvc:message code="target.MailingListFromTargetQuestion"/></h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label"><mvc:message code="mediatype.mediatypes"/></label>
                        </div>
                        <div class="col-sm-8">
                            <div class="checkbox">
                                <label>
                                    <input type="checkbox" name="mediatypes" value="${EMAIL_MEDIATYPE_CODE}" checked data-action="select-mediatype">
                                    <mvc:message code="mailing.MediaType.email"/>
                                </label>
                                <emm:ShowByPermission token="mediatype.sms">
                                    <br/>
                                    <label>
                                        <input type="checkbox" name="mediatypes" value="${SMS_MEDIATYPE_CODE}" data-action="select-mediatype">
                                        <mvc:message code="mailing.MediaType.sms"/>
                                    </label>
                                </emm:ShowByPermission>
                                <emm:ShowByPermission token="mediatype.post">
                                    <br/>
                                    <label>
                                        <input type="checkbox" name="mediatypes" value="${POST_MEDIATYPE_CODE}" data-action="select-mediatype">
                                        <mvc:message code="mailing.MediaType.post"/>
                                    </label>
                                </emm:ShowByPermission>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative"
                                data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" id="createMailinglistOkBtn" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.OK"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
