// /*doc
// ---
// title: DOM Initializers
// name: js-initializers-02
// parent: js-initializers
// ---
//
// The `Dom` initializer is basically a syntactic sugar for `Core` initializer. It will be automatically invoked for every element
// having `data-initializer` attribute with appropriate value.
//
// Here's an example of `Dom` initializer that loads some data and populates a UI element.
//
// First define an html element with `data-initializer` attribute:
//
// ```html_example
// <ul data-initializer="user-list">
//   <strong>Loading...</strong>
// </ul>
// ```
//
// Then define a `Dom` initializer that simulates loading data from another source and populates that UI:
//
// ```js_example
// AGN.Lib.DomInitializer.new('user-list', function($elem, $scope) {
//   // Let's simulate data loading.
//   setTimeout(function() {
//     $elem.empty();
//
//     ['Bob', 'Alice', 'Jack'].forEach(function(userName) {
//       $elem.append($('<li></li>', { text: userName }));
//     });
//   }, 1000);
// });
// ```
//
// There are two arguments passed to the handler function:<br/>
// 1. `$elem` is the element having appropriate `data-initializer` attribute;<br/>
// 2. `$scope` is the root element of an updated document section (or the whole document if it's initial page rendering), could be `undefined`.
//
// If you're using `AGN.Lib.Controller`, you could use its method `addDomInitializer` as a shortcut:
//
// ```js_example
// AGN.Lib.Controller.new('...', function() {
//   // ...
//   this.addDomInitializer('my-new-initializer-name', function() {
//     // Do your initialization here.
//   });
//   // ...
// });
// ```
//
// The same initializer can be used multiple times in the same document. The handler will be triggered for each one of them.
//
// */
//
// /*doc
// ---
// title: Manual invocation
// name: js-initializers-03
// parent: js-initializers
// ---
//
// Sometimes you would require to enforce the initializer to run (but keep in mind it's going to be actually called only
// if there's an appropriate `data-initializer` definition on the page):
//
// ```js
// AGN.Lib.DomInitializer.try('my-new-initializer-name');
// // or
// AGN.Lib.DomInitializer.try('my-new-initializer-name', $scope);
// ```
//
// In addition you could run an initializer directly (without checking whether or not there's an element having proper `data-initializer` attribute)
// but keep in mind to provide valid arguments:
//
// ```js
// var $elem, $scope;
// // Assign $elem and $scope somehow...
// AGN.Lib.DomInitializer.run('my-new-initializer-name', $elem, $scope);
// ```
// */
//
// /*doc
// ---
// title: Lifecycle
// name: js-initializers-04
// parent: js-initializers
// ---
//
// All the DOM initializers are triggered by `AGN.runAll()` call after all the core initializers (`AGN.Lib.CoreInitializer`).
//
// Note that once registered initializer (callback) will be triggered for each DOM element having proper `data-initializer` attribute.
// So the following DOM structure:
//
// ```html
// <div data-initializer="bless-you">
//   <span data-initializer="bless-you">Foo</span>
// </div>
// ```
//
// will cause two invocations of the initializer (if registered).
//
// You can associate only one handler with an initializer name so every statement like:
//
// ```js
// AGN.Lib.DomInitializer.new('some-initializer', function() {
//   // ...
// });
// ```
//
// will overwrite previously registered handler.
//
// By returning `false` from your handler you can un-register it:
//
// ```js
// // This handler will be called just once.
// AGN.Lib.DomInitializer.new('some-initializer', function() {
//   // Do some important stuff here.
//
//   return false;
// });
// ```
//
// */
//
// /*doc
// ---
// title: Actions
// name: js-initializers-05
// parent: js-initializers
// ---
//
// Within handler of DOM initializer you can use `this.addAction(events, action)` method.
// At first sight it does the same as `this.addAction()` method within controller initializer but there's some difference.
// An `addAction` method exposed by controller context attaches an event handler to a root document element so you can remove and re-create anything within document and all the attached handlers will be preserved.
//
// But an `addAction` method exposed by DOM initializer context attaches an event handler to an element that a DOM initializer
// belongs to (the one having `data-initializer` attribute). So if that element is removed/replaced then a handler gets removed as well.
//
// ```htmlexample
// <div data-initializer="dom-initializer-actions-demo">
//   <div class="form-group">
//     <div class="col-sm-4">
//       <label class="control-label">Type anything and press enter key</label>
//     </div>
//     <div class="col-sm-4">
//       <input type="text" class="form-control" data-action="enterText"/>
//     </div>
//   </div>
//
//   <div class="form-group">
//     <div class="col-sm-push-4 col-sm-4">
//       <button type="button" class="btn btn-regular btn-primary" data-action="makeBeep">Beep!</button>
//     </div>
//   </div>
// </div>
// ```
//
// ```js-example
// AGN.Lib.DomInitializer.new('dom-initializer-actions-demo', function($e) {
//   this.addAction({enterdown: 'enterText'}, function() {
//     AGN.Lib.Messages('Here we are.', this.el.val(), 'success');
//   });
//
//   this.addAction({click: 'makeBeep'}, function() {
//     AGN.Lib.Messages('Here we are.', 'Beep!', 'success');
//   });
// });
// ```
//
// */
//
// /*doc
// ---
// title: Config
// name: js-initializers-06
// parent: js-initializers
// ---
//
// There is a simple and reliable way to store DOM initializer's config as JSON content in `<script>` element.
// Now you don't have to query proper element, retrieve its content and then parse a JSON. That all is done automatically now.
// All you need is to use `<script>` element having `application/json` type and identifier that matches initializer's name prepended by `config:` string:
//
// ```htmlexample
// <div class="form-group" data-initializer="dom-initializer-config-demo-1">
//   <div class="col-sm-4">
//     <label for="demoText" class="control-label">Click button to get the message</label>
//   </div>
//   <div class="col-sm-4">
//     <input type="text" class="form-control" id="demoText"/>
//   </div>
//
//   <div class="col-sm-4">
//     <button type="button" class="btn btn-regular btn-primary" data-action="showConfigDemo">Demo</button>
//   </div>
//
//   <!-- Here is our config -->
//   <script id="config:dom-initializer-config-demo-1" type="application/json">
//     { "target": "#demoText", "message": "Hi there!", "color": "darkblue" }
//   </script>
// </div>
// ```
//
// Keep in mind that a script element must be a descendant of DOM initializer's element.
//
// And that's it, you can simply use `this.config` from DOM initializer code:
//
// ```js_example
// AGN.Lib.DomInitializer.new('dom-initializer-config-demo-1', function($e) {
//   var opt = this.config;
//   var $target = $(opt.target);
//
//   this.addAction({click: 'showConfigDemo'}, function() {
//     $target.val(opt.message);
//     $target.css('color', opt.color);
//   });
// });
// ```
//
// Another option is to use standalone `<script>` element marked with `data-initializer`:
//
// ```htmlexample
// <div class="form-group">
//   <div class="col-sm-12">
//     <script data-initializer="dom-initializer-config-demo-2" type="application/json">
//       { "message": "This is a message provided by dom-initializer-config-demo-2 DOM initializer" }
//     </script>
//   </div>
// </div>
// ```
//
// ```js_example
// AGN.Lib.DomInitializer.new('dom-initializer-config-demo-2', function($e) {
//   $e.replaceWith($('<label></label>', {'class': 'form-control', 'text': this.config.message}));
// });
// ```
//
// This approach is typical for controllers that are using `data-initializer` to refresh controller config when some UI gets updated.
//
// */

(function() {

  var SCRIPT_JSON_SELECTOR = 'script[type="application/json"]';
  var CONFIG_PREFIX = "config:";

  var map = {};

  function Context($e, name) {
    this.el = $e;

    if ($e.is(SCRIPT_JSON_SELECTOR)) {
      this.config = $e.json();
    } else {
      var $config = $e.find('script#' + CSS.escape(CONFIG_PREFIX + name));
      if ($config.exists()) {
        var type = $config.attr('type');
        if (!type || type === 'application/json') {
          this.config = $config.json();
        } else {
          console.error('Unexpected config script type: `' + type + '`');
        }
      }
    }
  }

  Context.prototype.addAction = function(events, action) {
    AGN.Lib.Action.new(AGN.Lib.Action.translate(events), action, this.el);
  };

  function autorun($scope) {
    var $root = $scope;
    if (!$root) {
      $root = $(document);
    }

    $root.all('[data-initializer]').each(function() {
      var $elem = $(this);
      run($elem.data('initializer'), $elem, $scope);
    });
  }

  function run(name, $elem, $scope) {
    var handler = map['key#' + name];

    if (handler) {
      var result = handler.call(new Context($elem, name), $elem, $scope);
      if (result === false) {
        map["key#" + name] = undefined;
      }
      return true;
    } else {
      return false;
    }
  }

  function tryRun(name, $scope) {
    if (map['key#' + name]) {
      var $root = $scope;
      if (!$root) {
        $root = $(document);
      }

      $root.all('[data-initializer="' + CSS.escape(name) + '"]').each(function() {
        run(name, $(this), $scope);
      });
    }
  }

  function register(name, handler) {
    if (_.isFunction(handler)) {
      map['key#' + name] = handler;
    } else {
      console.error('Handler must be a function');
    }
  }

  AGN.Lib.DomInitializer = {
    autorun: autorun,
    run: run,
    try: tryRun,
    new: register
  };

})();
