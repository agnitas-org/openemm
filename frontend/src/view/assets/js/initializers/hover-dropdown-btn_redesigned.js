/*doc
---
title: Hover-controlled Dropdown
name: dropdowns_03_on_hover
parent: dropdowns
---

It's the same as regular dropdown, but it's activated (shown) by hover, not by click. And it automatically hides when mouse cursor goes out.

All you need to do is to add `js-btn-dropdown` class to a dropdown button.

```html
<ul>
    <li class="dropdown">
        <button class="btn btn-primary btn-sm js-btn-dropdown" data-action="Save">Save</button>

        <ul class="dropdown-menu">
            <div class="dropdown__items-container">
                <li>
                    <a href="#" class="dropdown-item" data-action="saveAndBack">Save and back</a>
                </li>
                <li>
                    <a href="#" class="dropdown-item" data-action="saveAndDownload">Save and download</a>
                </li>
            </div>
        </ul>
    </li>
</ul>
```
*/

AGN.Lib.CoreInitializer.new('hover-dropdown-btn', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  const isActiveMenuAttrName = 'agn-isActivedDropdownMenu';

  _.each($scope.find('.js-btn-dropdown'), function(el) {
    const $el = $(el);
    const $dropdown = $el.closest(".dropdown");
    const $dropdownMenu = $dropdown.find('.dropdown-menu');

    $el.attr('aria-expanded', false);
    $el.data(isActiveMenuAttrName, false);

    $el.on('mouseenter', () => {
      $dropdownMenu.css('transform', `translate(0px, ${$el.outerHeight() + 5}px)`);
      toggleDropdownMenu($dropdown, $el, true);
    });
    $dropdownMenu.on('mouseenter', () => $el.data(isActiveMenuAttrName, true));

    $el.on('mouseleave', ()=>  {
      setTimeout(() => {
        if (!$el.data(isActiveMenuAttrName) && !$el.is(':hover')) {
          toggleDropdownMenu($dropdown, $el, false);
        }
      }, 400);
    });

    $dropdownMenu.on('mouseleave', () => {
      $el.data(isActiveMenuAttrName, false);
      toggleDropdownMenu($dropdown, $el, false);
    });
  });

  function toggleDropdownMenu($dropdown, $btn, open) {
    $btn.attr('aria-expanded', open);
    $dropdown.toggleClass('open', open);
  }
});
