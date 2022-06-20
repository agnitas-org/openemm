
<emm:instantiate var="delaysMap" type="java.util.LinkedHashMap">
    <c:set target="${delaysMap}" property="0"><mvc:message code="action.No_Delay"/></c:set>
    <c:set target="${delaysMap}" property="1">1&nbsp;<mvc:message code="workflow.deadline.Minute"/></c:set>
    <c:set target="${delaysMap}" property="5">5&nbsp;<mvc:message code="minutes"/></c:set>
    <c:set target="${delaysMap}" property="15">15&nbsp;<mvc:message code="minutes"/></c:set>
    <c:set target="${delaysMap}" property="30">30&nbsp;<mvc:message code="minutes"/></c:set>
    <c:set target="${delaysMap}" property="60">1&nbsp;<mvc:message code="Hour"/></c:set>
    <c:set target="${delaysMap}" property="360">6&nbsp;<mvc:message code="Hours"/></c:set>
    <c:set target="${delaysMap}" property="720">12&nbsp;<mvc:message code="Hours"/></c:set>
    <c:set target="${delaysMap}" property="1440">1&nbsp;<mvc:message code="Day"/></c:set>
    <c:set target="${delaysMap}" property="2880">2&nbsp;<mvc:message code="Days"/></c:set>
    <c:set target="${delaysMap}" property="5760">4&nbsp;<mvc:message code="Days"/></c:set>
    <c:set target="${delaysMap}" property="10080">7&nbsp;<mvc:message code="Days"/></c:set>
</emm:instantiate>

<script id="module-SendMailing" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <div class="form-group">
            <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="SendMailing"/>
            <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.mailingID"><mvc:message code="Mailing"/></label>
            </div>
            <div class="col-sm-8">
                <select name="modules[].mailingID" id="module_{{- index}}.mailingID" class="form-control js-select" size="1">
                    <option value="0"><mvc:message code="error.report.select_mailing"/></option>
                    <c:forEach items="${eventBasedMailings}" var="mailing">
                        {{ var selectedSign = '${mailing.id}' == mailingID ? 'selected="selected"' : ''; }}
                        <option value="${mailing.id}" {{- selectedSign}}>${mailing.shortname}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.delayMinutes"><mvc:message code="Delay"/></label>
            </div>
            <div class="col-sm-8">
                <select name="modules[].delayMinutes" id="module_{{- index}}.delayMinutes" class="form-control js-select" size="1">

                    <c:forEach var="delay" items="${delaysMap}">
                        {{ var selectedSign = ${delay.key} == delayMinutes ? 'selected="selected"' : ''; }}
                        <option value="${delay.key}" {{- selectedSign}}>${delay.value}</option>
                    </c:forEach>
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
        {{var visibilityClass = ${isForceSendingEnabled} ? 'hidden' : ''}}

        <div class="form-group {{- visibilityClass}}">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.forActiveRecipients">
                    <mvc:message code="action.send.doi.active"/>
                </label>
            </div>

            <div class="col-sm-8">
                <label class="toggle">
                    {{ if (forActiveRecipients) { }}
                        <input type="checkbox" name="modules[].forActiveRecipients" id="module_{{- index}}.forActiveRecipients" checked="checked">
                    {{ } else { }}
                        <input type="checkbox" name="modules[].forActiveRecipients" id="module_{{- index}}.forActiveRecipients">
                    {{ } }}
                    <div class="toggle-control"></div>
                </label>
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
