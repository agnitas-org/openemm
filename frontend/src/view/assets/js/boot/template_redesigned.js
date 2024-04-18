AGN.Opt.Templates['select2-result'] = '<span>{{- title ? title : text }}</span>'

// AGN.Opt.Templates['modal'] = '\
// <div class="modal"> \
//   <div class="modal-dialog {{= modalClass }}"> \
//     <div class="modal-content"> \
//       <div class="modal-header"> \
//         <button type="button" class="close-icon close" data-dismiss="modal"> \
//           <i aria-hidden="true" class="icon icon-times-circle"></i> \
//         </button> \
//         <h4 class="modal-title">{{= title }}</h4> \
//       </div> \
//       <div class="modal-body"> \
//         {{= content }} \
//       </div> \
//     </div> \
//   </div> \
// </div>';
//
// AGN.Opt.Templates['modal-yes-no-cancel'] = '\
// <div class="modal"> \
//   <div class="modal-dialog"> \
//     <div class="modal-content"> \
//       <form action="{{= action }}" method="{{= method }}"> \
//         <div class="modal-header"> \
//           <button type="button" class="close-icon close" data-dismiss="modal"> \
//             <i aria-hidden="true" class="icon icon-times-circle"></i> \
//           </button> \
//           <h4 class="modal-title">{{= title }}</h4> \
//         </div> \
//         <div class="modal-body"> \
//           {{= content }} \
//         </div> \
//         <div class="modal-footer"> \
//           <div class="btn-group"> \
//             <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal"> \
//               <i class="icon icon-times"></i> \
//               <span class="text">{{= t(\'defaults.no\') }}</span> \
//             </button> \
//             <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal"> \
//               <i class="icon icon-check"></i> \
//               <span class="text">{{= t(\'defaults.yes\') }}</span> \
//             </button> \
//           </div> \
//         </div> \
//       </form> \
//     </div> \
//   </div> \
// </div>';
//
// AGN.Opt.Templates['modal-yes-no-cancel-save-choice'] = '\
// <div class="modal"> \
//   <div class="modal-dialog {{= modalClass }}"> \
//     <div class="modal-content"> \
//       <div class="modal-header"> \
//         <button type="button" class="close-icon close" data-dismiss="modal"> \
//           <i aria-hidden="true" class="icon icon-times-circle"></i> \
//         </button> \
//         <h4 class="modal-title">{{= title }}</h4> \
//       </div> \
//       <div class="modal-body"> \
//         <div class="form-group"> \
//           <div class="col-sm-12"> \
//           {{= content }} \
//           </div> \
//         </div> \
//         <div class="form-group"> \
//           <div class="col-sm-8"> \
//             <label class="control-label-left"> \
//               {{ if(!choiceContent) { }} \
//                 {{= t(\'defaults.remember.choice\') }} \
//               {{ } else { }} \
//                 {{= choiceContent }} \
//               {{ } }} \
//             </label> \
//           </div> \
//           <div class="col-sm-4"> \
//             <label class="toggle"> \
//               <input type="checkbox" name="confirm-save-choice"> \
//               <div class="toggle-control"></div> \
//             </label> \
//           </div> \
//         </div> \
//       </div> \
//       <div class="modal-footer"> \
//         <div class="btn-group"> \
//           <button type="button" class="btn btn-default btn-large pull-left" data-confirm-negative="cancel" data-dismiss="modal"> \
//             <i class="icon icon-times"></i> \
//             <span class="text">{{= t(\'defaults.cancel\') }}</span> \
//           </button> \
//           <button type="button" class="btn btn-default btn-large" data-confirm-negative="no" data-dismiss="modal"> \
//             <i class="icon icon-times"></i> \
//             <span class="text">{{= t(\'defaults.no\') }}</span> \
//           </button> \
//           <button type="button" class="btn btn-primary btn-large" data-confirm-positive="yes" data-dismiss="modal"> \
//             <i class="icon icon-check"></i> \
//             <span class="text">{{= t(\'defaults.yes\') }}</span> \
//           </button> \
//         </div> \
//       </div> \
//     </div> \
//   </div> \
// </div>';

AGN.Opt.Templates['error'] = `
   <div class="modal modal-alert" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        <i class="icon icon-state-warning"></i>
                        {{= headline }}
                    </h1>
  
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
  
                <div class="modal-body">
                    <p>{{= text }}</p>
                </div>
                
                <div class="modal-footer">
                   <button class="btn btn-primary flex-grow-1" data-bs-dismiss="modal" onclick="location.reload();">
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
        <div class="modal-dialog modal-fullscreen-lg-down modal-dialog-centered">
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
                     <button class="btn btn-primary flex-grow-1" data-bs-dismiss="modal">
                        <i class="icon icon-check"></i>
                        <span class="text">{{= btn }}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
`;

AGN.Opt.Templates['autosave-restore'] = `
  <div class="modal" tabIndex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h1 class="modal-title">{{= title }}</h1>
          <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
          </button>
        </div>
        <div class="modal-body">
          <p>{{= content }}</p>
        </div>
  
        <div class="modal-footer">
            <button type="button" class="btn btn-danger js-confirm-negative flex-grow-1" data-bs-dismiss="modal">
              <i class="icon icon-times"></i>
              <span class="text">{{= negative }}</span>
            </button>
            <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive" data-bs-dismiss="modal">
                <i class="icon icon-check"></i>
                <span class="text">{{= positive }}</span>
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


// AGN.Opt.Templates['tooltip-message-with-title'] = '\
// <div class="helper-popup-header">{{= title }}</div> \
// <div class="helper-popup-content">{{= content }}</div>';
//
// AGN.Opt.Templates['tooltip-message-just-content'] = '<div class="helper-popup-content">{{= content }}</div>';

AGN.Opt.Templates['table-controls'] = `
<div class="table-controls pages-{{- totalPages }}">
  {{ if (showRecordsCount === 'simple') { }}
    <span>{{- itemTotal }} {{= t('defaults.entries') }}</span>    
  {{ } else if (showRecordsCount) { }}
    <div class="table-controls__entries-wrapper">
      <span>${t('defaults.listShowMax')}</span>
        <select name="numberOfRows" class="form-control compact" data-select-options="minimumResultsForSearch: -1, width: 'auto', dropdownAutoWidth: true">
          {{ _.each([20, 50, 100], function(numberOfRows) { }}       
            <option value="{{- numberOfRows }}" {{- pageSize == numberOfRows ? 'selected="selected"' : ''}}>{{- numberOfRows }} ${t('defaults.entries')}</option>
          {{ }) }}
        </select>       
      <span>${t('defaults.OutOf')} {{- itemTotal }} ${t('defaults.entries')}</span>
    </div>
  {{ } }}

  {{ if (pagination && totalPages > 1) { }}
    <ul class="pagination">
      <li class="js-data-table-first-page {{= currentPage == 1 ? 'disabled' : '' }}">
        <span><i class="icon icon-fast-backward"></i></span>
      </li>
      <li class="js-data-table-prev-page {{= currentPage == 1 ? 'disabled' : '' }}">
        <span><i class="icon icon-step-backward"></i></span>
      </li>
      {{ _.each(pageSelects, function(page) { }} 
      <li class="{{= page == currentPage ? 'active' : 'js-data-table-page' }}" data-page="{{= page - 1 }}">
        <span>{{= page }}</span>
      </li>
      {{ }) }}
      <li class="js-data-table-next-page {{= currentPage == totalPages ? 'disabled' : '' }}">
        <span><i class="icon icon-step-forward"></i></span>
      </li>
      <li class="js-data-table-last-page {{= currentPage == totalPages ? 'disabled' : '' }}">
        <span><i class="icon icon-fast-forward"></i></span>
      </li>
    </ul>
  {{ } }}
</div>`

AGN.Opt.Templates['session-expired'] = `
<div class="modal modal-warning" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <i class="icon icon-state-warning"></i>
                    {{= t(\'logon.session.expired\') }}
                </h1>
            </div>

            <div class="modal-body">
                <p>{{= t(\'logon.session.notification\') }}</p>
            </div>
            
            <div class="modal-footer">
               <button class="btn btn-primary flex-grow-1" data-bs-dismiss="modal">
                  {{= t(\'defaults.relogin\') }}
              </button>
            </div>
        </div>
    </div>
</div>
`;

AGN.Opt.Templates['datetime-picker'] = `
  <div class="date-time-container">
      <div class="date-picker-container">
        <input type="text" id="{{- property }}_date" class="form-control js-datepicker" value="{{- date}}"/>
      </div>
     <div class="time-picker-container">
         <input type="text" id="{{- property }}_time" class="form-control js-timepicker" value="{{- time}}" data-timepicker-options="mask: 'h:s'"/>
      </div>
  </div>
`;

AGN.Opt.Templates['plus-btn'] = `
<a href="#" class="btn btn-icon-sm btn-primary" {{= attrs }}>
    <i class="icon icon-plus"></i>
</a>`;

AGN.Opt.Templates['trash-btn'] = `
<a href="#" class="btn btn-icon-sm btn-danger" {{= attrs }}>
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
          <div class="progress-bar-white-bg"></div>
          <div class="progress-bar"
                     role="progressbar"
                     aria-valuenow="{{= currentProgress }}"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     style="width: {{= currentProgress }}%"></div>
          <div class="progress-bar-primary-bg"></div>
          <div class="percentage">{{= currentProgress }}%</div>
      </div>
  {{ } }}
`

AGN.Opt.Templates['tile-overlay'] = `
  <div class="tile-overlay tile-overlay--{{= state }}">
      {{ if (state === 'visible') { }}
        <button type="button" class="btn btn-danger btn-lg btn-sm-horizontal" data-edit-tile-visibility>
            <i class="icon icon-eye-slash"></i>
            ${t('editableView.tile.state.visible')}
        </button>
      {{ } else if (state === 'hidden') { }}
        <button type="button" class="btn btn-primary btn-lg btn-sm-horizontal" data-edit-tile-visibility>
            <i class="icon icon-eye"></i>
            ${t('editableView.tile.state.hidden')}
        </button>
      {{ } else { }}
        <button type="button" class="btn btn-dark btn-lg btn-sm-horizontal pe-none">
            <i class="icon icon-minus-circle"></i>
            ${t('editableView.tile.state.main')}
        </button>
      {{ } }}
  </div>
`;

AGN.Opt.Templates['multi-editor-modal'] = `
<div class="modal modal-adaptive modal-editor">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">{{= title }}</h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only">${t('defaults.cancel')}</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="modal-editors-container">
                    <div data-placeholder></div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn btn-primary flex-grow-1" data-apply-enlarged>
                    <i class="icon icon-save"></i>
                    <span class="text">${t('defaults.apply')}</span>
                </a>
            </div>
        </div>
    </div>
</div>
`;

AGN.Opt.Templates['mailing-locked'] = `
<div class="modal modal-warning" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <i class="icon icon-state-warning"></i>
                    {{= t(\'defaults.warning\') }}
                </h1>
                
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                  <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <p>{{= t(\'error.mailing.exclusiveLockingFailed\', username) }}</p>
            </div>
        </div>
    </div>
</div>
`;
