AGN.Lib.Controller.new('wysiwyg-agn-tags', function () {

  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  const Confirm = AGN.Lib.Confirm;

  const insertTagsHandlers = {};
  const selectTagsHandlers = {};

  let $tagSelect;

  insertTagsHandlers["agnFULLVIEW"] = () => {
    const $option = $tagSelect.children('option:selected');
    const name = $option.val();
    if (!name) {
      return;
    }

    if (!$('#createLinkToggle').prop('checked')) {
      return `[${name}]`;
    }

    let linkText = $('#tagLinkText').val();
    if (!linkText) {
      linkText = 'fullview';
    }

    return `<a href="[${name}]">${linkText}</a>`;
  }

  insertTagsHandlers["agnWEBVIEW"] = insertTagsHandlers["agnFULLVIEW"];

  insertTagsHandlers["agnFORM"] = function () {
    const $option = $tagSelect.children('option:selected');
    const name = $option.val();
    const attributes = $option.data('attributes');

    if (!name) {
      return;
    }

    if ($('#createLinkToggle').prop('checked') === true) {
      let linkText = $('#tagLinkText').val();
      if (!linkText) {
        const attrIndex = attributes.findIndex(function (attr) {
          return attr.name === 'name';
        });
        if (attrIndex > -1) {
          linkText = $(`#agn-tag-attribute-${attrIndex}`).val();
        }
      }
      if (linkText) {
        return `<a href="[${name + composeTagAttributes(attributes)}]">${linkText}</a>`;
      }
    }

    return `[${name + composeTagAttributes(attributes)}]`;
  }

  selectTagsHandlers["agnFORM"] = function () {
    const $toggle = $('#createLinkToggle');
    if ($toggle.prop('checked') !== true) {
      $('#tagLinkText').hide();
    }
    $toggle.on('change', function () {
      $('#tagLinkText').toggle($(this).prop('checked'));
    });
  }

  selectTagsHandlers["agnFULLVIEW"] = selectTagsHandlers["agnFORM"];
  selectTagsHandlers["agnWEBVIEW"] = selectTagsHandlers["agnFULLVIEW"];

  function selectTag(name, attributes) {
    const $inputs = $('#agn-tag-attributes');

    $inputs.empty();

    if (attributes && attributes.length > 0) {
      attributes.forEach(function (attribute, index) {
        switch (attribute.type) {
          case 'SELECT':
            $inputs.append(Template.text('agn-tag-select-attribute', {
              index,
              name: attribute.name,
              options: attribute.options
            }));
            break;

          case 'TEXT':
            $inputs.append(Template.text('agn-tag-text-attribute', {index, name: attribute.name}));
            break;
        }
      });
    }

    const extendedAttributesTemplateName = `${name}-extended-attributes`;
    if (Template.exists(extendedAttributesTemplateName)) {
      const extendedAttributes = Template.dom(extendedAttributesTemplateName);
      if (extendedAttributes) {
        $inputs.append(extendedAttributes);
      }
    }

    AGN.runAll($inputs);
  }

  function composeTag() {
    const $option = $tagSelect.children('option:selected');
    const name = $option.val();

    if (name) {
      return '[' + name + composeTagAttributes($option.data('attributes')) + ']';
    }

    return null;
  }

  function composeTagAttributes(attributes) {
    let result = '';

    if (attributes && attributes.length > 0) {
      attributes.forEach(function (attribute, index) {
        const $input = $(`#agn-tag-attribute-${index}`);
        result += ` ${attribute.name}=&quot;${_.escape($input.val())}&quot;`;
      });
    }

    return result;
  }

  this.addDomInitializer('wysiwyg-agn-tags', function () {
    $tagSelect = $('#agn-tag-name');

    this.config.forEach(function (tag) {
      const $option = $('<option>', {value: tag.name, text: tag.name});
      $tagSelect.append($option);
      $option.data('attributes', tag.attributes);
    });

    Select.get($tagSelect).selectFirstValue();
    $tagSelect.trigger('change');
  });

  this.addAction({change: 'select-agn-tag'}, function () {
    const $option = this.el.children('option:selected');
    const tagName = $option.val();

    selectTag(tagName, $option.data('attributes'));

    const handler = selectTagsHandlers[tagName];
    if (handler) {
      handler();
    }
  });

  this.addAction({click: 'insert-agn-tag'}, function () {
    const tagName = $tagSelect.val();
    const handler = insertTagsHandlers[tagName];
    let code;

    if (handler) {
      code = handler();
    } else {
      code = composeTag();
    }

    if (code) {
      Confirm.get(this.el).positive(code);
    }
  });
});
