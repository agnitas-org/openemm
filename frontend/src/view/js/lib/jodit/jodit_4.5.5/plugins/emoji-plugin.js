(() => {
  const POPUP_TEMPLATE = `
    <div class="jodit-emoji">
      <div class="jodit-emoji__header">
        <input type="text" placeholder="Search" class="jodit-emoji__search-input form-control">
      </div>
      
      <div class="jodit-emoji__body">
          {{ _.each(Object.keys(emojis), group => { }}
            <div class="jodit-emoji__group" data-jodit-emoji-group="{{- group }}">
                <h1 class="jodit-emoji__group-name">{{- t('wysiwyg.dialogs.emoji.groups.' + group) }}</h1>
                
                <div class="jodit-emoji__group-content">
                  {{ _.each(emojis[group], emoji => { }}
                      <a class="jodit-emoji__symbol" data-jodit-emoji-id="{{- emoji.id }}" type="button">{{- emoji.symbol }}</a>
                  {{ }) }}
                </div>
            </div>
          {{ }) }}
      </div>
    </div>
  `;

  Jodit.modules.Icon.set('smile', `<i class="icon icon-smile"></i>`);

  Jodit.plugins.add('emoji', editor => {
    let emojis;
    $.get(Jodit.BASE_PATH + 'emoji.json')
      .success(json => emojis = _.groupBy(json, 'group'));

    editor.options.controls['emoji'] = {
      icon: 'smile',
      tooltip: t('wysiwyg.emoji'),
      popup: (editor, current, close) => {
        setTimeout(() => {
          $(document).off('click', '.jodit-emoji__symbol');
          $(document).off('input', '.jodit-emoji__search-input');

          $(document).on('click', '.jodit-emoji__symbol', function () {
            editor.s.insertHTML($(this).text());
            close();
          });

          $(document).on('input', '.jodit-emoji__search-input', function () {
            const searchText = $(this).val().toUpperCase();
            const groupNames = Object.keys(emojis);

            groupNames.forEach(group => {
              let hasMatchedItem = false;
              emojis[group].forEach(emoji => {
                const matches = !!emoji.keywords.find(kw => kw.toUpperCase().includes(searchText));
                $(`[data-jodit-emoji-id="${emoji.id}"]`).toggle(matches);

                if (matches) {
                  hasMatchedItem = true;
                }
              });

              $(`[data-jodit-emoji-group="${group}"]`).toggle(hasMatchedItem);
            });
          });
        }, 0);

        return _.template(POPUP_TEMPLATE)({emojis});
      }
    }
  });
})();