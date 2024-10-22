(function(){

  var Editor,
    EditorHtml,
    EditorText,
    EditorEql,
    EditorCss;

  Editor = function(textArea) {
    var self = this,
      target,
      $containerEditor,
      id,
      readOnly;

    this.el = $(textArea);
    readOnly = !!this.el.attr('readonly');
    this.isFullHeight = !!this.el.data('full-height-editor');
    this.height = parseInt(this.el.attr('rows') || 15) * 15;

    target = this.el.attr('id') + 'Editor';
    $containerEditor = $('#' + target);
    if ($containerEditor.length === 1) {
      id = target;
      var isFullHeightEditor = $containerEditor.data('full-height-editor');
      $containerEditor.css('height', isFullHeightEditor ? '100%' : (this.height + 'px'));
    } else {
      id = _.uniqueId('Editor');
      $containerEditor = $('<div id="' + id + '" class="form-control"></div>');
      $containerEditor.css('height', this.isFullHeight ? '100%' : (this.height + 'px'));
      $containerEditor.insertAfter($(this.el));
    }

    this.el.hide();
    this.editor = ace.edit(id);
    this.editor.setReadOnly(readOnly);
    this.editor.getSession().setValue( this.el.val() );
    this.editor.getSession().on('change', function(e){
      self.el.val(self.editor.getSession().getValue());
      self.el.trigger('editor:change');
    });
    this.langTools = ace.require("ace/ext/language_tools");

    this.decorate();

    if (this.el.hasClass('js-editor-validate')) {
      this.editor.getSession().setUseWorker(true);
    }

    if (this.el.hasClass('js-editor-wrap')) {
      this.editor.getSession().setUseWrapMode(true);
    }

    self.el.trigger('editor:create');
  }

  Editor.get = function($textArea) {
    var editor = $textArea.data('_editor');

    if (editor) {
      return editor;
    }

    if ($textArea.hasClass('js-editor')) {
      editor = new EditorHtml($textArea);
    } else if ($textArea.hasClass('js-editor-text')) {
      editor = new EditorText($textArea);
    } else if ($textArea.hasClass('js-editor-eql')) {
      editor = new EditorEql($textArea);
    } else if ($textArea.hasClass('js-editor-css')) {
      editor = new EditorCss($textArea);
    }

    $textArea.data('_editor', editor);
    return editor;
  }

  Editor.all = function() {
    return _.map($('.js-editor, .js-editor-text, .js-editor-eql, .js-editor-css'), function(editor) {
      return Editor.get($(editor));
    })
  }


  Editor.prototype.decorate = function() {
    if($("body").hasClass("dark-theme")) {
      this.editor.setTheme("ace/theme/idle_fingers");
    } else {
      this.editor.setTheme("ace/theme/chrome");
    }
    this.editor.session.setUseWrapMode(true);
    this.editor.session.setWrapLimitRange(null, null);
    this.editor.setBehavioursEnabled(true);
    this.editor.setShowPrintMargin(false);
    this.editor.session.setUseSoftTabs(true);
    this.editor.getSession().setUseWorker(false);
    this.editor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true,
      minLines: 20
    });
  };

  Editor.prototype.resize = function() {
    this.editor.resize();
  }

  Editor.prototype.val = function(val, saveCursorPosition = false) {
    if (typeof(val) !== 'undefined') {
      const cursor = this.editor.getSelection().getCursor();
      this.editor.getSession().setValue(val);

      if (saveCursorPosition) {
        this.goToLine(cursor.row + 1, cursor.column);
      }
    } else {
      return this.editor.getSession().getValue();
    }
  }

  Editor.prototype.goToLine = function (row, column) {
    this.editor.focus();
    this.editor.gotoLine(row, column);
  }

  // inherit from Editor
  EditorHtml = function($textArea) {
    Editor.apply(this, $textArea);
  }
  EditorHtml.prototype = Object.create(Editor.prototype);
  EditorHtml.prototype.constructor = EditorHtml;

  EditorHtml.prototype.decorate = function() {
    // super
    Editor.prototype.decorate.call(this);

    this.editor.getSession().setMode("ace/mode/html");
  }

  // inherit from Editor
  EditorText = function($textArea) {
    Editor.apply(this, $textArea);
  }
  EditorText.prototype = Object.create(Editor.prototype);
  EditorText.prototype.constructor = EditorText;


  EditorText.prototype.decorate = function() {
    if($("body").hasClass("dark-theme")) {
      this.editor.setTheme("ace/theme/idle_fingers");
    } else {
      this.editor.setTheme("ace/theme/chrome");
    }
    this.editor.session.setUseWrapMode(false);
    this.editor.session.setWrapLimitRange(null, null);
    this.editor.setBehavioursEnabled(true);
    this.editor.setShowPrintMargin(false);
    this.editor.session.setUseSoftTabs(true);
    this.editor.getSession().setUseWorker(false);
    this.editor.setOptions({
      enableBasicAutocompletion: false,
      enableSnippets: false,
      enableLiveAutocompletion: false,
      minLines: 20
    });

    this.editor.getSession().setMode("ace/mode/text");
  }

  // inherit from Editor
  EditorEql = function($textArea) {
    Editor.apply(this, $textArea);
  }
  EditorEql.prototype = Object.create(Editor.prototype);
  EditorEql.prototype.constructor = EditorEql;

  EditorEql.prototype.decorate = function() {
    if($("body").hasClass("dark-theme")) {
      this.editor.setTheme("ace/theme/idle_fingers");
    } else {
      this.editor.setTheme("ace/theme/chrome");
    }
    this.editor.session.setUseWrapMode(false);
    this.editor.session.setWrapLimitRange(null, null);
    this.editor.setBehavioursEnabled(true);
    this.editor.setShowPrintMargin(false);
    this.editor.session.setUseSoftTabs(true);
    this.editor.getSession().setUseWorker(false);
    this.langTools.setCompleters([this.langTools.keyWordCompleter, this.langTools.snippetCompleter]);
    this.editor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: false,
      enableLiveAutocompletion: true,
      minLines: 20
    });

    ace.config.loadModule("ace/ext/eql_constructions_tokens");
    ace.config.loadModule("ace/ext/eql_constructions_basic");
    ace.config.loadModule("ace/ext/eql_constructions_extended");

    this.editor.getSession().setMode("ace/mode/eql");
  }

  // inherit from Editor
  EditorCss = function($textArea) {
    Editor.apply(this, $textArea);
  }
  EditorCss.prototype = Object.create(Editor.prototype);
  EditorCss.prototype.constructor = EditorCss;

  EditorCss.prototype.decorate = function() {
    // super
    Editor.prototype.decorate.call(this);

    this.editor.getSession().setMode("ace/mode/css");
  }

  AGN.Lib.Editor     = Editor;
  AGN.Lib.EditorHtml = EditorHtml;
  AGN.Lib.EditorText = EditorText;
  AGN.Lib.EditorEql  = EditorEql;
  AGN.Lib.EditorCss  = EditorCss;

})();
