AGN.Lib.Controller.new('user|groups-permissions', function () {

  let permissions;

  this.addDomInitializer('user|groups-permissions', function() {
    permissions = collectPermissions(this.config);
    switchCategory(+getVisibleCategory$().first().attr('id').match(/\d+/)[0]); // show first category
    updatePermissionsCounter();
  });


  function collectPermissions(config) {
    if (config.isSoapUser) {
      return collectSoapUserPermissions(config);
    }
    return collectAdminOrRestUserPermissions(config);
  }

  function collectSoapUserPermissions(config) {
    const permissions = config.permissions.map(permission => ({...permission, name: permission.endpointName}));
    return [...permissions, ...config.permissionGroups];
  }

  function collectAdminOrRestUserPermissions(config) {
    return config.permissionCategories.map(function(category) {
      return category.subCategories;
    }).flatMap(function(subcategories) {
      return Object.values(subcategories);
    }).flatMap(function(subcategory) {
      return subcategory.permissions;
    });
  }

  function getCategoryPermissions$(categoryIndex) {
    return $(`#${categoryIndex}-category-permissions`).find('.permission:not(.hidden)');
  }

  function getCategoriesButtons$() {
    return $('.permission-categories__btn');
  }

  function updatePermissionsCounter() {
    _.each(getCategoriesButtons$(), function(button) {
      const $button = $(button);
      const categoryIndex = $button.data('category-index');
      const $categoryPermissions = getCategoryPermissions$(categoryIndex);
      const categoryPermissionsCount = $categoryPermissions.length;
      const activePermissionsCount = $categoryPermissions.find('[role="switch"]:checked').length;
      $(`label[for="${categoryIndex}-category-btn"] .permissions-counter`)
        .text(`(${activePermissionsCount}/${categoryPermissionsCount})`);
      $button.toggleClass('disabled', categoryPermissionsCount <= 0);
    });
  }

  function getVisibleCategory$() {
    return $('[id$="-category-permissions"]:visible');
  }

  function scrollPermissionsTop() {
    $(`[id$="-category-permissions"]:visible .tile-body`).scrollTop(0);
  }

  function switchCategory(categoryIndex) {
    showCategoryPermissions(categoryIndex);
    scrollPermissionsTop(categoryIndex);
    $('[id$="-category-btn"]').prop("checked", false);
    $(`#${categoryIndex}-category-btn`).prop("checked", true);
    triggerToggleAllUpdate();
  }

  function showCategoryPermissions(categoryIndex) {
    $('[id$="-category-permissions"]').hide();
    $(`#${categoryIndex}-category-permissions`).show();
    controlSubCategoriesDisplaying();
  }

  this.addAction({'click': 'change-category'}, function () {
    switchCategory(this.el.data('category-index'));
  });

  function showPermissionsByFilters() {
    permissions
      .filter(permission => shouldBeShownByFilters(permission))
      .map(permission => $('[id="' + permission.name + '"]').closest('.permission'))
      .forEach(permission => permission.removeClass('hidden'))
  }

  function switchToFirstActiveCategory() {
    const $enabledCategories = getCategoriesButtons$().not('.disabled');
    if (getVisiblePermissions$().length > 0 || $enabledCategories.length <= 0) {
      return;
    }
    switchCategory(+$enabledCategories.first().data('category-index'));
  }

  this.addAction({click: 'apply-filter', enterdown: 'handle-filter-enterdown'}, function () {
    this.event.preventDefault();
    hideAllPermissions();
    showPermissionsByFilters();
    controlSubCategoriesDisplaying();
    updatePermissionsCounter();
    switchToFirstActiveCategory();
    scrollPermissionsTop();
    triggerToggleAllUpdate();
  });

  function allowedByTypeFilter(permission) {
    const types = $('#type-filter').val();
    if (!types?.length) {
      return true;
    }
    return types.some(filter => getPredicateByFilter(filter)(permission));
  }

  function allowedByNameFilter(permission) {
    const filterVal = $('#name-filter').val().trim().toLowerCase();
    const name = $(`#${permission.name.replaceAll('.', '\\.')}`).parent().find('label').text().toLowerCase().trim();
    return !filterVal || name.includes(filterVal);
  }

  function allowedByRightFilter(permission) {
    const filterVal = $('#right-filter').val()?.trim()?.toLowerCase();
    return !filterVal || permission.name.toLowerCase().includes(filterVal);
  }

  function shouldBeShownByFilters(permission) {
    return allowedByNameFilter(permission)
      && allowedByRightFilter(permission)
      && allowedByTypeFilter(permission);
  }

  function getPredicateByFilter(filter) {
    switch (filter) {
      case 'granted':
        return function (permission) { return permission.granted; }
      case 'grantedForUser':
        return function (permission) { return permission.granted && !permission.showInfoTooltip }
      case 'grantedByGroup':
        return function (permission) { return permission.showInfoTooltip }
      case 'grantable':
        return function (permission) { return !permission.granted && permission.changeable }
      case 'notGrantable':
        return function (permission) { return !permission.granted && !permission.changeable }
      case 'new':
        return function (permission) { return permission.recent; }
      default:
        return function() { return true; };
    }
  }

  function showAllSubCategories() {
    $('.permission_category-subcategory').show();
  }

  function getVisiblePermissions$() {
    return getVisibleCategory$().find('.permission:visible');
  }

  function controlNoResultsInfoDisplay() {
    const shown = getVisiblePermissions$().length <= 0;
    $('.no-results-found').toggle(shown);
  }

  // don't show sub category if it has no permissions displayed
  function controlSubCategoriesDisplaying() {
    showAllSubCategories();
    $('.permission_category-subcategory').each(function() {
      return $(this).toggle($(this).find('li:visible').length > 0);
    });
    controlNoResultsInfoDisplay();
  }

  function hideAllPermissions() {
    $('.permission').addClass('hidden');
  }

  function triggerToggleAllUpdate() {
    getVisibleCategory$().find('[data-toggle-checkboxes]').trigger('update-toggle-all');
  }
});
