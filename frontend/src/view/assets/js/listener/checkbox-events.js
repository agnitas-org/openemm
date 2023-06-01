(function(){

  $(document).on('click', '[data-toggle-checkboxes]', function(e) {
      const $elem = $(this);
      const action = $elem.data('toggle-checkboxes');
      const tileContentId = $elem.closest('.tile-header').find('[data-toggle-tile]').data('toggle-tile');

      _.each($(tileContentId + ' [type=checkbox]:enabled'), function(item) {
          const isChecked = action == 'on' ? true : false;
          $(item).prop('checked', isChecked)
      });

      e.preventDefault();
  });

  $(document).on('click', '[data-toggle-checkboxes-all]', function(e) {
      const action = $(this).data('toggle-checkboxes-all');

      _.each($('.checkboxes-content [type=checkbox]:enabled'), function(item) {
          const isChecked = action == 'on' ? true : false;
          $(item).prop('checked', isChecked)
      });

      e.preventDefault();
  });

})();