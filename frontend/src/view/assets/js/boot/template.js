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

AGN.Opt.Templates['modal-yes-no-cancel'] = '\
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
<div class="backdrop backdrop-error js-close-error" style="position: fixed; top: 0; left: 0; bottom: 0; right:0; z-index: 1100; background-color: rgba(0,0,0,0.5)"> \
    <div class="notification notification-alert" style="position: fixed; top: 50%; left: 50%; width: 420px; margin: -80px 0 0 -210px; z-index: 1101;"> \
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
<div class="backdrop backdrop-error js-close-error" style="position: fixed; top: 0; left: 0; bottom: 0; right:0; z-index: 1100; background-color: rgba(0,0,0,0.5)"> \
    <div class="notification notification-alert" style="position: fixed; top: 50%; left: 50%; width: 420px; margin: -80px 0 0 -210px; z-index: 1101;"> \
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
<div class="table-controls clearfix"> \
  <div class="table-control pull-left"> \
    <div class="well">{{= t(\'tables.range\', itemStart, itemEnd, itemTotal) }}</div> \
  </div> \
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
</div>';

AGN.Opt.Templates['table-controls-bottom'] = '\
<div class="table-controls clearfix"> \
  <div class="table-control pull-left"> \
    <div class="well">{{= t(\'tables.range\', itemStart, itemEnd, itemTotal) }}</div> \
  </div> \
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
