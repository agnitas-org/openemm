// /*doc
// ---
// title: Templates
// name: templates
// category: Javascripts - Templates
// ---
//
// `AGN.Lib.Template` provides a simple shortcut for a template engine provided by `underscore.js`.
//
// All the `text/x-mustache-template` scripts found during initialization are stored in `AGN.Opt.Templates` and accessible via `AGN.Lib.Template`.
//
// ```htmlexample
// <div class="form-group">
//   <div class="col-sm-4">
//     <label for="name" class="control-label">
//       Name
//     </label>
//   </div>
//   <div class="col-sm-8">
//     <input type="text" id="name" class="form-control" placeholder="Enter name"/>
//   </div>
// </div>
//
// <div class="form-group">
//   <div class="col-sm-offset-4 col-sm-8">
//     <button type="button" id="showGreetings" class="btn btn-regular btn-primary">Greet!</button>
//   </div>
// </div>
//
// <script type="text/javascript">
//   AGN.Lib.Action.new({
//     click: '#showGreetings'
//   }, function() {
//     var message = AGN.Lib.Template.text('greeting', {
//       salutation: $('#name').val() || 'John'
//     });
//     AGN.Lib.Messages('Greetings!', message, 'success');
//   });
// </script>
//
// <script id="greeting" type="text/x-mustache-template">
//   Hi, {{= salutation }}.
// </script>
// ```
//
// `AGN.Lib.Template.prepare` creates a reusable function that accepts template parameters and produces a string.
//
// ```js_example
// var composeGreeting = AGN.Lib.Template.prepare('greeting');
// // ...
// var greetingJohn = composeGreeting({salutation: 'John'});
// var greetingJames = composeGreeting({salutation: 'James'});
// ```
//
// To produce a text out of a template use `AGN.Lib.Template.text`.
//
// ```js_example
// AGN.Lib.Template.text('greeting', {salutation: 'John Doe'});
// ```
//
// To produce a text and immediately interpret it as an HTML code to create DOM elements use `AGN.Lib.Template.dom`.
//
// ```js_example
// var $modal = AGN.Lib.Template.dom('modal', {
//   title: 'John Doe',
//   modalClass: '',
//   content: 'Lorem ipsum...'
// });
//
// // $modal.modal();
// ```
//
// */

(function() {
  const CSRF = AGN.Lib.CSRF;
  const templates = AGN.Opt.Templates;

  function prepare(name) {
    if (name in templates) {
      const templateFunc = _.template(templates[name]);
      return createTemplateDecorator(templateFunc);
    }
    throw new Error("There's no template `" + name + "` registered");
  }

  function createTemplateDecorator(templateFunc) {
    return function () {
      const template = templateFunc.apply(this, arguments);
      return CSRF.updateTokenInDOM(template, true);
    }
  }

  function text(name, parameters) {
    return prepare(name)(parameters);
  }

  function dom(name, parameters) {
    return $(text(name, parameters));
  }

  AGN.Lib.Template = {
    prepare: prepare,
    text: text,
    dom: dom
  };
})();
