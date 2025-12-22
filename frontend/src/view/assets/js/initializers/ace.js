/*doc
---
title: Code Editors
name: code-editors
category: Components - Code Editors
---

A textarea can be decorated with a code editor using the following classes:

Class           | Used for
----------------|-----------------
`js-editor`     | Html
`js-editor-text`| Text only
`js-editor-css` | CSS
`js-editor-eql` | EQL builder


```html
<textarea class="form-control js-editor"></textarea>
```
*/

AGN.Lib.CoreInitializer.new('ace', ['form'], ($scope = $(document)) => {

  _.each($scope.find('.js-editor, .js-editor-text, .js-editor-eql, .js-editor-css'), function(textArea) {
    AGN.Lib.Editor.get($(textArea));
  });
});
