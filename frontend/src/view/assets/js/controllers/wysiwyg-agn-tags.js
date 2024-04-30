AGN.Lib.Controller.new('wysiwyg-agn-tags', function() {
  var Template = AGN.Lib.Template,
    Select = AGN.Lib.Select,
    Confirm = AGN.Lib.Confirm;

  var createSelect;
  var createTextInput;
  var insertTagsHandlers = {};
  var selectTagsHandlers = {};

  insertTagsHandlers["agnFORM"] = function () {
    var $option = $('#agn-tag-name').children('option:selected');
    var name = $option.val(),
        attributes = $option.data('attributes');
    if(!name) {
      return;
    }

    var createLinkChecked = $('#createLinkToggle').prop('checked') === true;
    if(createLinkChecked) {
      var linkText = $('#tagLinkText').val();
      if(!linkText) {
        var attrIndex = attributes.findIndex(function (attr) {
          return attr.name === 'name';
        });
        if(attrIndex > -1) {
          linkText = $('#agn-tag-attribute-' + attrIndex).val();
        }
      }
      if (linkText) {
        return '<a href="[' + name + composeTagAttributes(attributes) + ']">' + linkText + '</a>';
      }
    }

    return '[' + name + composeTagAttributes(attributes) + ']';
  }

  selectTagsHandlers["agnFORM"] = function () {
    var $toggle = $('#createLinkToggle');
    if($toggle.prop('checked') !== true) {
      $('#tagLinkParams').hide();
    }
    $toggle.on('change', function () {
      var checked = $(this).prop('checked');
      if(checked === false) {
        $('#tagLinkParams').hide();
      } else {
        $('#tagLinkParams').show();
      }
    });
  }

  function selectTag(name, attributes) {
    var $inputs = $('#agn-tag-attributes');

    $inputs.empty();

    if (attributes && attributes.length > 0) {
      attributes.forEach(function(attribute, index) {
        switch (attribute.type) {
          case 'SELECT':
            $inputs.append(createSelect({index: index, name: attribute.name, options: attribute.options}));
            break;

          case 'TEXT':
            $inputs.append(createTextInput({index: index, name: attribute.name}));
            break;
        }
      });

      try {
        var extendedAttributes = Template.dom(name + '-extended-attributes');
        if(extendedAttributes) {
          $inputs.append(extendedAttributes);
        }
      } catch(e) { /* do nothing */ }

      AGN.runAll($inputs);
    }
  }

  function composeTag() {
    var $option = $('#agn-tag-name').children('option:selected');
    var name = $option.val();

    if (name) {
      return '[' + name + composeTagAttributes($option.data('attributes')) + ']';
    } else {
      return null;
    }
  }

  function composeTagAttributes(attributes) {
    var result = '';

    if (attributes && attributes.length > 0) {
      attributes.forEach(function(attribute, index) {
        var $input = $('#agn-tag-attribute-' + index);

        result += ' ' + attribute.name + '="' + _.escape($input.val()) + '"';
      });
    }

    return result;
  }

  this.addDomInitializer('wysiwyg-agn-tags', function() {
    createSelect = Template.prepare('agn-tag-select-attribute');
    createTextInput = Template.prepare('agn-tag-text-attribute');

    var $select = $('#agn-tag-name');

    this.config.forEach(function(tag) {
      var $option = $('<option>', {value: tag.name, text: tag.name});
      $select.append($option);
      $option.data('attributes', tag.attributes);
    });

    Select.get($select).selectFirstValue();
    $select.trigger('change');
  });

  this.addAction({change: 'select-agn-tag'}, function() {
    var $option = this.el.children('option:selected');
    selectTag($option.val(), $option.data('attributes'));
    var tagName = $option.val(),
        handler = selectTagsHandlers[tagName];
    if(handler) {
     handler();
    }
  });

  this.addAction({click: 'insert-agn-tag'}, function() {
    var tagName = $('#agn-tag-name').val();
    var handler = insertTagsHandlers[tagName];
    var code;
    if(handler) {
      code = handler();
    } else {
      code = composeTag();
    }

    if (code) {
      Confirm.get(this.el).positive(code);
    }
  });
});
