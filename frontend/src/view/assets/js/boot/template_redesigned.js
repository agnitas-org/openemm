AGN.Opt.Templates['select2-result'] = '{{- title ? title : text }}'

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
//
// AGN.Opt.Templates['error'] = '\
// <div class="backdrop backdrop-error js-close-error overlay-box"> \
//     <div class="notification notification-alert overlay-content"> \
//         <div class="notification-header"> \
//             <p class="headline"> \
//                 <i class="icon icon-state-alert"></i> \
//                 <span class="text">{{= headline }}</span> \
//                 <i class="icon icon-times-circle close-icon js-close-error"></i> \
//             </p> \
//         </div> \
//         <div class="notification-content"> \
//             <p>{{= text }}</p> \
//             <button type="button" class="btn btn-regular btn-primary vspace-top-10" onclick="location.reload();"> \
//                 <i class="icon icon-repeat"></i> \
//                 <span class="text">{{= reload }}</span> \
//             </button> \
//         </div> \
//     </div> \
// </div>';
//
// AGN.Opt.Templates['permission-denied'] = '\
// <div class="backdrop backdrop-error js-close-error overlay-box"> \
//     <div class="notification notification-alert overlay-content"> \
//         <div class="notification-header"> \
//             <p class="headline"> \
//                 <i class="icon icon-state-alert"></i> \
//                 <span class="text">{{= title }}</span> \
//                 <i class="icon icon-times-circle close-icon js-close-error"></i> \
//             </p> \
//         </div> \
//         <div class="notification-content"> \
//             <p>{{= text }}</p> \
//             <button type="button" class="btn btn-regular btn-primary vspace-top-10 vspace-bottom-10 js-close-error" style="float: right"> \
//                 <i class="icon icon-check"></i> \
//                 <span class="text">{{= btn }}</span> \
//             </button> \
//         </div> \
//     </div> \
// </div>';
//
// AGN.Opt.Templates['autosave-restore'] = '\
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
//       <div class="modal-footer"> \
//         <div class="btn-group"> \
//           <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal"> \
//             <i class="icon icon-times"></i> \
//             <span class="text">{{= negative }}</span> \
//           </button> \
//           <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal"> \
//             <i class="icon icon-check"></i> \
//             <span class="text">{{= positive }}</span> \
//           </button> \
//         </div> \
//       </div> \
//     </div> \
//   </div> \
// </div>';


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
<div class="table-controls">
  {{ if (showRecordsCount) { }}
    <span>{{- itemTotal }} {{= t('defaults.entries') }}</span>
  {{ } }}
  {{ if (pagination) { }}
    <ul class="pagination">
      <li class="js-data-table-first-page {{= currentPage == 1 ? 'disabled' : '' }}">
        <span><i class="icon icon-angle-double-left"></i> {{= t('tables.first') }}</span>
      </li>
      <li class="js-data-table-prev-page {{= currentPage == 1 ? 'disabled' : '' }}">
        <span><i class="icon icon-angle-left"></i> {{= t('tables.previous') }}</span>
      </li>
      {{ _.each(pageSelects, function(page) { }} 
      <li class="{{= page == currentPage ? 'active' : 'js-data-table-page' }}" data-page="{{= page - 1 }}">
        <span>{{= page }}</span>
      </li>
      {{ }) }}
      <li class="js-data-table-next-page {{= currentPage == totalPages ? 'disabled' : '' }}">
        <span>{{= t('tables.next') }} <i class="icon icon-angle-right"></i></span>
      </li>
      <li class="js-data-table-last-page {{= currentPage == totalPages ? 'disabled' : '' }}">
        <span>{{= t('tables.last') }} <i class="icon icon-angle-double-right"></i></span>
      </li>
    </ul>
  {{ } }}
</div>
`

AGN.Opt.Templates['session-expired'] = `
<div class="modal modal-warning" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <span>
                    <i class="icon icon-state-warning"></i>
                    {{= t(\'logon.session.expired\') }}
                </span>
                
                 <button type="button" class="btn-close shadow-none hidden" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                
                <i class="icon icon-times-circle modal__close-icon" data-bs-dismiss="modal"></i>
            </div>

            <div class="modal-body pt-3">
                <span>{{= t(\'logon.session.notification\') }}</span>

                <div class="row mt-3">
                    <div class="col d-flex">
                        <button class="btn btn-lg btn-primary flex-grow-1" data-bs-dismiss="modal">
                            {{= t(\'defaults.relogin\') }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
`;

// AGN.Opt.Templates['datetime-picker'] = '\
// <div class="row">\
//   <div class="col-sm-8"> \
//     <div class="input-group"> \
//       <div class="input-group-controls"> \
//         <input type="text" id="{{- property }}_date" class="form-control datepicker-input js-datepicker" \
//           data-value="{{- date}}" data-datepicker-options="format: {{- dateFormat}}"/> \
//       </div> \
//       <div class="input-group-btn"> \
//         <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1"> \
//           <i class="icon icon-calendar-o"></i> \
//         </button> \
//       </div> \
//     </div> \
//   </div> \
//   <div class="col-sm-4"> \
//     <div class="input-group"> \
//       <div class="input-group-controls"> \
//         <input type="text" id="{{- property }}_time" class="form-control js-timepicker" value="{{- time}}" \
//              data-timepicker-options=\"mask: \'h:s\'\"/> \
//       </div> \
//       <div class="input-group-addon"> \
//         <span class="addon"><i class="icon icon-clock-o"></i></span> \
//       </div> \
//     </div> \
//   </div> \
// </div>';
//
// AGN.Opt.Templates['trackablelink-extension-table-row'] = '\
// <tr data-extension-row="{{- index}}"> \
//     <td> \
//         <input type="text" class="form-control" data-extension-name value="{{- name}}"/> \
//     </td> \
//     <td> \
//         <input type="text" class="form-control" data-extension-value value="{{- value}}"/> \
//     </td> \
//     <td class="table-actions"> \
//         {{ if (name == "" && value == "") { }} \
//             <a href="#" class="btn btn-regular btn-primary" data-extension-add id="newExtensionBtn"> \
//                 <i class="icon icon-plus"></i> \
//             </a> \
//         {{ } else { }} \
//             <a href="#" class="btn btn-regular btn-alert" data-extension-delete> \
//                 <i class="icon icon-trash-o"></i> \
//             </a> \
//         {{ } }} \
//     </td> \
// </tr>';
//
// AGN.Opt.Templates['mailing-param-row'] = '\
//   <tr data-param-row="{{- index}}"> \
//       <td> \
//           <input type="text" value="{{- name}}" data-param-name class="form-control" data-action="param-enterdown"/> \
//       </td> \
//       <td> \
//           <input type="text" value="{{- value}}" data-param-value class="form-control" data-action="param-enterdown"/> \
//       </td> \
//       <td> \
//           <input type="text" value="{{- description}}" data-param-description class="form-control" data-action="param-enterdown"/> \
//       </td> \
//       {{ if (isChangeable) { }} \
//       <td class="table-actions"> \
//         {{ if (name == "" && value == "") { }} \
//             <a href="#" class="btn btn-regular btn-primary" data-action="add-param-row" id="newParamBtn"> \
//                 <i class="icon icon-plus"></i> \
//             </a> \
//         {{ } else { }} \
//             <a href="#" class="btn btn-regular btn-alert" data-action="delete-param-row"> \
//                 <i class="icon icon-trash-o"></i> \
//             </a> \
//         {{ } }} \
//       </td> \
//       {{ } }} \
//   </tr>';
//
// AGN.Opt.Templates['mailing-reference-content-item'] = ' \
//   <div class="form-group"> \
//       <div class="col-sm-4"> \
//           <label class="control-label" for="input-{{- name }}">{{- name }}</label> \
//       </div> \
//       <div class="col-sm-8"> \
//           <div class="input-group-controls"> \
//               <input type="text" class="form-control" name="referenceContentSettings.items" value="{{- value }}" id="input-{{- name }}"/> \
//           </div> \
//           <div class="input-group-btn"> \
//               <button type="button" class="btn btn-regular btn-alert" \
//                       data-action="delete-reference-content-item"> \
//                   <i class="icon icon-trash-o"></i> \
//               </button> \
//           </div> \
//       </div> \
//   </div>';
// 
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
