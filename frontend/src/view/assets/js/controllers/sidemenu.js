AGN.Lib.Controller.new('sidemenu', function() {
  var STATUSPANEL_HEIGHT_PX = 20;
  var WebStorage = AGN.Lib.WebStorage;
  var submenuCloseDelay = 500;
  var tooltipCloseDelay = 400;
  var submenuOpenDelay = 1000/3; //this config is for JS(narrow) menu, for wide menu change delay in CSS

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
    click: 'switch-to-new-design'
  }, function() {
    const $el = this.el;
    $.post(AGN.url('/ui-design/switch.action'))
      .done(function() {
        $el.prop('disabled', true);
        // hack for wait for visual toggle switch.
        window.setTimeout(function () {
          AGN.Lib.Page.reload($el.data('switch-url'))
        }, 500);
      }).fail(function () {
        $el.prop("checked", !$el.prop("checked"));
      });
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
    var subMenuHeight = $header.outerHeight() + getSubitemsHeight($submenu);
    
    if (top + subMenuHeight > $window.height() + $window.scrollTop()) {
      top = $window.height() + $window.scrollTop() - subMenuHeight - STATUSPANEL_HEIGHT_PX;
    }
    if (top - $window.scrollTop() < 0) {
      top = $window.scrollTop();
    }

    $submenu.offset({top: top, left: left});
  }
  
  function getSubitemsHeight($submenu) {
    return $submenu.find('.subitems').find('li').first().height() 
            * $submenu.find('.subitems').find('li').length;
  }

  function setArrowOffset($arrow, $submenu, $menuItem) {
    var top = $menuItem.offset().top + $menuItem.outerHeight() / 2.0 - $arrow.outerHeight() / 2.0;
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