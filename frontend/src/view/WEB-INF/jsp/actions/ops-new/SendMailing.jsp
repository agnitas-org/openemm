
<script id="module-SendMailing" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <div class="form-group">
            <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="SendMailing"/>
            <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.mailingID"><mvc:message code="Mailing"/></label>
            </div>
            <div class="col-sm-8">
                <select name="modules[].mailingID" id="module_{{- index}}.mailingID" class="form-control js-select" size="1" value="{{- mailingID}}">
                    <option value="0"><mvc:message code="error.report.select_mailing"/></option>
                    <c:forEach items="${eventBasedMailings}" var="mailing">
                        <option value="${mailing.id}">${mailing.shortname}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.delayMinutes"><mvc:message code="Delay"/></label>
            </div>
            <div class="col-sm-8">
                <select name="modules[].delayMinutes" id="module_{{- index}}.delayMinutes" class="form-control js-select" size="1" value="{{- delayMinutes}}">
                    <option value="0"><mvc:message code="action.No_Delay"/></option>
                    <option value="60">1&nbsp;<mvc:message code="Hour"/></option>
                    <option value="360">6&nbsp;<mvc:message code="Hours"/></option>
                    <option value="720">12&nbsp;<mvc:message code="Hours"/></option>
                    <option value="1440">1&nbsp;<mvc:message code="Day"/></option>
                    <option value="2880">2&nbsp;<mvc:message code="Days"/></option>
                    <option value="5760">4&nbsp;<mvc:message code="Days"/></option>
                    <option value="10080">7&nbsp;<mvc:message code="Days"/></option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.bcc">
                    <mvc:message code="action.address.bcc"/>
                    <button class="icon icon-help" data-help="help_${helplanguage}/actions/BCCMsg.xml" tabindex="-1" type="button"></button>
                </label>
            </div>

            <div class="col-sm-8">
                <input type="text" name="modules[].bcc" id="module_{{- index}}.bcc" class="form-control" value="{{- bcc}}">
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
