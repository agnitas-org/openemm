<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="tiles-container">
    <div class="tile" style="height: min-content">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="mailing.mode.select" /></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="d-flex flex-column gap-3">
                <a href="<c:url value="/recipient/import/view.action"/>" class="horizontal-card" data-confirm>
                    <div class="horizontal-card__header">
                        <span class="horizontal-card__icon icon-badge text-bg-success">
                            <i class="icon icon-file-import text-white"></i>
                        </span>
                    </div>
                    <div class="horizontal-card__body">
                        <p class="horizontal-card__title"><mvc:message code="import.standard"/></p>
                        <p class="horizontal-card__subtitle"><mvc:message code="import.standard.description"/></p>
                    </div>
                </a>

                <a href="<c:url value="/recipient/import/wizard/step/file.action"/>" class="horizontal-card">
                    <div class="horizontal-card__header">
                        <span class="horizontal-card__icon icon-badge text-bg-dark-violet">
                            <i class="icon icon-hat-wizard"></i>
                        </span>
                    </div>
                    <div class="horizontal-card__body">
                        <p class="horizontal-card__title"><mvc:message code="import.Wizard"/></p>
                        <p class="horizontal-card__subtitle"><mvc:message code="import.wizard.description"/></p>
                    </div>
                </a>
            </div>
        </div>
    </div>
</div>
