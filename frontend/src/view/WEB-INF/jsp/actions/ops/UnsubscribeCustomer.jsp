
<script id="module-UnsubscribeCustomer" type="text/x-mustache-template">

    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" value="UnsubscribeCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

        <c:if test="${isUnsubscribeExtended}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="additionalMailinglistsToggle" class="control-label checkbox-control-label">
                        <mvc:message code="action.unsubscribe.additionally"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        {{var additionalListsCheckedAttr = mailinglistIds.length > 0 || allMailinglistsSelected ? 'checked' : '';}}
                        <input {{- additionalListsCheckedAttr}} name="modules[].additionalMailinglists" type="checkbox" id="additionalMailinglistsToggle"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div data-show-by-checkbox="#additionalMailinglistsToggle">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="allMailinglistsToggle" class="control-label checkbox-control-label">
                            <mvc:message code="recipient.AllMailinglists"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            {{var allMailinglistsChekcedAttr = allMailinglistsSelected ? 'checked' : '';}}
                            <input type="checkbox" {{- allMailinglistsChekcedAttr}} name="modules[].allMailinglistsSelected" id="allMailinglistsToggle"/>
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>
                <div class="form-group">
                    <div data-hide-by-checkbox="#allMailinglistsToggle">
                        <div class="col-sm-4">
                            <label class="control-label" for="id{{- index}}"><mvc:message code="Mailinglists"/></label>
                        </div>
                        <div class="col-sm-8">
                            <select name="modules[].mailinglistIds" multiple id="id{{- index}}" class="form-control js-select">       
                                <c:forEach var="mailinglist" items="${allowedMailinglists}">
                                    {{var selectedOptionAttr = mailinglistIds.includes(${mailinglist.id}) ? 'selected="selected"' : '';}}
                                    <option value="${mailinglist.id}" {{- selectedOptionAttr}} >${mailinglist.shortname}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
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
