<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<emm:CheckLogon/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><bean:message key="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <bean:message key="ClearAllProperties"/>
                </h4>
            </div>

            <agn:agnForm action="/tracklink.do" id="trackableLinkForm">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                
                <c:forEach var="linkId" items="${trackableLinkForm.bulkIDs}">
                    <html:hidden property="bulkID[${linkId}]"/>
                </c:forEach>

                <div class="modal-body">
                    <bean:message key="bulkAction.delete.trackablelink.extension.question"/>
                    <div class="element-inline">
                        <ul>
                            <c:forEach var="extension" items="${extensionsToDelete}">
                                <li><label>${extension.propertyName}=${extension.propertyValue}</label></li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="default.No"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="default.Yes"/></span>
                        </button>
                    </div>
                </div>
            </agn:agnForm>
        </div>
    </div>
</div>
