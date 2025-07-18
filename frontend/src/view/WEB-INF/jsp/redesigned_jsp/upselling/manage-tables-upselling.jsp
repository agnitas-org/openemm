<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div id="upselling-modal" class="modal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <img src="<c:url value="/assets/core/images/upselling/ReferenceTables.svg" />" alt="Reference tables">
            </div>
            <div class="modal-body d-flex flex-column gap-3">
                <h2><mvc:message code="upselling.headline.referenceTables"/></h2>
                <p><mvc:message code="upselling.description.referenceTables"/></p>
                <p><mvc:message code="upselling.feature.interest" /></p>
            </div>
            <div class="modal-footer">
                <c:choose>
                    <c:when test="${adminLocale eq 'de_DE'}">
                        <c:set var="moreInfoUrl" value="https://www.agnitas.de/e-marketing-manager/premium-features/retargeting-package" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="moreInfoUrl" value="https://www.agnitas.de/en/e-marketing-manager/premium-features/retargeting-package" />
                    </c:otherwise>
                </c:choose>

                <button type="button" class="btn btn-secondary flex-none" data-bs-dismiss="modal">
                    <i class="icon icon-reply"></i>
                    <span class="text"> <mvc:message code="button.Back"/></span>
                </button>

                <a type="button" href="#" class="btn btn-primary" data-popup="${moreInfoUrl}">
                    <span class="text"><mvc:message code="general.upselling.information" /></span>
                    <i class="icon icon-external-link-alt"></i>
                </a>
            </div>
        </div>
    </div>
</div>
