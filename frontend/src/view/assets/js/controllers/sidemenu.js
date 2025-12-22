AGN.Lib.Controller.new('side-menu', function () {

  const cursorExists = !!window.matchMedia("(pointer: fine)").matches;
  const TOOLTIP_CLOSE_DELAY = cursorExists ? 400 : 0;

  this.addAction({click: 'logout'}, function () {
    handleSidebarItemClick(
      this.el,
      this.event,
      () => AGN.Lib.Form.get($('#logoutForm')).submit('static')
    );
  });

  this.addAction({click: 'expand'}, function() {
    const isWide = isSidebarWide();
    $('.sidebar').toggleClass('sidebar--wide', !isWide);
    saveSidebarState(!isWide);

    $(window).trigger('viewportChanged');
  });

  function saveSidebarState(isWide){
    AGN.Lib.WebStorage.set('is-wide-sidebar', {value: isWide});
    $.post(AGN.url("/sidebar/setIsWide.action"), { isWide });
  }

  this.addAction({click: 'open-help'}, function () {
    handleSidebarItemClick(this.el, this.event, AGN.Lib.Helpers.openHelpModal);
  });

  function handleSidebarItemClick($el, event, callback) {
    const $tooltip = $el.find('.sidebar__tooltip');

    if (!cursorExists && !$tooltip.hasClass('open')) {
      displayTooltip($tooltip);
      event.preventDefault();

      if (!cursorExists) {
        $(document).one('click touchstart', e => {
          if (!$(e.target).closest($el).exists()) {
            closeTooltip($tooltip);
          }
        });
      }
    } else {
      callback();

      if (!cursorExists) {
        closeTooltip($tooltip);
      }
    }
  }

  if (cursorExists) {
    this.addAction({mouseenter: 'display-tooltip'}, function () {
      if (this.el.find('.sidebar__icon').exists() || !isSidebarWide()) {
        const $tooltip = this.el.find('.sidebar__tooltip');
        displayTooltip($tooltip);
      }
    });

    this.addAction({mouseleave: 'display-tooltip'}, function () {
      const $tooltip = this.el.find('.sidebar__tooltip');
      $tooltip.data('closeTimerId', setTimeout(() => closeTooltip($tooltip), TOOLTIP_CLOSE_DELAY));
    });

    this.addAction({mouseenter: 'activateSubmenu'}, function () {
      openSubMenu(this.el);
    });

    this.addAction({mouseleave: 'activateSubmenu'}, function () {
      const $link = $(this.el).find('.sidebar__link');
      const $submenu = $link.next('.sidebar__submenu');

      $link.data('closeTimerId', setTimeout(() => closeSubmenu($submenu), TOOLTIP_CLOSE_DELAY));
    });
  } else {
    this.addAction({click: 'activateSubmenu'}, function () {
      const $submenu = this.el.find('.sidebar__link').next('.sidebar__submenu');

      if ($submenu.hasClass('open')) {
        const $link = $(this.event.target).closest('a');
        if ($link.exists() && !$link.is('[data-confirm]')) {
          AGN.Lib.Page.reload($link.attr('href'));
        }
      } else {
        openSubMenu(this.el);
        this.event.preventDefault();

        const bindCloseHandler = () => {
          $(document).one('click touchstart', e => {
            if ($(e.target).closest(this.el).exists()) {
              bindCloseHandler();
            } else {
              closeSubmenu($submenu);
            }
          });
        }

        bindCloseHandler();
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
    $tooltip.css('left', getSidebarWidth() - 1);
    $tooltip.addClass('open');
  }

  function openSubMenu($el) {
    const $link = $el.find('.sidebar__link');
    const $submenu = $link.next('.sidebar__submenu');
    const $submenuList = $submenu.find('.sidebar__submenu-list');

    closeAnotherSidebarTooltips($submenu);
    clearTimeout($link.data('closeTimerId'));

    $submenu.addClass('open');
    setSubmenuOffset($submenu, $link);

    $submenuList.addClass('open');
  }

  function closeAnotherSidebarTooltips($el) {
    $('.sidebar__tooltip.open').not($el).each(function () {
      closeTooltip($(this));
    });

    $('.sidebar__submenu.open').not($el).each(function () {
      closeSubmenu($(this));
    });
  }

  function closeTooltip($el) {
    $el.removeClass('open');
  }

  function closeSubmenu($submenu) {
    $submenu.removeClass('open');
    $submenu.find('.sidebar__submenu-list').removeClass('open');
  }

  function setSubmenuOffset($submenu, $link) {
    const $window = $(window);
    const $title = $submenu.find('.sidebar__submenu-title');
    const itemsGap = parseInt($link.parent().parent().css('gap'));

    let top = $link.scrollTop() + $link.offset().top + $link.outerHeight() / 2.0 - $title.outerHeight() / 2.0 - itemsGap;
    const subMenuHeight = $submenu.outerHeight();

    if (top + subMenuHeight > $window.height() + $window.scrollTop()) {
      top = $window.height() + $window.scrollTop() - subMenuHeight - itemsGap;
    }
    if (top - $window.scrollTop() < 0) {
      top = $window.scrollTop();
    }

    $submenu.offset({ top });
    $submenu.css('left', getSidebarWidth() - 1);
  }

  function isSidebarWide() {
    return $('.sidebar').hasClass('sidebar--wide');
  }

  function getSidebarWidth() {
    return $('.sidebar').outerWidth();
  }

});