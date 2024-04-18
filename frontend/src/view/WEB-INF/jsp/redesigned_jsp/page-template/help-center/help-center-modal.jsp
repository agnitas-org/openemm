<%@ taglib prefix="mvc"  uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"  uri="https://emm.agnitas.de/jsp/jsp/common" %>

<div id="help-center-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-dialog-scrollable modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="help" /></h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-header mt-1">
                <nav class="navbar navbar-expand-lg">
                    <a class="chosen-tab btn btn-primary" href="#"><span class="text text-truncate"></span></a>
                    <button class="navbar-toggler btn-icon-sm" type="button" data-bs-toggle="offcanvas" data-bs-target="#help-center-navbar" aria-controls="help-center-navbar" aria-expanded="false">
                        <i class="icon icon-bars"></i>
                    </button>
                    <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="help-center-navbar">
                        <ul class="navbar-nav offcanvas-body">
                            <%@ include file="tab/help-center-docs-tab.jspf" %>
                            <li class="nav-item">
                                <a class="btn btn-outline-primary" href="#" data-toggle-tab="#help-center-manual-tab" data-bs-dismiss="offcanvas" data-help-tab="manual"><mvc:message code="manual.gui"/></a>
                            </li>
                            <%@ include file="tab/help-center-support-tab.jspf" %>
                            <emm:ShowByPermission token="ai.support.chat">
                                <li class="nav-item">
                                    <a class="btn btn-outline-primary" href="#" data-toggle-tab="#support-chat-tab" data-bs-dismiss="offcanvas" data-help-tab="support-chat" data-tooltip="<mvc:message code="beta.feature" />">
                                        <span class="text"><mvc:message code="GWUA.aiSupportChat"/></span>
                                        <i class="icon icon-flask"></i>
                                    </a>
                                </li>
                            </emm:ShowByPermission>
                        </ul>
                    </div>
                </nav>
            </div>
            
            <div class="modal-body pt-1" data-controller="help-center" data-initializer="help-center">
                <%@ include file="help-center-docs.jspf" %>
                <%@ include file="help-center-manual.jspf" %>
                <%@ include file="help-center-support.jspf" %>

                <emm:ShowByPermission token="ai.support.chat">
                    <%@ include file="help-center-support-chat.jspf" %>
                </emm:ShowByPermission>
            </div>
        </div>
    </div>
</div>
