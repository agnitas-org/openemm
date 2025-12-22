AGN.Lib.Controller.new('news-editor', function() {
    var Template = AGN.Lib.Template;
    var languages = ['de', 'en'];
    var index;
    
    this.addDomInitializer('news-editor-init', function() {
        index = 0;
        var newsId = $('input[name="newsId"]').val();
        var existentNews = $('.existent-news');

        if (existentNews && existentNews.length > 0 && newsId == 0) {
            $(existentNews).each(function() {
                var language = $(this).find('.existent-news-language').val();
                var title = $(this).find('.existent-news-title').val();
                var content = $(this).find('.existent-news-content').val();

                addNewsContentBlock(language, title, content);
                updateRemoveButtonVisibility();
                updateAddButtonsVisibility();
                $(this).remove();
            });
        } else if (newsId == 0) {
            addNewsContentBlock(getFirstUnusedLanguage(), '', '');
            updateRemoveButtonVisibility();
            updateAddButtonsVisibility();
        }
    });

    this.addAction({
        click: 'addNewsContent'
    }, function() {
        addNewsContentBlock(getFirstUnusedLanguage(), '', '');
        updateRemoveButtonVisibility();
        updateAddButtonsVisibility();
    });

    this.addAction({
        click: 'removeNewsContent'
    }, function() {
        var parentContentBlock = $(this.el).closest('.l-news-content-block');
        $(parentContentBlock).remove();

        updateRemoveButtonVisibility();
        updateAddButtonsVisibility();
        refreshDropDownLanguages();
    });

    this.addAction({
        change: 'changeLanguage'
    }, function() {
        refreshDropDownLanguages();
    });

    function addNewsContentBlock(language, title, content) {
        if (language != '') {
            var $block = Template.dom('news-content-template-row', {lang: language, index: index, title: title, content: content});

            $block.appendTo('#news-content-editor');
            AGN.runAll($block);

            refreshDropDownLanguages();
            index++;
        }
    }

    function updateAddButtonsVisibility() {
        const usedLanguages = getUsedLanguages();
        const lastContentBlockIndex = $('#news-content-editor > .l-news-content-block').last().data('index');
        $("#add-news-" + lastContentBlockIndex).toggle(languages.length != usedLanguages.length);
    }

    function updateRemoveButtonVisibility() {
        const contentBlocks = $('#news-content-editor > .l-news-content-block');
        $("button[id^='add-news-']").hide();
        const firstContentBlockIndex = $(contentBlocks).first().data('index');
        $("#delete-news-" + firstContentBlockIndex).toggle(contentBlocks.length != 1);
    }

    function refreshDropDownLanguages() {
        var unusedLanguages = getUnusedLanguages();

        $('#news-content-editor > .l-news-content-block').find('select.news-language').each(function() {
            $('option', this).not(':selected').remove();
            var selectItem = $(this);
            $.each(unusedLanguages, function(index, language) {
                $(selectItem).append($('<option>', {
                    value: language,
                    text: language
                }));
            });
        });
    }

    function getUnusedLanguages() {
        var unusedLanguages = [];
        var selectedLanguages = getUsedLanguages();

        $.each(languages, function(index, language){
            if ($.inArray(language, selectedLanguages) == -1) {
                unusedLanguages.push(language);
            }
        });
        return unusedLanguages;
    }

    function getUsedLanguages() {
        var selectedLanguages = $('#news-content-editor > .l-news-content-block').find('select.news-language').find('option:selected');
        return selectedLanguages.map(function() { return $(this).val(); }).toArray();
    }

    function getFirstUnusedLanguage() {
        var usedLanguages = getUsedLanguages();
        var unusedLanguage = '';

        if (usedLanguages.length == 0) {
            unusedLanguage = languages[0];
        } else {
            $.each(languages, function(index, language) {
                if (usedLanguages.indexOf(language) < 0) {
                    unusedLanguage = language;
                    return false;
                }
            });
        }
        return unusedLanguage;
    }
});