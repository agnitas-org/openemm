// /*doc
// ---
// title: Tabs
// name: tabs
// category: Javascripts - Tabs
// ---
//
// Tabs can be controlled by the `data-toggle-tab="jquerySelector"` directive. The directive should be attached to an anchor and will toggle the corresponding tab.
//
// The toggles save their state into localstorage under the jquerySelector provided to the directive. Thus if the same selector is used under different views those tabs share the same state.
//
// By default the first tab is active.
//
// ```htmlexample
// <div class="tile">
//     <div class="tile-header">
//         <ul class="tile-header-nav">
//             <li>
//                 <a href="#" data-toggle-tab="#tab1-id">Tab 1</a>
//             </li>
//             <li>
//                 <a href="#" data-toggle-tab="#tab2-id">Tab 2</a>
//             </li>
//         </ul>
//     </div>
//
//     <div class="tile-content tile-content-forms">
//       <div id="tab1-id">
//         <p>Tab 1 Content</p>
//       </div>
//
//       <div id="tab2-id">
//         <p>Tab 2 Content</p>
//       </div>
//     </div>
// </div>
// ```
// */
//
// /*doc
// ---
// title: Forcing Tab State
// name: tabs-01-force
// parent: tabs
// ---
//
// If you want to overwrite the clients tab state you can use the `data-tab-show="true"` and `data-tab-hide="true"` directive. These directives should be applied to the tab content holder, not the tab toggle.
//
// ```htmlexample
// <div class="tile">
//     <div class="tile-header">
//         <ul class="tile-header-nav">
//             <li>
//                 <a href="#" data-toggle-tab="#tabforce1-id">Tab 1</a>
//             </li>
//             <li>
//                 <a href="#" data-toggle-tab="#tabforce2-id">Tab 2</a>
//             </li>
//         </ul>
//     </div>
//
//     <div class="tile-content tile-content-forms">
//       <div id="tabforce1-id" data-tab-hide="true">
//         <p>Tab 1 Content</p>
//       </div>
//
//       <div id="tabforce2-id" data-tab-show="true">
//         <p>Tab 2 Content</p>
//       </div>
//     </div>
// </div>
// ```
// */
//
// /*doc
//
// ---
// title: Extending Tabs
// name: tabs-02-extend
// parent: tabs
// ---
//
// Sometimes it's useful to just extend the content of one tab when showing another tab. For example when displaying basic and advanced settings, the advanced settings tab should also show the settings from the basic tab. This can be achieved by the `data-extends-tab="jquerySelector"` directive
//
// ```htmlexample
// <div class="tile">
//     <div class="tile-header">
//         <ul class="tile-header-nav">
//             <li>
//                 <a href="#" data-toggle-tab="#tabbasic">Basic Settings</a>
//             </li>
//             <li>
//                 <a href="#" data-toggle-tab="#tabadvanced" data-extends-tab="#tabbasic">Advanced Settings</a>
//             </li>
//         </ul>
//     </div>
//
//     <div class="tile-content tile-content-forms">
//       <div id="tabbasic">
//         <p>Basic Tab Content</p>
//       </div>
//
//       <div id="tabadvanced">
//         <p>Advanced Tab Content</p>
//       </div>
//     </div>
// </div>
// ```
//
// */

(function(){

  var Storage = AGN.Lib.Storage;

  var init,
      show,
      hide,
      getGroupId;

  init = function($trigger) {
    var target = $trigger.data('toggle-tab'),
        group = getGroupId($trigger, target),
        conf = Storage.get('toggle_tab' + group) || {},
        $target = $(target);

    // overwrite settings
    if (typeof($target.data('tab-show')) !== 'undefined') {
      conf.hidden = false;
      $target.data('tab-show', undefined);
    }

    // overwrite settings
    if (typeof($target.data('tab-hide')) !== 'undefined') {
      conf.hidden = true;
      $target.data('tab-hide', undefined);
    }

    // Keep actual visibility unless explicitly specified.
    if (conf.hidden === false || conf.hidden !== true && $target.is(':visible')) {
      show($trigger);
    }
  };

  show = function($trigger) {
    var $siblings = $trigger.parents('ul').find('[data-toggle-tab]').not($trigger),
        target    = $trigger.data('toggle-tab'),
        group     = getGroupId($trigger, target),
        $target   = $(target),
        $icon     = $trigger.find('.tab-toggle'),
        $extends  = $($trigger.data('extends-tab'));

    _.each($siblings, function(sibling) {
      hide($(sibling));
    });

    $trigger.parent().addClass('active');
    $icon.removeClass('icon-angle-down').addClass('icon-angle-up');
    $target.removeClass('hidden');
    $extends.removeClass('hidden');
    $target.trigger('tile:show');

    Storage.set('toggle_tab' + group, {hidden: false});

    // Load lazy data if any
    AGN.Lib.CoreInitializer.run('load', $target);
  };

  hide = function($trigger) {
    var target = $trigger.data('toggle-tab'),
        group  = getGroupId($trigger, target),
        $icon  = $trigger.find('.tab-toggle');

    $trigger.parent().removeClass('active');
    $icon.removeClass('icon-angle-up').addClass('icon-angle-down');
    $(target).addClass('hidden');
    $(target).trigger('tile:hide');

    Storage.set('toggle_tab' + group, {hidden: true})
  };

  toggle = function($trigger) {
    var target = $trigger.data('toggle-tab'),
        $target = $(target);

    if ( $target.is(':visible') ) {
      hide($trigger)
    } else {
      show($trigger)
    }
  };

  getGroupId = function($trigger, defaultValue) {
    var group = $trigger.data('tab-group');

    if (group) {
      return "/" + group;
    } else {
      return defaultValue;
    }
  };

  AGN.Lib.Tab = {
    init: init,
    show: show,
    hide: hide,
    toggle: toggle
  }

})();
