AGN.Lib.Controller.new('side-menu', function () {

  const cursorExists = !!window.matchMedia("(pointer: fine)").matches;
  const TOOLTIP_CLOSE_DELAY = cursorExists ? 400 : 0;

  this.addAction({click: 'switch-to-classic-design'}, function () {
    const $el = this.el;

    handleSidebarItemClick($el.parent().parent(), this.event, () => {
      $.post(AGN.url('/ui-design/switch.action')).done(() => {
        $el.prop('disabled', true);
        // hack for wait for visual toggle switch.
        window.setTimeout(() => AGN.Lib.Page.reload($el.data('switch-url')), 500);
      }).fail(() => $el.prop("checked", !$el.prop("checked")));
    });
  });

  this.addAction({click: 'logout'}, function () {
    handleSidebarItemClick(
      this.el,
      this.event,
      () => AGN.Lib.Form.get($('#logoutForm')).submit('static')
    );
  });

  this.addAction({click: 'open-help'}, function () {
    handleSidebarItemClick(this.el, this.event, AGN.Lib.Helpers.openHelpModal);
  });

  function handleSidebarItemClick($el, event, callback) {
    const $tooltip = $el.find('.sidebar__tooltip');

    if (!cursorExists && !$tooltip.hasClass('open')) {
      displayTooltip($tooltip);
      event.preventDefault();
    } else {
      callback();
    }
  }

  if (cursorExists) {
    this.addAction({mouseenter: 'display-tooltip'}, function () {
      const $tooltip = this.el.find('.sidebar__tooltip');
      displayTooltip($tooltip);
    });

    this.addAction({mouseleave: 'display-tooltip'}, function () {
      const $tooltip = this.el.find('.sidebar__tooltip');
      $tooltip.data('closeTimerId', setTimeout(() => closeTooltip($tooltip), TOOLTIP_CLOSE_DELAY));
    });

    this.addAction({mouseenter: 'activateSubmenu'}, function () {
      openSubMenu(this.el);
    });

    this.addAction({mouseleave: 'activateSubmenu'}, function () {
      const $item = $(this.el).find('.menu-item');
      const $submenu = $item.next('.submenu');

      $item.data('closeTimerId', setTimeout(() => closeSubmenu($submenu), TOOLTIP_CLOSE_DELAY));
    });
  } else {
    this.addAction({click: 'activateSubmenu'}, function () {
      const $submenu = this.el.find('.menu-item').next('.submenu');

      if ($submenu.hasClass('open')) {
        const $link = $(this.event.target).closest('a');
        if ($link.exists() && !$link.is('[data-confirm]')) {
          AGN.Lib.Page.reload($link.attr('href'));
        }
      } else {
        openSubMenu(this.el);
      }
    });

    this.addAction({click: 'open-account-data'}, function () {
      handleSidebarItemClick(
        this.el,
        this.event,
        () => AGN.Lib.Page.reload(AGN.url(this.el.data('url')))
      );
    });
  }

  function displayTooltip($tooltip) {
    closeAnotherSidebarTooltips($tooltip);
    clearTimeout($tooltip.data('closeTimerId'));
    $tooltip.addClass('open');
  }

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

  function closeAnotherSidebarTooltips($el) {
    $('.sidebar__tooltip.open').not($el).each(function () {
      closeTooltip($(this));
    });

    $('.submenu.open').not($el).each(function () {
      closeSubmenu($(this));
    });
  }

  function closeTooltip($el) {
    $el.removeClass('open');
  }

  function closeSubmenu($submenu) {
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