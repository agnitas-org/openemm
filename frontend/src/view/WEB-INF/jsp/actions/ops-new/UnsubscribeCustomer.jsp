
<script id="module-UnsubscribeCustomer" type="text/x-mustache-template">

    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" value="UnsubscribeCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
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
