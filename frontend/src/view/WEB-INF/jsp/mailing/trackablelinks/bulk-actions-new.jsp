<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="KEEP_UNCHANGED" value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>

<div class="modal modal-extra-wide" >
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="bulkActionsForm" onsubmit="return false;">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title">
                        <mvc:message code="bulkAction"/>
                    </h4>
                </div>
                <div class="modal-body">
                    <%@include file="fragments-new/bulk_action/link-description.jspf" %>
                    <%@include file="fragments-new/bulk_action/link-measurable.jspf" %>
                    <%@include file="fragments-new/bulk_action/link-actions.jspf" %>
                    <%@include file="fragments-new/bulk_action/link-list-revenue.jspf" %>
                    <%@include file="fragments-new/bulk_action/link-static.jspf" %>
                    <%@include file="fragments-new/bulk_action/link-extensions.jspf" %>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large pull-left" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-large btn-primary"
                                data-form-target="#trackableLinkForm"
                                data-form-set="everyPositionLink: false"
                                data-action="save-bulk-actions">
                            <span class="text"><mvc:message code="button.Save"/></span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
