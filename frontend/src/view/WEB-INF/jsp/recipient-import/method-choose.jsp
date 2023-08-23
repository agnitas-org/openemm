<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="col-md-offset-3 col-md-6">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="mailing.mode.select"/>:</h2>
        </div>
        <div class="tile-content">
            <ul class="link-list">
                <li>
                    <a href="<c:url value="/recipient/import/view.action"/>" class="link-list-item">
                        <div class="thumbnail">
                            <img alt="" class="media-object" src="<c:url value="/assets/core/images/facelift/agn_import_standard.png" />">
                        </div>
                        <div class="media-body">
                            <p class="headline">
                                <mvc:message code="import.standard"/>
                            </p>
                            <p class="description">
                                <mvc:message code="import.standard.description"/>
                            </p>
                        </div>
                        <i class="nav-arrow icon icon-angle-right"></i>
                    </a>
                </li>
                <li>
                    <emm:ShowByPermission token="import.wizard.rollback">
                        <c:url var="importWizardLink" value="/importwizard.do?action=1"/>
                    </emm:ShowByPermission>
                    <emm:HideByPermission token="import.wizard.rollback">
                        <c:url var="importWizardLink" value="/recipient/import/wizard/step/file.action"/>
                    </emm:HideByPermission>

                    <a href="${importWizardLink}" class="link-list-item">
                        <div class="thumbnail">
                            <img alt="" class="media-object" src="<c:url value="/assets/core/images/facelift/agn_mailing-new-assistant.png" />">
                        </div>
                        <div class="media-body">
                            <p class="headline">
                                <mvc:message code="import.Wizard"/>
                            </p>
                            <p class="description">
                                <mvc:message code="import.wizard.description"/>
                            </p>
                        </div>
                        <i class="nav-arrow icon icon-angle-right"></i>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</div>
