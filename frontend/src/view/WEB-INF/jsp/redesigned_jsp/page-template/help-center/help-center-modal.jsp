<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div id="help-center-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-dialog-scrollable modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <mvc:message code="help" />
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-header mt-1">
                <nav class="navbar navbar-expand-lg">
                    <a class="chosen-tab btn btn-primary" href="#"></a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" data-bs-target="#help-center-navbar" aria-controls="help-center-navbar" aria-expanded="false">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="help-center-navbar">
                        <ul class="navbar-nav offcanvas-body">
                            <%@ include file="tab/help-center-docs-tab.jspf" %>
                            <li class="nav-item">
                                <a class="btn btn-outline-primary" href="#" data-toggle-tab="#help-center-manual-tab" data-bs-dismiss="offcanvas"><mvc:message code="manual.gui"/></a>
                            </li>
                            <%@ include file="tab/help-center-support-tab.jspf" %>
                        </ul>
                    </div>
                </nav>
            </div>
            
            <div class="modal-body pt-2" data-controller="help-center">
                <%@ include file="help-center-docs.jspf" %>
                <%@ include file="help-center-manual.jspf" %>
                <%@ include file="help-center-support.jspf" %>
            </div>
        </div>
    </div>
</div>
