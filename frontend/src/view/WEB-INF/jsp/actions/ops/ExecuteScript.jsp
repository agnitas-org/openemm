
<script id="module-ExecuteScript" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="ExecuteScript"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
        <div class="inline-tile form-group" data-field="validator">
            <div class="inline-tile-header">
                <h2 class="headline"><mvc:message code="Script"/></h2>
                <ul class="inline-tile-header-actions">
                    <li>
                        <a href="#" data-modal="action-modal-editor" data-modal-set="title: <mvc:message code="Script"/>,
                                target: module_{{-index}}_script, id: textTemplateLarge, type: text" data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                           <i class="icon icon-arrows-alt"></i>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="inline-tile-content">
                <textarea name="modules[].script" id="module_{{-index}}_script" data-field-validator="reject-script-element"
                       class="form-control js-editor" rows="25" cols="75">{{- script}}</textarea>
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
