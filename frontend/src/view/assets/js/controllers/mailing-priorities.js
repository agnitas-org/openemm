AGN.Lib.Controller.new('mailing-priorities', function() {
  var Template = AGN.Lib.Template,
    Helpers = AGN.Lib.Helpers;

  var $saveButton, $priorityCount, $orderedArea, $unorderedArea;
  var mailingsMap;
  var originPrioritizedIds;
  var originPriorityCount;

  var createEntry;

  function updateBadges() {
    var index = 1;

    $orderedArea.children('.l-mailing-entry')
      .each(function() {
        $(this).find('.l-badge .badge')
          .text(index++);
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
    $orderedArea.find('.l-mailing-entry')
      .remove();
    $unorderedArea.find('.l-mailing-entry')
      .remove();
  }

  // "yyyy-mm-dd" -> [yyyy, mm, dd]
  function strDateToArray(date, stub) {
    var match = date.match(/^(\d{4})\-(\d{2})\-(\d{2})$/);
    if (match) {
      return [parseInt(match[1]), parseInt(match[2]) - 1, parseInt(match[3])];
    }

    return arguments.length == 1 ? null : stub;
  }

  // [yyyy, mm, dd] -> "yyyy-mm-dd"
  function arrayDateToStr(date) {
    return Helpers.pad(date[0], 4) + '-' + Helpers.pad(parseInt(date[1]) + 1, 2) + '-' + Helpers.pad(date[2], 2);
  }

  function minDay() {
    return _.reduce(_.keys(mailingsMap), function(a, b) {
      return a < b ? a : b;
    });
  }

  function select(day) {
    var entries = mailingsMap[day] ||
      mailingsMap[minDay()];

    clear();

    if (entries && entries.length) {
      var $orderedStub = $orderedArea.find('.l-stub');
      var $unorderedStub = $unorderedArea.find('.l-stub');

      entries.forEach(function(e) {
        var html = createEntry(e);

        if (e.priority) {
          $orderedStub.before(html);
        } else {
          $unorderedStub.before(html);
        }
      });
    }

    checkOriginChanges(true);
  }

  this.addDomInitializer('mailing-priorities', function() {
    var $container = $('.l-content-area');
    var $orderedBackground, $unorderedBackground;

    $saveButton = $('#save-button');
    $priorityCount = $('#priorityCount');
    $orderedArea = $('#ordered-area');
    $unorderedArea = $('#unordered-area');

    $orderedBackground = $orderedArea.parent();
    $unorderedBackground = $unorderedArea.parent();

    mailingsMap = this.config.mailingsMap;
    createEntry = Template.prepare('draggable-mailing-entry');

    $orderedArea.sortable({
      opacity: 0.4,
      connectWith: '#unordered-area',
      items: '.l-mailing-entry',
      helper: 'clone',
      containment: $container,
      appendTo: $container,
      update: function() {
        updateBadges();
        checkOriginChanges();
      },
      out: function() {
        $orderedBackground.removeClass('highlight');
      },
      over: function() {
        $unorderedBackground.removeClass('highlight');
        $orderedBackground.addClass('highlight');
      },
      start: function() {
        $orderedBackground.addClass('highlight');
      },
      stop: function() {
        $orderedBackground.removeClass('highlight');
      }
    });

    $unorderedArea.sortable({
      opacity: 0.4,
      connectWith: '#ordered-area',
      items: '.l-mailing-entry',
      helper: 'clone',
      containment: $container,
      appendTo: $container,
      out: function() {
        $unorderedBackground.removeClass('highlight');
      },
      over: function() {
        $orderedBackground.removeClass('highlight');
        $unorderedBackground.addClass('highlight');
      },
      start: function() {
        $unorderedBackground.addClass('highlight');
      },
      stop: function() {
        $unorderedBackground.removeClass('highlight');
      }
    });

    var $picker = $('#date-picker');
    var dates = _.keys(mailingsMap);

    if (dates.length) {
      var constraints = [true];

      dates.forEach(function(str) {
        var date = strDateToArray(str);
        if (date) {
          constraints.push(date);
        }
      });

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
