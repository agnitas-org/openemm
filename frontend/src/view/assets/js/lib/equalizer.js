(function(){

  var Equalizer;

  Equalizer = function(el) {
    var $el = $(el),
        $els = $el.find('[data-equalizer-watch]:visible'),
        type = $el.data('equalizer'),
        min = parseInt($el.data('equalizer-min')) || 0,
        max = parseInt($el.data('equalizer-max')) || 10000,
        heights, height;

    if ( $els.length == 0 ) {
      return;
    }

    if ( $els.length == 1 && type == 'min' ) {
      max = min;
    }

    $els.css({
      'height': '',
      'overflow': '',
      'overflow-y': ''
    });

    heights = _.map($els, function(e){
      return $(e).outerHeight(false);
    });

    if (type == 'min') {
      height = Math.min.apply(null, heights);
    } else {
      height = Math.max.apply(null, heights);
    }

    if (height < min) {
      height = min;
    }

    if (height > max) {
      height = max;
    }

    $els.css({
      'height': height,
      'overflow': 'hidden',
      'overflow-y': 'auto'
    });
  }

  AGN.Lib.Equalizer = Equalizer;

})();
