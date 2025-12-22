AGN.Opt.Templates['select2-result'] = '<span>{{- title ? title : text }}</span>';

AGN.Opt.Templates['select2-badge-option'] = `
<div class="d-flex align-items-center gap-1">
    {{ if (value) { }}
        <span class="status-badge {{- element.getAttribute('data-badge-class')}}"></span>
    {{ } }}
    <span>{{- text }}</span>
</div>
`;

AGN.Opt.Templates['input-feedback'] = `
<div class="form-control-feedback-message">
    <i class="icon icon-state-{{- type === 'success' ? 'success' : 'warning' }}"></i>
    {{ print(message); }}
</div>
`;

AGN.Opt.Templates['datepicker-btn'] = `
<button id="{{- id }}" class="btn btn-secondary">
    <i class="icon {{- iconClass }}"></i>
    <span class="text">{{- text }}</span>
</button>
`;

AGN.Opt.Templates['error'] = `
   <div class="modal modal-alert" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        <i class="icon icon-state-warning"></i>
                        {{= headline }}
                    </h1>
  
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only">${t('defaults.cancel')}</span>
                    </button>
                </div>
  
                <div class="modal-body">
                    <p>{{= text }}</p>
                </div>
                
                <div class="modal-footer">
                   <button class="btn btn-primary" data-bs-dismiss="modal" onclick="location.reload();">
                        <i class="icon icon-redo"></i>
                        <span class="text">{{= reload }}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
`;

AGN.Opt.Templates['permission-denied'] = `
   <div class="modal modal-alert" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        <i class="icon icon-state-alert"></i>
                        {{= title }}
                    </h1>
                </div>

                <div class="modal-body">
                    <p>{{= text }}</p>
                </div>
                
                <div class="modal-footer">
                     <button class="btn btn-primary" data-bs-dismiss="modal">
                        <i class="icon icon-check"></i>
                        <span class="text">{{= btn }}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
`;

AGN.Opt.Templates['autosave-restore'] = `
  <div class="modal" tabIndex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h1 class="modal-title">{{= title }}</h1>
          <button type="button" class="btn-close" data-confirm-negative>
            <span class="sr-only">${t('defaults.cancel')}</span>
          </button>
        </div>
        <div class="modal-body">
          <p>{{= content }}</p>
        </div>
  
        <div class="modal-footer">
            <button type="button" class="btn btn-danger" data-confirm-negative>
              <i class="icon icon-times"></i>
              <span>${t('defaults.no')}</span>
            </button>
            <button type="button" class="btn btn-primary" data-confirm-positive>
                <i class="icon icon-check"></i>
                <span>${t('defaults.yes')}</span>
            </button>
        </div>
      </div>
    </div>
  </div>
`;

AGN.Opt.Templates['tooltip-template'] = `
  <div class="tooltip {{= tooltipStyle }}" role="tooltip">
    <div class="tooltip-arrow {{= arrowStyle }}"></div>
    <div class="tooltip-inner {{= innerStyle }}"></div>
  </div>
`;

AGN.Opt.Templates['table-footer'] = `
 {{ if (showRecordsCount === 'simple') { }}
    <div class="table-wrapper__footer"></div>
 {{ } else { }}
    <div class="table-wrapper__footer">
      {{ if (showRecordsCount) { }}
        <div class="table-wrapper__rows-selection">
          <select data-number-of-rows class="form-control compact" data-select-options="width: 'auto', dropdownAutoWidth: true">
            {{ _.each([20, 50, 100, 200], numberOfRows => { }}       
              <option value="{{- numberOfRows }}" {{- pageSize == numberOfRows ? 'selected="selected"' : ''}}>{{- numberOfRows }}</option>
            {{ }) }}
          </select>       
          <span>${t('defaults.rowsToDisplay')}</span>
        </div>
      {{ } }}
    
      {{ if (pagination && totalPages > 1) { }}
        <ul class="pagination">
          <li class="{{= currentPage == 1 ? 'disabled' : '' }}" data-js-table-page="first" role="button">
            <span><i class="icon icon-angle-double-left"></i></span>
          </li>
          <li class="{{= currentPage == 1 ? 'disabled' : '' }}" data-js-table-page="prev" role="button">
            <span><i class="icon icon-angle-left"></i></span>
          </li>
          {{ _.each(pageSelects, page => { }} 
            <li class="{{= page == currentPage ? 'active' : '' }}" data-js-table-page="{{= page - 1 }}" role="button">
              <span>{{= page }}</span>
            </li>
          {{ }) }}
          <li class="{{= currentPage == totalPages ? 'disabled' : '' }}" data-js-table-page="next" role="button">
            <span><i class="icon icon-angle-right"></i></span>
          </li>
          <li class="{{= currentPage == totalPages ? 'disabled' : '' }}" data-js-table-page="last" role="button">
            <span><i class="icon icon-angle-double-right"></i></span>
          </li>
        </ul>
      {{ } }}
    </div>
 {{ } }}
`;

AGN.Opt.Templates['js-table-wrapper'] = `
  <div class="table-wrapper">
    <div class="table-wrapper__header justify-content-end">
        <div class="table-wrapper__controls">
          <button type="button" class="btn" data-toggle-table-truncation>
            <i class="icon icon-ellipsis-h"></i>
            <span>${t('defaults.toggleTruncation')}</span>
          </button>
          
          <p class="table-wrapper__entries-label">
            <b></b>
            <span class="text-truncate">${t('defaults.entries')}</span>
          </p>
        </div>
    </div>
  </div>
`;

AGN.Opt.Templates['session-expired'] = `
<div class="modal modal-warning" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <i class="icon icon-state-warning"></i>
                    ${t('logon.session.expired')}
                </h1>
            </div>

            <div class="modal-body">
                <p>${t('logon.session.notification')}</p>
            </div>
            
            <div class="modal-footer">
               <button class="btn btn-primary" data-bs-dismiss="modal">
                  ${t('defaults.relogin')}
              </button>
            </div>
        </div>
    </div>
</div>
`;

AGN.Opt.Templates['datetime-picker'] = `
  <div class="date-time-container">
      <div class="date-picker-container">
        <input type="text" class="form-control js-datepicker" value="{{- date }}" placeholder="{{- window.adminDateFormat }}" {{- extraAttrs }} />
      </div>
      {{ var timeMask = timeMask ? timeMask : 'h:s' }}
      <div class="time-picker-container">
         <input type="text" class="form-control js-timepicker" value="{{- time }}" data-timepicker-options="mask: '{{- timeMask }}'" {{- extraAttrs }} />
      </div>
  </div>
`;

AGN.Opt.Templates['plus-btn'] = `
<a href="#" class="btn btn-icon btn-primary" {{= attrs }}>
    <i class="icon icon-plus"></i>
</a>`;

AGN.Opt.Templates['trash-btn'] = `
<a href="#" class="btn btn-icon btn-danger" {{= attrs }}>
    <i class="icon icon-trash-alt"></i>
</a>`;

AGN.Opt.Templates['notification-info'] = `
  <div class="notification-simple">
      <i class="icon icon-info-circle"></i>
      <span>{{- message }}</span>
  </div>
`;

AGN.Opt.Templates['progress'] = `
  {{ if (isNaN(currentProgress) || currentProgress === true) { }}
      <div class="progress loop" style="width: 100%"></div>
  {{ } else { }}
      <div class="progress">
          <div class="progress-bar"
                     role="progressbar"
                     aria-valuenow="{{= currentProgress }}"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     style="width: {{= currentProgress }}%"></div>
          <div class="percentage">{{= currentProgress }}%</div>
      </div>
  {{ } }}
`

AGN.Opt.Templates['tile-overlay'] = `
  <div class="tile-overlay tile-overlay--{{= state }}">
      {{ if (state === 'visible') { }}
        <button type="button" class="btn btn-lg btn-sm-horizontal tile-overlay__btn" data-edit-tile-visibility>
            <i class="icon icon-eye-slash"></i>
            ${t('editableView.tile.state.visible')}
        </button>
      {{ } else if (state === 'hidden') { }}
        <button type="button" class="btn btn-lg btn-sm-horizontal tile-overlay__btn" data-edit-tile-visibility>
            <i class="icon icon-eye"></i>
            ${t('editableView.tile.state.hidden')}
        </button>
      {{ } else { }}
        <button type="button" class="btn btn-lg btn-sm-horizontal tile-overlay__btn pe-none">
            <i class="icon icon-minus-circle"></i>
            ${t('editableView.tile.state.main')}
        </button>
      {{ } }}
  </div>
`;

AGN.Opt.Templates['deletable-table-column'] = `
  <div class="d-flex gap-1 align-items-center hidden">
    {{ if (!permanent) { }}
      <button type="button" class="icon-btn icon-btn--danger" data-remove-table-column>
        <i class="icon icon-times-circle"></i>
      </button>
    {{ } }}
    <span class="text-truncate">{{= text }}</span>
  </div>
`;

AGN.Opt.Templates['table-column-picker'] = `
  <div class="dropdown dropstart hidden">
      <button type="button" class="icon-btn icon-btn--primary" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
        <i class="icon icon-plus-circle"></i>
      </button>
      <ul class="dropdown-menu dropdown-menu--table-column mt-4">
          <select class="form-control" multiple data-select-options="preventPlaceholderClear: true, placeholder: '${t('tables.searchOoo')}'">
            {{ _.each(columns, col => { }}
              <option value="{{- col.name }}" {{ col.selected ? print('selected') : print('') }}>
                {{- col.text }}
              </option>
            {{ }); }}
          </select>
      </ul>
  </div>
`;

AGN.Opt.Templates['multi-editor-modal'] = `
<div class="modal modal-adaptive modal-editor">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">{{= title }}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only">${t('defaults.cancel')}</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="modal-editors-container">
                    <div data-placeholder></div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn btn-primary" data-apply-enlarged>
                    <i class="icon icon-save"></i>
                    <span class="text">{{- btnText }}</span>
                </a>
            </div>
        </div>
    </div>
</div>
`;

AGN.Opt.Templates['mailing-locked'] = `
<div class="modal modal-warning" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <i class="icon icon-state-warning"></i>
                    {{= t(\'defaults.warning\') }}
                </h1>
                
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                  <span class="sr-only">${t('defaults.cancel')}</span>
                </button>
            </div>

            <div class="modal-body">
                <p>{{= t(\'error.mailing.exclusiveLockingFailed\', username) }}</p>
            </div>
        </div>
    </div>
</div>
`;

AGN.Opt.Templates['double-select-options'] = `
{{ _.forEach(opts, (name, value) => { }}
   {{ let isSelected = Array.isArray(valueSelected) ? valueSelected.includes(value) : value === valueSelected; }}
    <option value="{{- value }}" {{ isSelected ? print('selected') : print('') }}>
      {{- name }}
    </option>
{{ }); }}
`;

AGN.Opt.Templates['processing-loader'] = `
<div class="tile tile--notification tile--processing">
  <div class="tile-header">
    <h1 class="tile-title">
      <i class="icon icon-sync icon-pulse"></i>
      {{- t('statistic.waitProcessing') }}
    </h1>
  </div>
  <div class="tile-body">
    <a type="button" class="btn btn-light w-100" data-form-abort data-form-target="{{- targetForm }}">
      {{- t('statistic.cancelProcessing') }}
    </a>
  </div>
</div>
`;
