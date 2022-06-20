<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="tags" type="java.util.List<com.agnitas.beans.AgnTagDto>"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content" data-controller="wysiwyg-agn-tags">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title"><mvc:message code="htmled.agntagsWindowTitle"/></h4>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="agn-tag-name"><mvc:message code="htmled.tag"/>:</label>
                    </div>
                    <div class="col-sm-8">
                        <select id="agn-tag-name" class="form-control js-select" data-action="select-agn-tag">
                            <%-- To be populated by JS --%>
                        </select>
                    </div>
                </div>

                <div id="agn-tag-attributes">
                    <%-- To be populated by JS --%>
                </div>

                <script type="text/x-mustache-template" id="agn-tag-select-attribute">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="agn-tag-attribute-{{- index }}">{{- name.replace(/_/g,' ')}}:</label>
                        </div>
                        <div class="col-sm-8">
                            <select class="form-control js-select" id="agn-tag-attribute-{{- index }}">
                                {{ _.each(options, function(value, key) { }}
                                <option value="{{- key }}">{{- value }}</option>
                                {{ }) }}
                            </select>
                        </div>
                    </div>
                </script>

                <script type="text/x-mustache-template" id="agn-tag-text-attribute">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="agn-tag-attribute-{{- index }}">{{- name.replace(/_/g,' ')}}:</label>
                        </div>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="agn-tag-attribute-{{- index }}"/>
                        </div>
                    </div>
                </script>

                <script type="text/x-mustache-template" id="agnFORM-extended-attributes">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label"><mvc:message code="TrackableLink.createLink"/>:</label>
                        </div>
                        <div class="col-sm-8">
                            <label class="toggle">
                                <input type="checkbox" class="form-control" id="createLinkToggle"/>
                                <div class="toggle-control"></div>
                            </label>
                        </div>
                        <div id="tagLinkParams">
                            <div class="col-sm-4">
                                <label class="control-label"><mvc:message code="TrackableLink.linkText"/>:</label>
                            </div>
                            <div class="col-sm-8">
                                <input type="text" class="form-control" id="tagLinkText"/>
                            </div>
                        </div>
                    </div>
                </script>

                <script type="application/json" data-initializer="wysiwyg-agn-tags">
                    ${emm:toJson(tags)}
                </script>
            </div>

            <div class="modal-footer">
                <div class="btn-group">
                    <button type="button" class="btn btn-default btn-large pull-left js-confirm-negative" data-dismiss="modal">
                        <i class="icon icon-times"></i>
                        <span class="text"><mvc:message code="button.Cancel"/></span>
                    </button>
                    <button type="button" class="btn btn-primary btn-large" data-action="insert-agn-tag">
                        <i class="icon icon-check"></i>
                        <span class="text"><mvc:message code="button.Apply"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
