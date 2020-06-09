AGN.Lib.Controller.new('sidemenu', function() {
  var WebStorage = AGN.Lib.WebStorage;
  var submenuCloseDelay = 500;
  var tooltipCloseDelay = 400;
  var submenuOpenDelay = 1000/3; //this config is for JS(narrow) menu, for wide menu change delay in CSS

  this.addDomInitializer('sidemenu', function ($e) {
    // Move up submenu if after open animation it goes beyond the screen
    $e.find('.subitems').on('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend',
      function(e) {
        if(!checkIfSidebarIsWide()) {
          var $subitems = $(e.target);
          var $submenu = $subitems.closest('.submenu');
          var $arrow = $submenu.find('.arrow');
          var $item = $submenu.prev('.menu-item');
          setSubmenuOffset($arrow, $submenu, $item);
          setArrowOffset($arrow, $submenu, $item);
        }
      });
  });

  this.addAction({
    mouseenter: 'activateSubmenu'
  }, function() {
    var $item = $(this.el).find('.menu-item');
    var $submenu = $item.next('.submenu');
    var $subitems = $submenu.find('.subitems');

    $('.submenu.open').not($submenu).each(function() {
      closeSubmenu($(this));
    });

    if(!checkIfSidebarIsWide()){
      clearTimeout($item.data('closeTimerId'));

      $submenu.addClass('open');
      var $arrow = $submenu.find('.arrow');
      setSubmenuOffset($arrow, $submenu, $item);
      setArrowOffset($arrow, $submenu, $item);

      $item.data('openTimerId', setTimeout(function() {
        if($submenu.is('.open')) {
          $subitems.addClass('open');
        }
      }, submenuOpenDelay));
    }
  });

  this.addAction({
    mouseleave: 'activateSubmenu'
  }, function() {
    var $item = $(this.el).find('.menu-item');
    var $submenu = $item.next('.submenu');
    var $subitems = $submenu.find('.subitems');

    if(!checkIfSidebarIsWide()){
      clearTimeout($item.data('openTimerId'));
      var closeDelay = $subitems.is('.open') ? submenuCloseDelay : tooltipCloseDelay;
      $item.data('closeTimerId', setTimeout(function() {
        closeSubmenu($submenu);
      }, closeDelay));
    }
  });

  this.addAction({
    click: 'expandMenu'
  }, function() {
    if(checkIfSidebarIsWide()) {
      collapseMenu();
    } else {
      expandMenu();
    }
    $(window).trigger('viewportChanged');
  });

  function closeSubmenu($submenu){
    $submenu.removeClass('open');
    $submenu.find('.subitems').removeClass('open');
  }

  function setSubmenuOffset($arrow, $submenu, $menuItem) {
    var $header = $submenu.find('.submenu-header');
    var top = $menuItem.scrollTop() + $menuItem.offset().top + $menuItem.outerHeight() / 2.0 - $header.outerHeight() / 2.0;
    var left = $menuItem.offset().left + $menuItem.outerWidth() + $arrow.outerWidth();
    var $window = $(window);

    if (top + $submenu.outerHeight() > $window.height() + $window.scrollTop()) {
      top = $window.height() + $window.scrollTop() - $submenu.outerHeight();
    }
    if (top - $window.scrollTop() < 0) {
      top = $window.scrollTop();
    }

    $submenu.offset({top: top, left: left});
  }

  function setArrowOffset($arrow, $submenu, $menuItem) {
    var $window = $(window);
    var top;

    if ($submenu.offset().top + $submenu.outerHeight() >= $window.height()) {
      top = $menuItem.offset().top + $menuItem.outerHeight() / 2.0 - $arrow.outerHeight() / 2.0;
    } else {
      var $header = $submenu.find('.submenu-header');
      top = $header.offset().top + $header.outerHeight() / 2.0 - $arrow.outerHeight() / 2.0;
    }

    $arrow.offset({top: top, left: $arrow.offset().left})
  }

  function expandMenu() {
    saveIfSidebarIsWide(true);
    $('body').addClass('wide-sidebar');
  }

  function collapseMenu() {
    saveIfSidebarIsWide(false);
    $('body').removeClass('wide-sidebar');
  }

  function saveIfSidebarIsWide(val){
    if(val !== true) {
      val = false;
    }
    WebStorage.set('is-wide-sidebar', {value: val});
    $.ajax(AGN.url("/sidebar/ajax/setIsWide.action"), {
      type: 'GET',
      data: {
        isWide: val
      }
    });
  }

  function checkIfSidebarIsWide() {
    return $('body').hasClass('wide-sidebar');
  }
});