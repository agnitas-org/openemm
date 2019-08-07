AGN.Lib.Controller.new('trackable-link-list', function() {
  var self = this,
      scrollToElemId;

  this.addAction({'change': 'elem-edited'}, function() {
    scrollToElemId = this.el.attr('id');
  });

  this.addAction({'scrollTo': 'scroll-to'}, function() {
    var $scrollContainer,
        $target,
        highestOffsetTop,
        targetOffsetTop;

    if (scrollToElemId) {
      $target = $(this.el).find('#' + scrollToElemId);
    } else if ($(this.el).find('[data-sizing="scroll-top-target"]').length > 0) {
      $target = $(this.el).find('[data-sizing="scroll-top-target"]');
      $target.removeAttr('data-sizing');
    } else if (location.href.indexOf('scrollToLinkId') > 0) {
      var parser = document.createElement('a'),
          params,
          targetId;
      parser.href = location.href;
      params = parser.search.substring(1, parser.search.length).split('&');
      for (var i = 0; i < params.length; i++) {
        if (params[i].indexOf('scrollToLinkId') >= 0) {
          targetId = params[i].substring(params[i].indexOf('=') + 1, params[i].length);
          $target = $('#link-'+targetId);
        }
      }
    }

    if ($target && $target.length > 0) {
      $scrollContainer = $target.closest('[data-sizing="scroll"]');
      if ($scrollContainer.length > 0) {
        highestOffsetTop = $scrollContainer.find(':first-child').offset().top;
        targetOffsetTop = $target.offset().top;
        $scrollContainer.scrollTop(0);
        $scrollContainer.scrollTop(targetOffsetTop - highestOffsetTop);
      }
    }
  });

  this.addAction({
    'click': 'delete-link'
  }, function(){
    var linkId = this.el.data('link-id');
    $(this.el).closest('[data-action="elem-edited"]').trigger('change');
    $('#linkPropertyTable').find('#linkProperty_' + linkId).remove();
  });

  this.addAction({
    'click': 'delete-all-links'
  }, function(){
    $('#linkPropertyTable').find('tbody tr').remove();
    $(this.el).closest('[data-action="elem-edited"]').trigger('change');
  });

  this.addAction({
    'click': 'add-default-extensions'
  }, function(){
    var cTemplate,
        existentExtension,
        count,
        $links = $('#linkPropertyTable').find('tr[data-link-id]');

    if ( $links.length != 0 ) {
      count = Math.max.apply(Math, _.map($links, function(link) {
        return parseInt($(link).data('link-id'));
      })) + 1;
    } else  {
      count = $('#linkPropertyTable').find('tbody tr').length + 1 ;
    }

    _.each(AGN.Opt.DefaultExtensions, function(value, name) {
      existentExtension = $('#linkPropertyTable').find('[data-link-name="' + name + '"]');

      // already exists -> update values
      if ( existentExtension.length != 0 ) {

        cTemplate = _.template(AGN.Opt.Templates['link-table-row'], {
          count: existentExtension.data('link-id'),
          linkName: name,
          linkValue: value
        });

        existentExtension.replaceWith(cTemplate);

      } else {

        cTemplate = _.template(AGN.Opt.Templates['link-table-row'], {
          count: count,
          linkName: name,
          linkValue: value
        });

        count += 1;
        $('#linkPropertyTable').find('tbody').append(cTemplate);
      }

    });
    $(this.el).closest('[data-action="elem-edited"]').trigger('change');
  });

  this.addAction({
    'click': 'add-extension'
  }, function(){

    var cTemplate,
        count,
        $links = $('#linkPropertyTable').find('tr[data-link-id]');

    if ( $links.length != 0 ) {
      count = Math.max.apply(Math, _.map($links, function(link) {
        return parseInt($(link).data('link-id'));
      })) + 1;
    } else  {
      count = $('#linkPropertyTable').find('tbody tr').length + 1 ;
    }

    cTemplate = _.template(AGN.Opt.Templates['link-table-row'], {
      count: count,
      linkName: '',
      linkValue: ''
    });

    $('#linkPropertyTable').find('tbody').append(cTemplate);
    $(this.el).closest('[data-action="elem-edited"]').trigger('change');
  });

});
