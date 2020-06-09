AGN.Lib.Controller.new('mailing-priorities', function() {
  var Template = AGN.Lib.Template,
    Helpers = AGN.Lib.Helpers,
    DateFormat = AGN.Lib.DateFormat;

  var DATE_BASED_TYPE = 2;
  var NORMAL_TYPE = 0;

  var $saveButton, $priorityCount, $orderedArea,
    $unorderedDateBasedArea, $unorderedNormalArea;

  var mailingsMap;
  var originPrioritizedIds;
  var originPriorityCount;

  var createEntry;

  function updateBadges() {
    var index = 1;
    var nextDateBasedIndex = 10;
    $orderedArea.children('.l-mailing-entry')
      .each(function() {
        var $e = $(this);
        var priority = 0;
        if ($e.data('type') == DATE_BASED_TYPE) {
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

        $e.find('.l-badge .badge').text(priority);
      });
  }

  function collectPrioritizedIds(separator) {
    var ids = [];

    $orderedArea.children('.l-mailing-entry')
      .each(function() {
        var id = $(this).data('id');
        if (id > 0) {
          ids.push(id);
        }
      });

    return separator ? ids.join(separator) : ids;
  }

  function countPrioritizedTemplates() {
    var count = 0;

    $orderedArea.children('.l-mailing-entry')
      .each(function() {
        if ($(this).data('id') > 0) {
          count++;
        }
      });

    return count;
  }

  // Disable save button if there's nothing to save.
  function checkOriginChanges(reset) {
    var newPrioritizedIds = collectPrioritizedIds(';');
    var newPriorityCount = $priorityCount.val();

    if (originPrioritizedIds == null && originPriorityCount == null || reset) {
      originPrioritizedIds = newPrioritizedIds;
      originPriorityCount = newPriorityCount;

      if (newPriorityCount > 0) {
        $saveButton.prop('disabled', true);
      } else {
        var count = countPrioritizedTemplates();
        $priorityCount.val(count);
        $saveButton.prop('disabled', !count);
      }
    } else {
      $saveButton.prop('disabled', newPrioritizedIds == originPrioritizedIds && newPriorityCount == originPriorityCount);
    }
  }

  function clear() {
    $('#datebased-container, #normal-container, #ordered-area')
      .find('.l-mailing-entry').remove();
  }

  // "yyyy-mm-dd" -> [yyyy, mm, dd]
  function strDateToArray(date, stub) {
    return DateFormat.toArray(DateFormat.parse(date), stub);
  }

  // [yyyy, mm, dd] -> "yyyy-mm-dd"
  function arrayDateToStr(date) {
    return Helpers.pad(date[0], 4) + '-' + Helpers.pad(parseInt(date[1]) + 1, 2) + '-' + Helpers.pad(date[2], 2);
  }

  function shiftDateArray(dateArray, years, months, days) {
    var newDate = _.clone(dateArray);
    if (!!years) {
      newDate[0] = dateArray[0] + years;
    }

    if (!!months) {
      newDate[1] = dateArray[1] + months;
    }

    if (!!days) {
      newDate[2] = dateArray[2] + days;
    }

    return newDate;
  }

  function minDay() {
    return _.reduce(_.keys(mailingsMap), function(a, b) {
      return a < b ? a : b;
    });
  }

  var getAllDateBasedMailingsPriority = function(excludeIds) {
    var dateBasedMailings = [];
    var exclude = _.clone(excludeIds);
    _.keys(mailingsMap).forEach(function(date){
        mailingsMap[date].forEach(function(e){
          if (e.mailingType == DATE_BASED_TYPE && !exclude.includes(e.id)) {
            //reset priority for general list
            var m = _.clone(e);
            exclude.push(m.id);
            dateBasedMailings.push(e);
          }
        });

    });

    return dateBasedMailings;
  };

  function collectMailingEntries(day) {
    var entries = mailingsMap[day] || [];
    entries = entries.filter(function(e){return e.mailingType != DATE_BASED_TYPE || (e.mailingType == DATE_BASED_TYPE && e.priority > 0);});
    var excludeIds = entries.map(function (e) { return e.id;});
    return _.union(entries, getAllDateBasedMailingsPriority(excludeIds), 'id').sort(function(a, b){return a.priority > b.priority ? 1 : -1});
  }

  function select(day) {
    var entries = collectMailingEntries(day);
    clear();
    moveToProperBox(entries);
    checkOriginChanges(true);
  }

  function moveToProperBox(entries) {
    if (entries && entries.length) {
      entries.forEach(function(e) {
        updateIconClass(e);
        var html = createEntry(e);
        var ordered = e.priority;
        var type = e.mailingType;

        if (ordered) {
          $orderedArea.find('.l-stub').before(html);
        } else {
          if (type == DATE_BASED_TYPE) {
            $unorderedDateBasedArea.find('.l-stub').before(html);
          }
          if (type == NORMAL_TYPE) {
            $unorderedNormalArea.find('.l-stub').before(html);
          }
        }
      });
    }
  }

  function updateIconClass(e) {
    var iconClass = 'normal-mailing-icon';
    if (e.mailingType == DATE_BASED_TYPE) {
      iconClass = 'datebase-mailing-icon';
    }

    e.iconClass = iconClass;
  }

  var isAppropriateType = function (targetId, mailingType) {
    var isDateBasedArea = targetId === $unorderedDateBasedArea.attr('id');
    var isNormalArea = targetId === $unorderedNormalArea.attr('id');
    var isOrderedArea = targetId === $orderedArea.attr('id');

    return isOrderedArea || (isDateBasedArea && mailingType == DATE_BASED_TYPE) ||
      (isNormalArea && mailingType == NORMAL_TYPE);
  };

  var highlightBackground = function ($target, activate) {
      if (activate) {
        $target.parent().addClass('highlight');
      } else {
        $target.parent().removeClass('highlight');
      }
  };
  var findDateBasedStart = function(dates) {
    for (var key in dates) {
      var firstMailing = mailingsMap[dates[key]].find(function(value){
        return value.mailingType == DATE_BASED_TYPE;
      });
      if (!!firstMailing) {
        return dates[key];
      }
    }

    return '';
  };

  var computeDisableConstraints = function(dates) {
    var constraints = [true];

    var dateBasedStart = strDateToArray(findDateBasedStart(dates));
    var filteredDates = !dateBasedStart ? dates : dates.filter(function(d){return d < dateBasedStart});

    filteredDates.forEach(function (str) {
        var date = strDateToArray(str);
        if (date) {
          constraints.push(date);
        }
      });

    if (!!dateBasedStart) {
      constraints.push({'from': dateBasedStart, 'to': shiftDateArray(dateBasedStart, 50)});
    }

    return constraints;
  };

  var activateAvailableDates = function(mailingsMap) {
    var $picker = $('#date-picker');
    var dates = _.keys(mailingsMap).sort();

    if (dates.length) {
      var constraints = computeDisableConstraints(dates, mailingsMap);

      $picker.pickadate(_.merge({
        editable: true,
        disable: constraints,
        klass: {
          input: 'picker__input js-datepicker',
          holder: 'datepicker__holder picker__holder datepicker__holder-right'
        }
      }, Helpers.objFromString($picker.data('datepicker-options'))));

      var picker = $picker.pickadate('picker');
      if (!picker.get('select')) {
        picker.set('select', strDateToArray(minDay()));
      }
    } else {
      $picker.pickadate({editable: false});
    }

    $picker.trigger('change');
  };

  function init(config) {
    $saveButton = $('#save-button');
    $priorityCount = $('#priorityCount');
    $orderedArea = $('#ordered-area');

    $unorderedDateBasedArea = $("#datebased-container");
    $unorderedNormalArea = $("#normal-container");

    mailingsMap = config.mailingsMap;
    createEntry = Template.prepare('draggable-mailing-entry');
  }

  this.addDomInitializer('mailing-priorities', function() {
    var self = this;
    init(self.config);

    var $container = $('.l-content-area');
    $('.priority-container').sortable({
      opacity: 0.4,
      connectWith: '.priority-container',
      items: '.l-mailing-entry',
      helper: 'clone',
      containment: $container,
      appendTo: $container,
      update: function() {
        if (this.id === $orderedArea.attr('id')) {
          updateBadges();
          checkOriginChanges();
        }
      },
      out: function() {
        highlightBackground($(this), false);
      },
      over: function(e, ui) {
        highlightBackground($(e.target), false);
        highlightBackground($(this), true);
      },
      start: function() {
        highlightBackground($(this), true);
      },
      stop: function() {
        highlightBackground($(this), false);
      },
      receive: function(e, ui) {
        var mailingType = ui.item.data('type');
        var targetId = e.target.id;

        if (!isAppropriateType(targetId, mailingType)) {
          ui.sender.sortable("cancel");
        }
      }
    });

    activateAvailableDates(mailingsMap);
  });

  this.addAction({
    click: 'back'
  }, function() {
    window.history.back();
  });

  this.addAction({
    click: 'save'
  }, function() {
    var form = AGN.Lib.Form.get(this.el);

    form.setValueOnce('prioritizedIds', collectPrioritizedIds());
    form.submit();
  });

  this.addAction({
    change: 'priorityCount'
  }, function() {
    checkOriginChanges();
  });

  this.addAction({
    change: 'change-date'
  }, function() {
    var d = this.el.pickadate('picker')
      .get('select');

    if (d) {
      select(arrayDateToStr([d.year, d.month, d.date]));
    } else {
      select();
    }
  });
});
