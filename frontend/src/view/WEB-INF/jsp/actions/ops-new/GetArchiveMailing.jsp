
<c:set var="localeDatePattern" value="dd.mm.yyyy"/>
<script id="module-GetArchiveMailing" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <div class="form-group">
            <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="GetArchiveMailing"/>
            <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.expireDate"><mvc:message code="action.op.GetArchiveMailing.expireDate"/></label>
            </div>
            <div class="col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <input type="text" name="modules[].expireDate" id="module_{{- index}}.expireDate" value="{{- expireDate}}"
                               class="form-control datepicker-input js-datepicker"
                               data-datepicker-options="format: '${localeDatePattern}'">
                    </div>
                    <div class="input-group-btn">
                        <button class="btn btn-regular btn-toggle js-open-datepicker" type="button">
                            <i class="icon icon-calendar-o"></i>
                        </button>
                    </div>
                </div>
                <p class="help-block"><mvc:message code="default.date.format.DD.MM.YYYY"/></p>
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





