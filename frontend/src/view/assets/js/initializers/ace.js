/*doc
---
title: View Directives
name: directives
category: Javascripts - View Directives
---

A directive usually consists of a css class name or an html attribute which is hooked to a initializer function or a listener, thus automatically wiring html with extended javascript functionality.

*/


/*doc
---
title: Code Editor Directive
name: ace-directive
parent: directives
---

A textarea can be decorated with a code editor using the following classes:

Class           | Used for
----------------|-----------------
`js-editor`     | Html
`js-editor-text`| Text only
`js-editor-css` | CSS
`js-editor-eql` | ?


```htmlexample
<div class="form-group">
  <div class="col-sm-4">
    <label class="form-label">
      Code Editor
    </label>
  </div>
  <div class="col-sm-8">
    <textarea class="form-control js-editor"></textarea>
  </div>
</div>
```
*/

AGN.Lib.CoreInitializer.new('ace', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('.js-editor, .js-editor-text, .js-editor-eql, .js-editor-css'), function(textArea) {
    AGN.Lib.Editor.get($(textArea));
  });
});
