
<script id="module-ServiceMail" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="ServiceMail"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.toAddress"><mvc:message code="To"/></label>
            </div>
            <div class="col-sm-8">
                <input type="text" name="modules[].toAddress" id="module_{{- index}}.toAddress" class="form-control" value="{{- toAddress}}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.fromAddress"><mvc:message code="From"/></label>
            </div>
            <div class="col-sm-8">
                <input type="text" name="modules[].fromAddress" id="module_{{- index}}.fromAddress" class="form-control" value="{{- fromAddress}}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.replyAddress"><mvc:message code="mailing.ReplyEmail"/></label>
            </div>
            <div class="col-sm-8">
                <input type="text" name="modules[].replyAddress" id="module_{{- index}}.replyAddress" class="form-control" value="{{- replyAddress}}"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.subjectLine"><mvc:message code="mailing.Subject"/></label>
            </div>
            <div class="col-sm-8">
                <input type="text" name="modules[].subjectLine" id="module_{{- index}}.subjectLine" class="form-control" value="{{- subjectLine}}"/>
            </div>
        </div>
        <div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="module_{{- index}}.mailtype"><mvc:message code="action.Format"/></label>
                </div>
                <div class="col-sm-8">
                    <select name="modules[].mailtype" id="module_{{- index}}.mailtype" class="form-control" size="1">
                        <option value="0" {{ mailtype == 0 ? print('selected="selected"') : print('') }}><mvc:message code="Text"/></option>
                        <option value="1" {{ mailtype == 1 ? print('selected="selected"') : print('') }}><mvc:message code="mailing.Text_HTML"/></option>
                    </select>
                </div>
            </div>
            <div id="TextVersion_{{-index}}" class="inline-tile form-group" data-field="validator">
                <div class="inline-tile-header">
                    <h2 class="headline"><mvc:message code="Text_Version"/></h2>
                    <ul class="inline-tile-header-actions">
                        <li>
                            <a href="#" data-modal="action-modal-editor"
                               data-modal-set="title: <mvc:message code="Text_Version"/>,
                                                target: module_{{- index}}.textMail, id: textTemplateLarge, type: text"
                               data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                               <i class="icon icon-arrows-alt"></i>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="inline-tile-content">
                    <div class="row">
                        <div class="col-sm-12">
                            <textarea name="modules[].textMail" id="module_{{- index}}.textMail" rows="14" cols="70"
                                      data-field-validator="reject-script-element" class="form-control js-editor-text">{{- textMail}}</textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div id="HTMLVersion{{-index}}" class="inline-tile form-group" data-field="validator">
                <div class="inline-tile-header">
                    <h2 class="headline"><mvc:message code="mailing.HTML_Version"/></h2>
                    <ul class="inline-tile-header-actions">
                        <li>
                            <a href="#" data-modal="action-modal-editor"
                               data-modal-set="title: <mvc:message code="mailing.HTML_Version"/>,
                                                target: module_{{- index}}.htmlMail, id: htmlTemplateLarge"
                               data-tooltip="<mvc:message code='editor.enlargeEditor'/>">
                               <i class="icon icon-arrows-alt"></i>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="inline-tile-content">
                    <div class="row">
                        <div class="col-sm-12">
                            <textarea name="modules[].htmlMail" id="module_{{- index}}.htmlMail" rows="14" cols="70"
                                      data-field-validator="reject-script-element" class="form-control js-editor">{{- htmlMail}}</textarea>
                        </div>
                    </div>
                </div>
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
