<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div id="upselling-modal" class="modal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <img src="<c:url value="/assets/core/images/upselling/InboxPreview.svg" />" alt="Inbox-Preview">
            </div>
            <div class="modal-body vstack gap-3">
                <h2><mvc:message code="upselling.headline.inboxPreview"/></h2>
                <p><mvc:message code="upselling.description.inboxPreview"/></p>
                <p><mvc:message code="upselling.feature.interest" /></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary flex-none" data-bs-dismiss="modal">
                    <i class="icon icon-reply"></i>
                    <span class="text"> <mvc:message code="button.Back"/></span>
                </button>

                <a type="button" href="mailto:sales@agnitas.de?subject=<mvc:message code="mailing.provider.preview"/>" class="btn btn-primary">
                    <span class="text"><mvc:message code="general.upselling.information" /></span>
                    <i class="icon icon-external-link-alt"></i>
                </a>
            </div>
        </div>
    </div>
</div>
