(function(){

  var Form    = AGN.Lib.Form,
      Confirm = AGN.Lib.Confirm,
      Helpers = AGN.Lib.Helpers,
      Page = AGN.Lib.Page;

  // avoid loading the Single View
  // when inputs or links are clicked
  $(document).on('click', '.js-table a, .js-table input, .js-table select, .js-table button, .js-table .js-checkable, .js-table .disabled', function(e) {
    e.preventViewLoad = true;
  });

  $(document).on('click', '.js-table-sort', function(e) {
    var $this = $(this),
        $sortLink = $this.children('a'),
        $target   = $(e.target),
        href = $sortLink.attr('href');

    if ( $target[0] != $sortLink[0] ) {
      return;
    }

    if (Form.getWrapper($this).exists()) {
      var form = Form.get($this);
      var params = Helpers.paramsFromUrl(href);

      // feed the link params to the form
      _.each(params, function(field, fieldName) {
        form.setValue(fieldName, field);
      });

      form.submit();
    } else {
      $.get(href).done(function(resp) {
        Page.render(resp, false);
      });
    }

    e.preventDefault();
  });

  $(document).on('click', '.js-table-paginate', function(e) {
    var $this = $(this),
        href  = $this.attr('href'),
        form  = Form.get($this),
        params = Helpers.paramsFromUrl(href);

    // feed the link params to the form
    _.each(params, function(field, fieldName) {
      form.setValue(fieldName, field);
    });

    form.submit();
    e.preventDefault();

    var $tile = $this.closest('.tile');
    if ($tile.exists()) {
      var top = $tile.offset().top;
      if (window.scrollY > top) {
        window.scrollTo(0, top - 150);
      }
    } else {
      window.scrollTo(0, 0);
    }
  });

  $(document).on('click', '.js-row-display', function(e) {
    if (!AGN.Lib.Helpers.isMobileView()) {
      const $this = $(this);
      const href  = $this.is('button') ? $this.data('url') : $this.attr('href');

      if (href) {
        displayRowData($this, href);
      }
    }

    e.preventDefault();
  });

  function displayRowData($el, href) {
    const form  = Form.get($el)
    const jqxhr = $.get(href);
    jqxhr.done(function(resp) {
      const $resp = $(resp);
      const $modal = $resp.filter('.modal').add($resp.find('.modal'));

      if ($modal.length != 1) {
        form.updateHtml(resp);
        return;
      }

      Confirm.create(resp).done(function(resp) {
        form.updateHtml(resp);
      });
    });
  }

  $(document).on('click', '.js-row-delete', function(e) {
    var $this = $(this),
        href  = $this.is('button') ? $this.data('url') : $this.attr('href'),
        form  = Form.get($this),
        jqxhr;

    if (href) {
      jqxhr = $.get(href);
      jqxhr.done(function(resp) {

        var $resp = $(resp),
          $modal;

        $modal = $resp.filter('.modal').add($resp.find('.modal'));

        if ($modal.length != 1) {
          form.updateHtml(resp);
          return;
        }

        Confirm.create(resp).done(function(resp) {
          $this.closest('tr').remove();
          form.updateHtml(resp);
        });

      });
    }

    e.preventDefault();
  });

  // load the resource when
  // a table row is clicked
  $(document).on('click', '.js-table tr', function(e) {
    if (e.preventViewLoad || AGN.Lib.Helpers.isMobileView()) {
      return;
    }
    const $this = $(this);
    const target = $this.data('link');

    if (typeof(target) !== 'undefined') {
      displayRowData($this, target);
    }
  });

  // Make sure we stop propagation of input click event,
  // otherwise .js-checkable will immediately revert it
  $(document).on('click', '.js-checkable label, .js-checkable input[type="checkbox"], .js-checkable input[type="radio"]', function(e) {
    e.stopPropagation();
  });

  $(document).on('click', '.js-checkable', function(e) {
    var $this = $(this),
        $targets,
        $target;

      $targets = $this.find('input[type="checkbox"], input[type="radio"]');

      if ($targets.length) {
        _.each($targets, function(target) {
          $target = $(target);
          if (!$target.prop('disabled')){
            $target.prop('checked', !$target.prop('checked'));
            $target.trigger('change');
          }
        })
      }
  });

  $(document).on('click', '.disabled', function(e) {
    e.preventDefault();
  });


})();
