<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="tags" type="java.util.List<com.agnitas.beans.AgnTagDto>"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" data-controller="wysiwyg-agn-tags">
            <script type="application/json" data-initializer="wysiwyg-agn-tags">
                ${emm:toJson(tags)}
            </script>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="htmled.agntagsWindowTitle" /></h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-12">
                        <div class="notification-simple notification-simple--lg notification-simple--info">
                            <span><mvc:message code="info.manual.agnTags" arguments="${emm:getHelpUrl(pageContext.request, 'agnTags')}" /></span>
                        </div>
                    </div>

                    <div class="col-12">
                        <label for="agn-tag-name" class="form-label"><mvc:message code="htmled.tag"/></label>
                        <select id="agn-tag-name" class="form-control js-select" data-action="select-agn-tag">
                            <%-- To be populated by JS --%>
                        </select>
                    </div>

                    <div id="agn-tag-attributes" class="col-12 d-flex flex-column gap-3">
                        <%-- To be populated by JS --%>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary flex-grow-1" data-action="insert-agn-tag">
                    <i class="icon icon-save"></i>
                    <span class="text">
                        <mvc:message code="button.Apply"/>
                    </span>
                </button>
            </div>
        </div>
    </div>

    <script type="text/x-mustache-template" id="agn-tag-select-attribute">
        <div>
            <label class="form-label" for="agn-tag-attribute-{{- index }}">{{- name.replace(/_/g,' ')}}:</label>
            <select class="form-control js-select" id="agn-tag-attribute-{{- index }}">
                {{ _.each(options, function(value, key) { }}
                <option value="{{- key }}">{{- value }}</option>
                {{ }) }}
            </select>
        </div>
    </script>

    <script type="text/x-mustache-template" id="agn-tag-text-attribute">
        <div>
            <label class="form-label" for="agn-tag-attribute-{{- index }}">{{- name.replace(/_/g,' ')}}:</label>
            <input type="text" class="form-control" id="agn-tag-attribute-{{- index }}"/>
        </div>
    </script>

    <c:set var="linkCreationExtendedAttrs">
        <div class="d-flex flex-column gap-1">
            <div class="form-check form-switch">
                <input type="checkbox" class="form-check-input" id="createLinkToggle" role="switch"/>
                <label class="form-label form-check-label" for="createLinkToggle">
                    <mvc:message code="TrackableLink.createLink"/>
                </label>
            </div>

            <mvc:message var="linkTextMsg" code="TrackableLink.linkText"/>
            <input type="text" class="form-control" id="tagLinkText" placeholder="${linkTextMsg}"/>
        </div>
    </c:set>

    <script type="text/x-mustache-template" id="agnFORM-extended-attributes">
        ${linkCreationExtendedAttrs}
    </script>

    <script type="text/x-mustache-template" id="agnFULLVIEW-extended-attributes">
        ${linkCreationExtendedAttrs}
    </script>

    <script type="text/x-mustache-template" id="agnWEBVIEW-extended-attributes">
        ${linkCreationExtendedAttrs}
    </script>
</div>
