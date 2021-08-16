
<script id="module-GetArchiveList" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="GetArchiveList"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.campaignID"><mvc:message code="mailing.archive"/></label>
            </div>
            <div class="col-sm-8">
                <select name="modules[].campaignID" id="module_{{- index}}.campaignID" size="1" class="form-control js-select">
                    <c:forEach items="${archives}" var="campaign">
                        {{ var selectedSign = ${campaign.id} == campaignID ? 'selected="selected"' : ''; }}
                        <option value="${campaign.id}" {{- selectedSign}}>${campaign.shortname}</option>
                    </c:forEach>
                </select>
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
