(function(){

  var init,
      trigger,
      initClasses;



  init = function() {
     var $view = $('[data-view]:checked'),
         conf = AGN.Lib.Storage.get('view_' + $view.data('view'));

    if (conf) {
        $('[data-view][value="' + conf.view + '"]').prop('checked', true);
    } else {
      conf = {
        view: $view.val()
      }
    }

    initClasses(conf.view);
  }

  trigger = function($trigger) {
    var type = $trigger.val();

    initClasses(type);
    AGN.Lib.Storage.set('view_' + $trigger.data('view'), { view: type });
  }

  initClasses = function(type) {
    _.each($('[data-view-' + type + ']'), function(target) {
      var $target = $(target);
      $target.attr('class', $target.data('view-' + type));
    })

    // fix issues w/ element rerendering
    $(window).trigger('resize');
    AGN.Lib.CoreInitializer.run('load');
    if ($('#preview').length) {
      controlPreviewDisplaying(type);
    }    
  }

  function removePreviewInnerScroll() {
    $('#preview-contents').css("height", "");
    $(".mailing-preview-wrapper").css("overflow-y", "");
  }

  function addPreviewInnerScroll($previewContainer) {
    $previewContainer.css({
      "flex-grow": "1",
      "overflow-y": "auto"
    });
  }

  function makePreviewSticky(isEmc) {
    var anyFirstTileTop = $('.tile').first().offset().top;
    $('[data-load-target="#preview"]').parent().css({
      'position': 'sticky',
      'top': (isEmc ? 15 : anyFirstTileTop) + 'px'
    });
  }

  function previewFrameLoaded($mailingPreviewWrapper) {
    return $mailingPreviewWrapper.length && $mailingPreviewWrapper.find("iframe").length;
  }

  function controlPreviewDisplaying(type) {
    var isEmc = $('#gt-wrapper').exists();
    var $previewContainer = $(".mailing-preview-wrapper");
    if (!$previewContainer.length && isEmc) {
      $previewContainer = $('#preview').find('.tile-content-padded');
    }
    if (previewFrameLoaded($previewContainer)) {
      if (type === 'block') {
        removePreviewInnerScroll($previewContainer);
      } else if (type === 'split') {
        $previewContainer.css('height', '0px');
        makePreviewSticky(isEmc);
        setPreviewTileHeight($previewContainer, isEmc);
        addPreviewInnerScroll($previewContainer, isEmc);
        $previewContainer.css('height', '');
      }
    }
  }

  function setPreviewTileHeight($previewContainer, isEmc) {
    var $previewTileContent = $previewContainer.parent();
    $previewTileContent.css({
      "display": "flex",
      "flex-direction": "column",
      "height": "calc(100vh - " + Math.ceil(($previewTileContent.offset().top - $(window).scrollTop() + (isEmc ? 45 : 10))) + "px)",
      "min-height": "200px"
    });
  }

  AGN.Lib.View = {
    init: init,
    trigger: trigger
  };

})();
