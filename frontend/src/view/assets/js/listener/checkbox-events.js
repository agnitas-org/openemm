(function(){

  $(document).on('click', '[data-toggle-checkboxes]', function(e) {
      var $elem = $(this);
      var action = $elem.data('toggle-checkboxes');
      var tileContentId = $elem.closest('.tile-header').find('[data-toggle-tile]').data('toggle-tile');
      _.each($(tileContentId + ' [type=checkbox]:enabled'), function(item){
          var isChecked = action == 'on' ? true : false;
          $(item).prop('checked', isChecked)
      });
    e.preventDefault();
  });

  $(document).on('click', '[data-toggle-checkboxes-all]', function(e) {
    var action = $(this).data('toggle-checkboxes-all');
    _.each($('.checkboxes-content [type=checkbox]:enabled'), function(item){
      var isChecked = action == 'on' ? true : false;
      $(item).prop('checked', isChecked)
    });
    e.preventDefault();
  });

})();