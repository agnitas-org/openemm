
<script id="module-ContentView" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="ContentView"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="action.Tagname"/></label>
            </div>
            <div class="col-sm-8">
                <input type="text" name="modules[].tagName" id="module_{{- index}}.tagName" value="{{- tagName}}" class="form-control"/>
            </div>
        </div>
    </div>
    <div class="inline-tile-footer">
        <emm:ShowByPermission token="actions.change">
            <a class="btn btn-regular" href="#" data-action="action-delete-module" data-property-id="{{- index}}">
                <i class="icon icon-trash-o"></i>
                <span class="text"><mvc:message code="button.Delete"/></span>
            </a>
        </emm:ShowByPermission>
    </div>
</script>
