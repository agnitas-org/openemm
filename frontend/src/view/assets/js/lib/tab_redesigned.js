/*doc
---
title: Tabs
name: tiles_15_tabs
parent: tiles
---

Tabs can be controlled by the `data-toggle-tab="jquerySelector"` directive. The directive should be attached to an anchor and will toggle the corresponding tab.

The toggles save their state into localstorage under the jquerySelector provided to the directive. Thus if the same selector is used under different views those tabs share the same state.
To prevent saving state in localstorage, you must specify the attribute `data-store-tab-state="false"` for the tab.

By default the first tab is active.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <ul class="d-flex gap-1">
            <li><a href="#" class="btn" data-toggle-tab="#tab1-id">Tab 1</a></li>
            <li><a href="#" class="btn"  data-toggle-tab="#tab2-id">Tab 2</a></li>
        </ul>
    </div>
    <div class="tile-body">
        <div id="tab1-id">
            <p>Tab 1 Content</p>
        </div>
        <div id="tab2-id">
            <p>Tab 2 Content</p>
        </div>
    </div>
</div>
```

##### Forcing Tab State

If you want to overwrite the clients tab state you can use the `data-tab-show="true"` and `data-tab-hide="true"` directive. These directives should be applied to the tab content holder, not the tab toggle.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <ul class="d-flex gap-1">
            <li><a href="#" class="btn" data-toggle-tab="#tabforce1-id">Tab 1</a></li>
            <li><a href="#" class="btn" data-toggle-tab="#tabforce2-id">Tab 2</a></li>
        </ul>
    </div>
    <div class="tile-body">
      <div id="tabforce1-id" data-tab-hide="true">
          <p>Tab 1 Content</p>
      </div>
      <div id="tabforce2-id" data-tab-show="true">
          <p>Tab 2 Content</p>
      </div>
    </div>
</div>
```

##### Extending Tabs

Sometimes it's useful to just extend the content of one tab when showing another tab. For example when displaying basic and advanced settings, the advanced settings tab should also show the settings from the basic tab. This can be achieved by the `data-extends-tab="jquerySelector"` directive

```htmlexample
<div class="tile">
    <div class="tile-header">
        <ul class="d-flex gap-1">
            <li><a href="#" class="btn" data-toggle-tab="#tabbasic">Basic Settings</a></li>
            <li><a href="#" class="btn" data-toggle-tab="#tabadvanced" data-extends-tab="#tabbasic">Advanced Settings</a></li>
        </ul>
    </div>
    <div class="tile-body">
        <div id="tabbasic">
            <p>Basic Tab Content</p>
        </div>
        <div id="tabadvanced">
            <p>Advanced Tab Content</p>
        </div>
    </div>
</div>
```

##### Tab relative elements

Sometimes elements outside the tab should be shown/hidden if the tab is active/inactive.
To do this you need to add `data-tab-related="#tabSelector"` attribute to the element.

```htmlexample
<div class="tile">
    <div class="tile-header flex-column flex-wrap border-bottom">
        <ul class="d-flex gap-1">
            <li><a href="#" class="btn" data-toggle-tab="#tab1">Tab 1</a></li>
            <li><a href="#" class="btn" data-toggle-tab="#tab2">Tab 2</a></li>
        </ul>

        <button class="btn btn-primary" data-tab-related="#tab1">Tab1 btn</button>
        <button class="btn btn-danger" data-tab-related="#tab2">Tab2 btn</button>
    </div>
    <div class="tile-body">
        <div id="tab1">
            <p>Tab1 Content</p>
        </div>
        <div id="tab2">
            <p>Tab2 Content</p>
        </div>
    </div>
</div>
```
*/

(() => {

  const Storage = AGN.Lib.Storage;

  class Tab {
    static init($trigger) {
      const target = $trigger.data('toggle-tab');
      const conf = Storage.get(this.#getStorageKey($trigger, target)) || {};
      const $target = $(target);

      // overwrite settings
      if (typeof ($target.data('tab-show')) !== 'undefined') {
        conf.hidden = false;
        $target.data('tab-show', undefined);
      }

      // overwrite settings
      if (typeof ($target.data('tab-hide')) !== 'undefined') {
        conf.hidden = true;
        $target.data('tab-hide', undefined);
      }

      // Keep actual visibility unless explicitly specified.
      if (conf.hidden === false || conf.hidden !== true && $target.is(':visible')) {
        this.show($trigger);
      }
    }

    static show($trigger) {
      const target = $trigger.data('toggle-tab');
      const $siblings = $trigger.parents('ul').find('[data-toggle-tab]').not($trigger);
      const $target = $(target);
      const $icon = $trigger.find('.tab-toggle');
      const $extends = $($trigger.data('extends-tab'));

      _.each($siblings, sibling => this.hide($(sibling)));

      $trigger.addClass('active');
      $trigger.closest('nav').find('.btn-header-tab.active > span').text($trigger.text())
      $icon.removeClass('icon-angle-down').addClass('icon-angle-up');
      $target.removeClass('hidden');
      $extends.removeClass('hidden');
      $target.trigger('tile:show');

      this.#updateStorage($trigger, target, false);

      // Load lazy data if any
      AGN.Lib.CoreInitializer.run('load', $target);
    }

    static hide($trigger) {
      const target = $trigger.data('toggle-tab');
      const $icon = $trigger.find('.tab-toggle');

      $trigger.removeClass('active');
      $icon.removeClass('icon-angle-up').addClass('icon-angle-down');
      $(target).addClass('hidden');
      $(target).trigger('tile:hide');

      this.#updateStorage($trigger, target, true);
    }

    static toggle($trigger) {
      const $target = $($trigger.data('toggle-tab'));

      if ($target.is(':visible')) {
        this.hide($trigger)
      } else {
        this.show($trigger)
      }
    }

    static #updateStorage($trigger, target, hidden) {
      if ($trigger.attr('data-store-tab-state') === 'false') {
        return;
      }

      Storage.set(this.#getStorageKey($trigger, target), {hidden})
    }

    static #getStorageKey($trigger, target) {
      const prefix = 'toggle_tab';

      const group = $trigger.data('tab-group');
      if (group) {
        return `${prefix}/${group}`;
      }

      return prefix + target;
    }
  }

  AGN.Lib.Tab = Tab;

})();
