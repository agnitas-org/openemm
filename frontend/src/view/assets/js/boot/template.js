AGN.Opt.Templates['modal'] = '\
<div class="modal"> \
  <div class="modal-dialog {{= modalClass }}"> \
    <div class="modal-content"> \
      <div class="modal-header"> \
        <button type="button" class="close-icon close" data-dismiss="modal"> \
          <i aria-hidden="true" class="icon icon-times-circle"></i> \
        </button> \
        <h4 class="modal-title">{{= title }}</h4> \
      </div> \
      <div class="modal-body"> \
        {{= content }} \
      </div> \
    </div> \
  </div> \
</div>';

AGN.Opt.Templates['csrf-error'] = '\
    <div class="backdrop backdrop-error js-close-error" style="position: fixed; top: 0; left: 0; bottom: 0; right:0; z-index: 1100; background-color: rgba(0,0,0,0.5)">\
        <div class="notification notification-alert" style="position: fixed; top: 50%; left: 50%; width: 420px; margin: -80px 0 0 -210px; z-index: 1101;">\
            <div class="notification-header">\
                <p class="headline">\
                    <i class="icon icon-state-alert"></i>\
                    <span class="text">{{= headline }}</span>\
                    <i class="icon icon-times-circle close-icon js-close-error"></i>\
                </p>\
            </div>\
            <div class="notification-content">\
                <p>{{= content }}</p>\
                <a href="#" class="btn btn-regular btn-primary vspace-top-10" onclick="location.reload();">\
                    <i class="icon icon-repeat"></i>\
                    <span class="text">{{= reload }}</span>\
                </a>\
            </div>\
        </div>\
    </div>\
';

AGN.Opt.Templates['modal-yes-no-cancel'] = '\
<div class="modal"> \
  <div class="modal-dialog"> \
    <div class="modal-content"> \
      <form action="{{= action }}" method="{{= method }}"> \
        <div class="modal-header"> \
          <button type="button" class="close-icon close" data-dismiss="modal"> \
            <i aria-hidden="true" class="icon icon-times-circle"></i> \
          </button> \
          <h4 class="modal-title">{{= title }}</h4> \
        </div> \
        <div class="modal-body"> \
          {{= content }} \
        </div> \
        <div class="modal-footer"> \
          <div class="btn-group"> \
            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal"> \
              <i class="icon icon-times"></i> \
              <span class="text">{{= t(\'defaults.no\') }}</span> \
            </button> \
            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal"> \
              <i class="icon icon-check"></i> \
              <span class="text">{{= t(\'defaults.yes\') }}</span> \
            </button> \
          </div> \
        </div> \
      </form> \
    </div> \
  </div> \
</div>';

AGN.Opt.Templates['modal-yes-no-cancel-save-choice'] = '\
<div class="modal"> \
  <div class="modal-dialog {{= modalClass }}"> \
    <div class="modal-content"> \
      <div class="modal-header"> \
        <button type="button" class="close-icon close" data-dismiss="modal"> \
          <i aria-hidden="true" class="icon icon-times-circle"></i> \
        </button> \
        <h4 class="modal-title">{{= title }}</h4> \
      </div> \
      <div class="modal-body"> \
        <div class="form-group"> \
          <div class="col-sm-12"> \
          {{= content }} \
          </div> \
        </div> \
        <div class="form-group"> \
          <div class="col-sm-8"> \
            <label class="control-label-left"> \
              {{ if(!choiceContent) { }} \
                {{= t(\'defaults.remember.choice\') }} \
              {{ } else { }} \
                {{= choiceContent }} \
              {{ } }} \
            </label> \
          </div> \
          <div class="col-sm-4"> \
            <label class="toggle"> \
              <input type="checkbox" name="confirm-save-choice"> \
              <div class="toggle-control"></div> \
            </label> \
          </div> \
        </div> \
      </div> \
      <div class="modal-footer"> \
        <div class="btn-group"> \
          <button type="button" class="btn btn-default btn-large pull-left" data-confirm-negative="cancel" data-dismiss="modal"> \
            <i class="icon icon-times"></i> \
            <span class="text">{{= t(\'defaults.cancel\') }}</span> \
          </button> \
          <button type="button" class="btn btn-default btn-large" data-confirm-negative="no" data-dismiss="modal"> \
            <i class="icon icon-times"></i> \
            <span class="text">{{= t(\'defaults.no\') }}</span> \
          </button> \
          <button type="button" class="btn btn-primary btn-large" data-confirm-positive="yes" data-dismiss="modal"> \
            <i class="icon icon-check"></i> \
            <span class="text">{{= t(\'defaults.yes\') }}</span> \
          </button> \
        </div> \
      </div> \
    </div> \
  </div> \
</div>';

AGN.Opt.Templates['error'] = '\
<div class="backdrop backdrop-error js-close-error overlay-box"> \
    <div class="notification notification-alert overlay-content"> \
        <div class="notification-header"> \
            <p class="headline"> \
                <i class="icon icon-state-alert"></i> \
                <span class="text">{{= headline }}</span> \
                <i class="icon icon-times-circle close-icon js-close-error"></i> \
            </p> \
        </div> \
        <div class="notification-content"> \
            <p>{{= text }}</p> \
            <button type="button" class="btn btn-regular btn-primary vspace-top-10" onclick="location.reload();"> \
                <i class="icon icon-repeat"></i> \
                <span class="text">{{= reload }}</span> \
            </button> \
        </div> \
    </div> \
</div>';

AGN.Opt.Templates['permission-denied'] = '\
<div class="backdrop backdrop-error js-close-error overlay-box"> \
    <div class="notification notification-alert overlay-content"> \
        <div class="notification-header"> \
            <p class="headline"> \
                <i class="icon icon-state-alert"></i> \
                <span class="text">{{= title }}</span> \
                <i class="icon icon-times-circle close-icon js-close-error"></i> \
            </p> \
        </div> \
        <div class="notification-content"> \
            <p>{{= text }}</p> \
            <button type="button" class="btn btn-regular btn-primary vspace-top-10 vspace-bottom-10 js-close-error" style="float: right"> \
                <i class="icon icon-check"></i> \
                <span class="text">{{= btn }}</span> \
            </button> \
        </div> \
    </div> \
</div>';

AGN.Opt.Templates['autosave-restore'] = '\
<div class="modal"> \
  <div class="modal-dialog {{= modalClass }}"> \
    <div class="modal-content"> \
      <div class="modal-header"> \
        <button type="button" class="close-icon close" data-dismiss="modal"> \
          <i aria-hidden="true" class="icon icon-times-circle"></i> \
        </button> \
        <h4 class="modal-title">{{= title }}</h4> \
      </div> \
      <div class="modal-body"> \
        {{= content }} \
      </div> \
      <div class="modal-footer"> \
        <div class="btn-group"> \
          <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal"> \
            <i class="icon icon-times"></i> \
            <span class="text">{{= negative }}</span> \
          </button> \
          <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal"> \
            <i class="icon icon-check"></i> \
            <span class="text">{{= positive }}</span> \
          </button> \
        </div> \
      </div> \
    </div> \
  </div> \
</div>';


AGN.Opt.Templates['tooltip-template'] = '<div class="tooltip {{= tooltipStyle }}" role="tooltip"> \
  <div class="tooltip-arrow {{= arrowStyle }}"></div> \
  <div class="tooltip-inner {{= innerStyle }}"></div> \
</div>';


AGN.Opt.Templates['tooltip-message-with-title'] = '\
<div class="helper-popup-header">{{= title }}</div> \
<div class="helper-popup-content">{{= content }}</div>';

AGN.Opt.Templates['tooltip-message-just-content'] = '<div class="helper-popup-content">{{= content }}</div>';

AGN.Opt.Templates['table-controls-top'] = '\
{{ if (showRecordsCount || pagination) { }} \
<div class="table-controls clearfix"> \
  {{ if (showRecordsCount) { }} \
  <div class="table-control pull-left"> \
    <div class="well">{{= t(\'tables.range\', itemStart, itemEnd, itemTotal) }}</div> \
  </div> \
  {{ } }}\
  <div class="table-control pull-left" id="filtersDescription"></div> \
  {{ if (pagination) { }} \
  <div class="table-control pull-right"> \
    <ul class="pagination"> \
      <li class="js-data-table-first-page {{= currentPage == 1 ? \'disabled\' : \'\' }}"> \
        <span><i class="icon icon-angle-double-left"></i> {{= t(\'tables.first\') }}</span> \
      </li> \
      <li class="js-data-table-prev-page {{= currentPage == 1 ? \'disabled\' : \'\' }}"> \
        <span><i class="icon icon-angle-left"></i> {{= t(\'tables.previous\') }}</span> \
      </li> \
      {{ _.each(pageSelects, function(page) { }} \
      <li class="{{= page == currentPage ? \'active\' : \'js-data-table-page\' }}" data-page="{{= page - 1 }}"> \
        <span>{{= page }}</span> \
      </li> \
      {{ }) }} \
      <li class="js-data-table-next-page {{= currentPage == totalPages ? \'disabled\' : \'\' }}"> \
        <span>{{= t(\'tables.next\') }} <i class="icon icon-angle-right"></i></span> \
      </li> \
      <li class="js-data-table-last-page {{= currentPage == totalPages ? \'disabled\' : \'\' }}"> \
        <span>{{= t(\'tables.last\') }} <i class="icon icon-angle-double-right"></i></span> \
      </li> \
    </ul> \
  </div> \
  {{ } }}  \
</div>\
{{ } }}';

AGN.Opt.Templates['table-controls-bottom'] = '\
<div class="table-controls clearfix"> \
  {{ if (showRecordsCount) { }} \
  <div class="table-control pull-left"> \
    <div class="well">{{= t(\'tables.range\', itemStart, itemEnd, itemTotal) }}</div> \
  </div> \
  {{ } }} \
  {{ if (pagination) { }} \
  <div class="table-control pull-right"> \
    <ul class="pagination"> \
      <li class="js-data-table-first-page {{= currentPage == 1 ? \'disabled\' : \'\' }}"> \
        <span><i class="icon icon-angle-double-left"></i> {{= t(\'tables.first\') }}</span> \
      </li> \
      <li class="js-data-table-prev-page {{= currentPage == 1 ? \'disabled\' : \'\' }}"> \
        <span><i class="icon icon-angle-left"></i> {{= t(\'tables.previous\') }}</span> \
      </li> \
      {{ _.each(pageSelects, function(page) { }}  \
      <li class="{{= page == currentPage ? \'active\' : \'js-data-table-page\' }}" data-page="{{= page - 1 }}"> \
        <span>{{= page }}</span> \
      </li> \
      {{ }) }} \
      <li class="js-data-table-next-page {{= currentPage == totalPages ? \'disabled\' : \'\' }}"> \
        <span>{{= t(\'tables.next\') }} <i class="icon icon-angle-right"></i></span> \
      </li> \
      <li class="js-data-table-last-page {{= currentPage == totalPages ? \'disabled\' : \'\' }}"> \
        <span>{{= t(\'tables.last\') }} <i class="icon icon-angle-double-right"></i></span> \
      </li> \
    </ul> \
  </div> \
  {{ } }} \
</div>';

AGN.Opt.Templates['session-expired'] = '\
<div class="backdrop backdrop-warning overlay-box" id="session-expired-overlay"> \
    <div class="notification notification-warning overlay-content"> \
        <div class="notification-header"> \
            <p class="headline"> \
                <i class="icon icon-state-alert"></i> \
                <span class="text">{{= t(\'logon.session.expired\') }}</span> \
            </p> \
        </div> \
        <div class="notification-content"> \
            <div class="form-group"> \
              {{= t(\'logon.session.notification\') }} \
            </div>\
            <div class="form-group"> \
              <a href="{{= AGN.url("/logonOld.action", true) }}"> \
                {{= t(\'defaults.relogin\') }} \
              </a> \
            </div>\
        </div> \
    </div> \
</div>';

AGN.Opt.Templates['datetime-picker'] = '\
<div class="row">\
  <div class="col-sm-8"> \
    <div class="input-group"> \
      <div class="input-group-controls"> \
        <input type="text" id="{{- property }}_date" class="form-control datepicker-input js-datepicker" {{- extraAttrs }} \
          data-value="{{- date}}" data-datepicker-options="format: {{- dateFormat}}"/> \
      </div> \
      <div class="input-group-btn"> \
        <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1"> \
          <i class="icon icon-calendar-o"></i> \
        </button> \
      </div> \
    </div> \
  </div> \
  <div class="col-sm-4"> \
    <div class="input-group"> \
      <div class="input-group-controls"> \
        <input type="text" id="{{- property }}_time" class="form-control js-timepicker" value="{{- time}}" {{- extraAttrs }} \
             data-timepicker-options=\"mask: \'h:s\'\"/> \
      </div> \
      <div class="input-group-addon"> \
        <span class="addon"><i class="icon icon-clock-o"></i></span> \
      </div> \
    </div> \
  </div> \
</div>';

AGN.Opt.Templates['trackablelink-extension-table-row'] = '\
<tr data-extension-row="{{- index}}"> \
    <td> \
        <input type="text" class="form-control" data-extension-name value="{{- name}}"/> \
    </td> \
    <td> \
        <input type="text" class="form-control" data-extension-value value="{{- value}}"/> \
    </td> \
    <td class="table-actions"> \
        {{ if (name == "" && value == "") { }} \
            <a href="#" class="btn btn-regular btn-primary" data-extension-add id="newExtensionBtn"> \
                <i class="icon icon-plus"></i> \
            </a> \
        {{ } else { }} \
            <a href="#" class="btn btn-regular btn-alert" data-extension-delete> \
                <i class="icon icon-trash-o"></i> \
            </a> \
        {{ } }} \
    </td> \
</tr>';

AGN.Opt.Templates['mailing-param-row'] = '\
  <tr data-param-row="{{- index}}"> \
      <td> \
          <input type="text" value="{{- name}}" data-param-name class="form-control" data-action="param-enterdown" {{ isChangeable ? print("") : print("readonly") }} /> \
      </td> \
      <td> \
          <input type="text" value="{{- value}}" data-param-value class="form-control" data-action="param-enterdown" {{ isChangeable ? print("") : print("readonly") }} /> \
      </td> \
      <td> \
          <input type="text" value="{{- description}}" data-param-description class="form-control" data-action="param-enterdown" {{ isChangeable ? print("") : print("readonly") }} /> \
      </td> \
      {{ if (isChangeable) { }} \
      <td class="table-actions"> \
        {{ if (name == "" && value == "") { }} \
            <a href="#" class="btn btn-regular btn-primary" data-action="add-param-row" id="newParamBtn"> \
                <i class="icon icon-plus"></i> \
            </a> \
        {{ } else { }} \
            <a href="#" class="btn btn-regular btn-alert" data-action="delete-param-row"> \
                <i class="icon icon-trash-o"></i> \
            </a> \
        {{ } }} \
      </td> \
      {{ } }} \
  </tr>';

AGN.Opt.Templates['mailing-reference-content-item'] = ' \
  <div class="form-group"> \
      <div class="col-sm-4"> \
          <label class="control-label" for="input-{{- name }}">{{- name }}</label> \
      </div> \
      <div class="col-sm-8"> \
          <div class="input-group-controls"> \
              <input type="text" class="form-control" name="referenceContentSettings.items" value="{{- value }}" {{ disable ? print("disabled") : print("")}} id="input-{{- name }}"/> \
          </div> \
          <div class="input-group-btn"> \
              <button type="button" class="btn btn-regular btn-alert" {{ disable ? print("disabled") : print("")}} \
                      data-action="delete-reference-content-item"> \
                  <i class="icon icon-trash-o"></i> \
              </button> \
          </div> \
      </div> \
  </div>';
