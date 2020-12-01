
<script id="module-ActivateDoubleOptIn" type="text/x-mustache-template">
    <emm:ShowByPermission token="actions.change">
        <div class="inline-tile-content" data-module-content="{{- index}}">
            <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="ActivateDoubleOptIn"/>
            <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="action.op.ActivateDOI.mailinglists"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        {{ if (forAllLists) { }}
                            <input type="checkbox" name="modules[].forAllLists" id="module_{{- index}}.forAllLists" checked="ckecked"/>
                        {{ } else { }}
                            <input type="checkbox" name="modules[].forAllLists" id="module_{{- index}}.forAllLists" />
                        {{ } }}

                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
        </div>
        <div class="inline-tile-content">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="module_{{- index}}.mediaTypeCode"><mvc:message code="action.op.ActivateDOI.mediatype"/></label>
                </div>
                <div class="col-sm-8">
                    <select name="modules[].mediaTypeCode" id="module_{{- index}}.mediaTypeCode" size="1"
                            value="{{- mediaTypeCode}}" class="form-control js-select">
                        <c:forEach items="${MediaTypes.values()}" var="mediaType">
                            <emm:ShowByPermission token="${mediaType.requiredPermission}">
                                <option value="${mediaType.mediaCode}"><mvc:message code="mailing.MediaType.${mediaType.mediaCode}" /></option>
                            </emm:ShowByPermission>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="inline-tile-footer">
            <a class="btn btn-regular" href="#" data-action="action-delete-module" data-property-id="{{- index}}">
                <i class="icon icon-trash-o"></i>
                <span class="text"><mvc:message code="button.Delete"/></span>
            </a>
        </div>
    </emm:ShowByPermission>
</script>
