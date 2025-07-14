AGN.Lib.DomInitializer.new( 'mailing-content-overview', function($elem, $scope) {
  const config = this.config;
  const $datePicker = $('#myDatePicker');
  const $timePicker = $('#myTimePicker');

  if (!$scope) {
    var data = AGN.Lib.Storage.get('mailings-content-dynNameID');
    if (data) {
      var $tr = $("tr[data-dyn-name-id='" + data.dynNameID + "']");
      if ($tr.exists()) {
        var top = $tr.position().top;
        setTimeout(function () {
          $("body").scrollTop(top);
        }, 1);
        AGN.Lib.Storage.set('mailings-content-dynNameID', {dynNameID: 0});
      }
    }
  }

  if ($datePicker.exists()) {
    $datePicker.on('change', function(){
      const d = $datePicker.datepicker("getDate");
      $('#startDay').val(d ? d.getDate() : 0);
      $('#startMonth').val(d ? d.getMonth() + 1 : 0);
      $('#startYear').val(d ? d.getFullYear() : 0);
    });


    $timePicker.on('timepicker:complete', function() {
      var time = $timePicker.val();

      $('#startHour').val(time.substr(0, 2));
      $('#startMinute').val(time.substring(3, 5));
    });

    $timePicker.on('timepicker:incomplete', function() {
      $('#startHour').val(0);
      $('#startMinute').val(0);
    });
  }

  if (!config.isEditableMailing || !AGN.Opt.Components.MailingContentLock.manageLock(config.mailingId, config.isMailingExclusiveLockingAcquired, config.anotherLockingUserName)) {
    var $controls = $('[data-controls-group="editing"]');
    $controls.prop('disabled', true);
    $controls.addClass('disabled');
  }
});
