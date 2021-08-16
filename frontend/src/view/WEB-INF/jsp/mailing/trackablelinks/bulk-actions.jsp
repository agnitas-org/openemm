<%@ page import="com.agnitas.web.ComTrackableLinkAction" %>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_SAVE_ALL" value="<%= ComTrackableLinkAction.ACTION_SAVE_ALL %>"/>
<c:set var="KEEP_UNCHANGED" value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>

<div class="modal modal-extra-wide" >
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="bulkActionsForm" onsubmit="return false;">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only"><bean:message key="button.Cancel"/></span>
                    </button>
                    <h4 class="modal-title">
                        <bean:message key="bulkAction"/>
                    </h4>
                </div>
                <div class="modal-body">
                    <%@include file="fragments/bulk_action/link-description.jspf" %>
                    <%@include file="fragments/bulk_action/link-measurable.jspf" %>
                    <%@include file="fragments/bulk_action/link-actions.jspf" %>
                    <%@include file="../mailing-trackablelink-list-revenue.jspf" %>
                    <%@include file="fragments/bulk_action/link-static.jspf" %>
                    <%@include file="fragments/bulk_action/link-extensions.jspf" %>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large pull-left" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-large btn-primary"
                                data-form-target="#trackableLinkForm"
                                data-form-set="everyPositionLink: false"
                                data-action="save-bulk-actions">
                            <span class="text"><bean:message key="button.Save"/></span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
