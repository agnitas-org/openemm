AGN.Lib.Controller.new('mailing-priorities', function () {

  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;
  const DateFormat = AGN.Lib.DateFormat;

  const ENTRY_SELECTOR = '.mailing-priority__entry';
  const ENTRIES_CONTAINER_SELECTOR = '.mailing-priority__container';

  let $orderedArea;
  let $unorderedArea;
  let $saveButton;
  let $priorityCount

  let originPrioritizedIds;
  let originPriorityCount;

  let config;

  const initDomVariables = () => {
    $orderedArea = $('#ordered-area');
    $unorderedArea = $('#unordered-area');
    $priorityCount = $('#priorityCount');
    $saveButton = $('#save-btn');
  }

  function minDay() {
    return _.reduce(_.keys(config.mailingsMap), function(a, b) {
      return a < b ? a : b;
    });
  }

  const activateAvailableDates = () => {
    const $picker = $('#date-picker');
    const dates = _.keys(config.mailingsMap).sort();

    if (dates.length) {
      if (!$picker.datepicker('getDate')) {
        $picker.datepicker('setDate', DateFormat.parse(minDay()))
      }
    }

    $picker.trigger('change');
  };

  this.addDomInitializer('mailing-priorities', function () {
    config = this.config;
    initDomVariables();

    const $container = $('.tiles-container');
    $(ENTRIES_CONTAINER_SELECTOR).sortable({
      handle: '.priority-entry__drag-btn',
      opacity: 0.4,
      connectWith: ENTRIES_CONTAINER_SELECTOR,
      items: ENTRY_SELECTOR,
      helper: 'clone',
      containment: $container,
      appendTo: document.body,
      update: function () {
        if (this.id === $orderedArea.attr('id')) {
          updatePriorityOrder();
          checkOriginChanges();
        }
      },
      out: function () {
        highlightBackground($(this), false);
      },
      over: function (e, ui) {
        highlightBackground($(e.target), false);
        highlightBackground($(this), true);
      },
      start: function () {
        highlightBackground($(this), true);
      },
      stop: function () {
        highlightBackground($(this), false);
      }
    });

    activateAvailableDates();
  });

  this.addAction({change: 'priorityCount'}, checkOriginChanges);
  this.addAction({input: 'priorityCount'}, function () {
    const value = parseFloat(this.el.val());
    const newValue = !isNaN(value) && value >= 0 ? value : 0;
    this.el.val(newValue);
  });

  this.addAction({change: 'change-date'}, function() {
    const date = this.el.datepicker('getDate');
    if (date) {
      select(arrayDateToStr([date.getFullYear(), date.getMonth(), date.getDate()]));
    } else {
      select();
    }
  });

  this.addAction({click: 'save'}, function() {
    const form = AGN.Lib.Form.get(this.el);

    form.setValueOnce('prioritizedIds', collectPrioritizedIds());
    form.submit();
  });

  const highlightBackground = ($target, activate) => {
    $target.closest(ENTRIES_CONTAINER_SELECTOR).toggleClass('highlighted', activate);
  };

  function updatePriorityOrder() {
    let index = 1;
    let nextDateBasedIndex = 10;
    $orderedArea.children(ENTRY_SELECTOR)
      .each(function () {
        const $e = $(this);
        let priority = 0;
        if ($e.data('type') == config.DATE_BASED_TYPE) {
          priority = nextDateBasedIndex;
          index = nextDateBasedIndex;
          nextDateBasedIndex += 10;
        } else {
          priority = index;
          if (index > nextDateBasedIndex) {
            nextDateBasedIndex = Math.round(index / 10) * 10;
          }
        }
        index++;

        $e.find('.priority-entry__order').text(`${priority}.`);
      });
  }

  function select(day) {
    const entries = collectMailingEntries(day);
    $(ENTRY_SELECTOR).remove();
    moveToProperBox(entries);
    checkOriginChanges(true);
  }

  function collectMailingEntries(day) {
    let entries = (config.mailingsMap[day] || [])
      .filter(e => e.mailingType !== config.DATE_BASED_TYPE || e.priority > 0);

    const excludeIds = entries.map(e => e.id);

    const dateBasedMailingsWithoutPriority = getAllDateBasedMailingsWithoutPriority(excludeIds);
    return _.union(entries, dateBasedMailingsWithoutPriority, 'id')
      .sort((a, b) => a.priority > b.priority ? 1 : -1);
  }

  const getAllDateBasedMailingsWithoutPriority = (excludeIds) => {
    const dateBasedMailings = [];
    const exclude = _.clone(excludeIds);

    _.keys(config.mailingsMap).forEach(date => {
      config.mailingsMap[date].forEach(entry => {
        if (entry.mailingType == config.DATE_BASED_TYPE && !exclude.includes(entry.id)) {
          //reset priority for general list
          exclude.push(_.clone(entry).id);
          dateBasedMailings.push(entry);
        }
      });

    });

    return dateBasedMailings;
  };

  function moveToProperBox(entries) {
    if (entries && entries.length) {
      entries.forEach(function (e) {
        if (e.thumbnailId) {
          e.thumbnailUrl = config.thumbnailLinkPattern.replace('__thumbnailId__', e.thumbnailId);
        } else {
          e.thumbnailUrl = config.noPreviewUrl;
        }

        const $priorityEntry = Template.dom('draggable-mailing-priority-entry', e);

        if (e.priority) {
          $orderedArea.append($priorityEntry);
        } else {
          $unorderedArea.append($priorityEntry);
        }

        AGN.runAll($priorityEntry);
      });
    }
  }

  // [yyyy, mm, dd] -> "yyyy-mm-dd"
  function arrayDateToStr(date) {
    return `${Helpers.pad(date[0], 4)}-${Helpers.pad(parseInt(date[1]) + 1, 2)}-${Helpers.pad(date[2], 2)}`;
  }

  // Disable save button if there's nothing to save.
  function checkOriginChanges(reset) {
    const newPrioritizedIds = collectPrioritizedIds(';');
    const newPriorityCount = $priorityCount.val();

    if (originPrioritizedIds == null && originPriorityCount == null || reset) {
      originPrioritizedIds = newPrioritizedIds;
      originPriorityCount = newPriorityCount;

      if (newPriorityCount > 0) {
        $saveButton.prop('disabled', true);
      } else {
        const count = countPrioritizedTemplates();
        $priorityCount.val(count);
        $saveButton.prop('disabled', !count);
      }
    } else {
      $saveButton.prop('disabled', newPrioritizedIds == originPrioritizedIds && newPriorityCount == originPriorityCount);
    }
  }

  function collectPrioritizedIds(separator) {
    const ids = [];

    $orderedArea.children(ENTRY_SELECTOR)
      .each(function() {
        const id = $(this).data('id');
        if (id > 0) {
          ids.push(id);
        }
      });

    return separator ? ids.join(separator) : ids;
  }

  function countPrioritizedTemplates() {
    return $orderedArea
      .children(ENTRY_SELECTOR)
      .filter(function () {
        return $(this).data('id') > 0;
      })
      .length;
  }
});
