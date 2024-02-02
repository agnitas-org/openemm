AGN.Lib.Controller.new('side-menu', function() {
  const WebStorage = AGN.Lib.WebStorage;
  let TOOLTIP_CLOSE_DELAY;
  let cursorExists = true;

  this.addDomInitializer('side-menu', function() {
      cursorExists = !!window.matchMedia("(pointer: fine)").matches;
      TOOLTIP_CLOSE_DELAY = cursorExists ? 400 : 0;
  });

  this.addAction({
    click: 'switch-to-classic-design'
  }, function() {
    const $el = this.el;
    const $tooltip = $el.parent().parent().find('.sidebar__tooltip');

    handleSidebarItemClick($tooltip, this.event, () => {
      $.post(AGN.url('/ui-design/switch.action')).done(() => {
        $el.prop('disabled', true);
        // hack for wait for visual toggle switch.
        window.setTimeout(() => AGN.Lib.Page.reload($el.data('switch-url')), 500);
      });
    });
  });

  this.addAction({
    click: 'logout'
  }, function() {
    const $tooltip = this.el.find('.sidebar__tooltip');

    handleSidebarItemClick($tooltip, this.event, () => AGN.Lib.Form.get($('#logoutForm')).submit('static'));
  });

  this.addAction({
    click: 'open-account-data'
  }, function() {
    const $tooltip = this.el.find('.sidebar__tooltip');

    handleSidebarItemClick($tooltip, this.event, () => AGN.Lib.Page.reload(AGN.url('/user/self/view.action')));
  });

  this.addAction({
    click: 'open-help'
  }, function() {
    const $tooltip = this.el.find('.sidebar__tooltip');

    handleSidebarItemClick($tooltip, this.event, () => {
      const jqxhr = $.get(AGN.url('/support/help-center.action'));
      jqxhr.done((resp) => {
        AGN.Lib.Confirm.create(resp)
            .done((resp) => Page.render(resp));
      });
    });
  });

  function handleSidebarItemClick($tooltip, event, callback) {
    if (!cursorExists && !$tooltip.hasClass('open')) {
      displayTooltip($tooltip);
      event.preventDefault();
    } else {
      callback();
    }
  }

  this.addAction({
    mouseenter: 'display-tooltip'
  }, function() {
    if (cursorExists) {
      const $tooltip = this.el.find('.sidebar__tooltip');
      displayTooltip($tooltip);
    }
  });

  function displayTooltip($tooltip) {
    closeAnotherSidebarTooltips($tooltip);
    clearTimeout($tooltip.data('closeTimerId'));
    $tooltip.addClass('open');
  }

  this.addAction({
    mouseleave: 'display-tooltip'
  }, function() {
    const $tooltip = this.el.find('.sidebar__tooltip');
    $tooltip.data('closeTimerId',setTimeout(()=> closeTooltip($tooltip), TOOLTIP_CLOSE_DELAY));
  });

  this.addAction({
    mouseenter: 'activateSubmenu'
  }, function() {
    if (!cursorExists) {
      return;
    }

    openSubMenu(this.el);
  });

  this.addAction({
    click: 'activateSubmenu'
  }, function() {
    const $submenu = this.el.find('.menu-item').next('.submenu');

    if ($submenu.hasClass('open')) {
      const $link = $(this.event.target).closest('a');
      if ($link.exists()) {
        AGN.Lib.Page.reload($link.attr('href'));
      }
    } else {
      openSubMenu(this.el);
    }
  });

  function openSubMenu($el) {
    const $item = $el.find('.menu-item');
    const $submenu = $item.next('.submenu');
    const $submenuItemsContainer = $submenu.find('.submenu-items-container');

    closeAnotherSidebarTooltips($submenu);
    clearTimeout($item.data('closeTimerId'));

    $submenu.addClass('open');
    setSubmenuOffset($submenu, $item);

    $submenuItemsContainer.addClass('open');
  }

  this.addAction({
    mouseleave: 'activateSubmenu'
  }, function() {
    const $item = $(this.el).find('.menu-item');
    const $submenu = $item.next('.submenu');

    $item.data('closeTimerId', setTimeout(()=> closeSubmenu($submenu), TOOLTIP_CLOSE_DELAY));
  });

  function closeAnotherSidebarTooltips($el) {
    $('.sidebar__tooltip.open').not($el).each(function() {
      closeTooltip($(this));
    });

    $('.submenu.open').not($el).each(function() {
      closeSubmenu($(this));
    });
  }

  function closeTooltip($el) {
    $el.removeClass('open');
  }

  function closeSubmenu($submenu){
    $submenu.removeClass('open');
    $submenu.find('.submenu-items-container').removeClass('open');
  }

  function setSubmenuOffset($submenu, $menuItem) {
    const $window = $(window);
    const $header = $submenu.find('.submenu-header');
    const itemsGap = parseInt($menuItem.parent().parent().css('gap'));

    let top = $menuItem.scrollTop() + $menuItem.offset().top + $menuItem.outerHeight() / 2.0 - $header.outerHeight() / 2.0 - itemsGap;
    const subMenuHeight = $submenu.outerHeight();

    if (top + subMenuHeight > $window.height() + $window.scrollTop()) {
      top = $window.height() + $window.scrollTop() - subMenuHeight - itemsGap;
    }
    if (top - $window.scrollTop() < 0) {
      top = $window.scrollTop();
    }

    $submenu.offset({top: top, left: $submenu.offset().left});
  }
});