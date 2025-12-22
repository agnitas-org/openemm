/*doc
---
title: Templates
name: templates
category: Javascripts - Templates
---

`AGN.Lib.Template` provides a simple shortcut for a template engine provided by `underscore.js`.

All the `text/x-mustache-template` scripts found during initialization are stored in `AGN.Opt.Templates` and accessible via `AGN.Lib.Template`.

```htmlexample
<div class="d-flex flex-column gap-1">
  <div>
    <label for="name" class="form-label">Name</label>
    <input type="text" id="name" class="form-control" placeholder="Enter name"/>
  </div>

  <button id="showGreetings" type="button" class="btn btn-primary">Greet!</button>
</div>

<script type="text/javascript">
  AGN.Lib.Action.new({
    click: '#showGreetings'
  }, function() {
    var message = AGN.Lib.Template.text('greeting', {
      salutation: $('#name').val() || 'John'
    });
    AGN.Lib.Messages('Greetings!', message, 'success');
  });
</script>

<script id="greeting" type="text/x-mustache-template">
  Hi, {{= salutation }}.
</script>
```

`AGN.Lib.Template.prepare` creates a reusable function that accepts template parameters and produces a string.

```js_example
var composeGreeting = AGN.Lib.Template.prepare('greeting');
// ...
var greetingJohn = composeGreeting({salutation: 'John'});
var greetingJames = composeGreeting({salutation: 'James'});
```

To produce a text out of a template use `AGN.Lib.Template.text`.

```js_example
AGN.Lib.Template.text('greeting', {salutation: 'John Doe'});
```

To produce a text and immediately interpret it as an HTML code to create DOM elements use `AGN.Lib.Template.dom`.

```js_example
var $modal = AGN.Lib.Template.dom('modal', {
  title: 'John Doe',
  modalClass: '',
  content: 'Lorem ipsum...'
});

// $modal.modal();
```

*/

(() => {

  const CSRF = AGN.Lib.CSRF;
  const templates = AGN.Opt.Templates;

  const cache = [];

  class Template {
    static prepare(name) {
      if (!this.exists(name)) {
        throw new Error(`There's no template \`${name}\` registered`);
      }

      if (!cache[name]) {
        const templateFunc = _.template(templates[name]);
        cache[name] = function () {
          const template = templateFunc.apply(this, arguments);
          return CSRF.updateTokenInDOM(template);
        };
      }

      return cache[name];
    }

    static text(name, parameters) {
      return this.prepare(name)(parameters);
    }

    static dom(name, parameters) {
      return $(this.text(name, parameters));
    }

    static exists(name) {
      return name in templates;
    }

    static register(id, content) {
      AGN.Opt.Templates[id] = content;
      delete cache[id];
    }
  }

  AGN.Lib.Template = Template;

})();
