AGN.Lib.Controller.new('recipient-list', function () {
  var self = this;

  this.addDomInitializer('recipient-list', function () {

    $('#basicSearch').on("click", function () {
      $("input[name=advancedSearch]").val(false);
    });
    $('#advancedSearch').on("click", function () {
      $("input[name=advancedSearch]").val(true);
    });
    $('#add-rule-table').on('change', function () {
      $('#refresh-button').attr('data-form-set', 'advancedSearch:true, addTargetNode:true')
    });
    var basicSearchActive = $("#basicSearch").hasClass("tab active");
    var advancedSearchActive = $("#advancedSearch").hasClass("tab active");
    if (advancedSearchActive) {
      $("input[name=advancedSearch]").val(true);
    }
    if (basicSearchActive) {
      $("input[name=advancedSearch]").val(false);
    }
  });

  this.addAction({
    'change': 'change-fields-to-search'
  }, function () {
    if (this.el.val() == "-1") {
      $("#search_recipient_type").prop("disabled", true);
      $("#search_recipient_state").prop("disabled", true);
    } else {
      $("#search_recipient_type").prop("disabled", false);
      $("#search_recipient_state").prop("disabled", false);
    }
    var value = $("#search_mailinglist").val();
    $("#search_mailinglist_advanced").select2('val', value);
    if (value == "-1") {
      $("#search_recipient_type_advanced").prop("disabled", true);
      $("#search_recipient_state_advanced").prop("disabled", true);
    } else {
      $("#search_recipient_type_advanced").prop("disabled", false);
      $("#search_recipient_state_advanced").prop("disabled", false);
    }
  });

  this.addAction({
    'change': 'change-target-group'
  }, function () {
    var value = $("#search_targetgroup").val();
    $("#search_targetgroup_advanced").select2('val', value);
  });

  this.addAction({
    'change': 'change-recipient-type'
  }, function () {
    var value = $("#search_recipient_type").val();
    $("#search_recipient_type_advanced").select2('val', value);
  });

  this.addAction({
    'change': 'change-user-status'
  }, function () {
    var value = $("#search_recipient_state").val();
    $("#search_recipient_state_advanced").select2('val', value);
  });

  this.addAction({
    'change': 'change-fields-to-search-advanced'
  }, function () {
    if (this.el.val() == "-1") {
      $("#search_recipient_type_advanced").prop("disabled", true);
      $("#search_recipient_state_advanced").prop("disabled", true);
    } else {
      $("#search_recipient_type_advanced").prop("disabled", false);
      $("#search_recipient_state_advanced").prop("disabled", false);
    }
    var value = $("#search_mailinglist_advanced").val();
    $("#search_mailinglist").select2('val', value);
    if (value == "-1") {
      $("#search_recipient_type").prop("disabled", true);
      $("#search_recipient_state").prop("disabled", true);
    } else {
      $("#search_recipient_type").prop("disabled", false);
      $("#search_recipient_state").prop("disabled", false);
    }
  });

  this.addAction({
    'change': 'change-target-group-advanced'
  }, function () {
    var value = $("#search_targetgroup_advanced").val();
    $("#search_targetgroup").select2('val', value);
  });

  this.addAction({
    'change': 'change-recipient-type-advanced'
  }, function () {
    var value = $("#search_recipient_type_advanced").val();
    $("#search_recipient_type").select2('val', value);
  });

  this.addAction({
    'change': 'change-user-status-advanced'
  }, function () {
    var value = $("#search_recipient_state_advanced").val();
    $("#search_recipient_state").select2('val', value);
  });

  this.addAction({
    'click': 'choose-advanced-search'
  }, function () {
    if ($('#search_first_name').val().trim() ||
        $('#search_name').val().trim() || $('#search_email').val().trim()) {
      $('#recipientForm').submit();
    }
  });

  this.addAction({
    'click': 'toggle-recipient-tab'
  }, function () {
    //imitate tab toggling for recipient tab overview
    //necessary to redirect to specific link
    var $el = $(this.el);
    var siblings = $("[data-action='toggle-recipient-tab']");
    _.each(siblings, function(sibling){
      AGN.Lib.Storage.set('toggle_tab' + $(sibling).data('tab-id'), {hidden: true});
    });

    AGN.Lib.Storage.set('toggle_tab' + $el.data('tab-id'), {hidden: false});
    var url = $el.data('url');
    if (!!url) {
      window.location.href = url;
    }
  });

  this.addAction({
    'click': 'reset-search'
  }, function () {
    /* cleaning basic search */
    $('#search_first_name').val("");
    $('#search_name').val("");
    $('#search_email').val("");
    $("#search_mailinglist_advanced").select2('val', 0);
    $("#search_targetgroup_advanced").select2('val', 0);
    $("#search_recipient_type_advanced").select2('val', "");
    $("#search_recipient_state_advanced").select2('val', 0);
    $("#search_mailinglist").select2('val', 0);
    $("#search_targetgroup").select2('val', 0);
    $("#search_recipient_type").select2('val', "");
    $("#search_recipient_state").select2('val', 0);

    /* cleaning advanced search */
    $('[name="columnAndTypeNew"]').val("");
    $('[name="chainOperatorNew"]').val("");
    $('[name="parenthesisOpenedNew"]').val("");
    $('[name="primaryOperatorNew"]').val("");
    $('[name="primaryValueNew"]').val("");
    $('[name="parenthesisClosedNew"]').val("");
    $('[name="dateFormatNew"]').val("");
    $('[name="secondaryOperatorNew"]').val("");
    $('[name="secondaryValueNew"]').val("");

    $('#recipientForm').submit();
  });
});
