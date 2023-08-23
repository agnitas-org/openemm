AGN.Lib.Controller.new('user-permissions', function () {
  
  var permissions;

  this.addDomInitializer('user-permissions', function() {
    permissions = this.config.permissionCategories.map(function(permission) {
      return permission.subCategories;
    }).flatMap(function(subcategories) {
      return Object.values(subcategories);
    }).flatMap(function(subcategory) {
      return subcategory.permissions;
    })
  });
  
  this.addAction({'click': 'reset-filter'}, function () {
    $('#permission-filter').val('').trigger('change');
    showAllCategories();
    showAllPermissions();
  });

  function showPermissionsByFilters(filters) {
    permissions
      .filter(function(permission) {
        return shouldBeShownByFilters(permission, filters)
      })
      .forEach(function(permission) {
        $('[id="' + permission.name + '"]').closest('li').show();
      })
  }

  this.addAction({'click': 'apply-filter'}, function () {
    var filters = $('#permission-filter').val();
    if (!filters.length) {
      return;
    }
    hideAllPermissions();
    showPermissionsByFilters(filters);
    controlCategoriesDisplaying();
  });

  function shouldBeShownByFilters(permission, filters) {
    var shouldBeShown = false;
    filters.forEach(function(filter) {
      if (getPredicateByFilter(filter)(permission)) {
        shouldBeShown = true;
      }
    })
    return shouldBeShown;
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
    $('.sub-category-header').each(function() {
      return $(this).show();
    });
  }
  
  function showAllCategoriesTiles() {
    getCategoriesTiles$().each(function() {
      this.show();
    });
  }
  
  function getCategoriesTiles$() {
    return $('[id^="userrights_category_"]').map(function() {
      return $(this).closest('.tile');
    })
  }

  // don't show category if it has no permissions displayed
  function controlCategoriesDisplaying() {
    showAllCategories();
    getCategoriesTiles$().each(function() {
      this.toggle(this.has('.list-group-item:visible').length > 0);
    });
    $('.sub-category-header').each(function() {
      return $(this).toggle($(this).next('div').find('li:visible').length > 0);
    });
  }
  
  function showAllCategories() {
    showAllCategoriesTiles();
    showAllSubCategories();
  }
  
  function showAllPermissions() {
    $('.list-group-item').each(function() {
      $(this).show();
    })
  }
  
  function hideAllPermissions() {
    $('.list-group-item').each(function() {
      $(this).hide();
    })
  }
});
