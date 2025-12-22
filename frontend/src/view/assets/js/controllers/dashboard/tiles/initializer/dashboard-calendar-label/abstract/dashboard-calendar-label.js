(() => {
  const Template = AGN.Lib.Template;
  const Popover = AGN.Lib.Popover;

  class CalendarLabel {

    static SELECTOR = '.dashboard-calendar__label';

    constructor(data) {
      this.data = data;

      if (!this.$day.exists() && !this.data.inUnsentList) {
        return;
      }
      this.create$();
    }

    get date() {
      return this.data.date;
    }

    get $day() {
      return this.calendar.$day(this.date);
    }

    toggle(show) {
      this.$el.toggle(show);
    }

    get text() {
      return this.$elText.text();
    }

    get $elText() {
      return this.$el.find(`span:nth-child(2) span`);
    }

    create$() {
      this.$el = Template.dom(this.templateName, this.templateParams);
      this.attachToCalendar();
      this.createPopover();
      this.$el.data('label', this);
      if (this.draggable) {
        this.initDragging();
      }
      AGN.Lib.CoreInitializer.run('tooltip', this.$el);
      return this.$el;
    }

    attachToCalendar() {
      if (this.data.inUnsentList) {
        this.attachToUnsentMailings();
      } else {
        this.attachToDay();
      }
    }

    attachToDay() {
      this.calendar.$dayBody(this.$day).append(this.$el);
    }

    attachToUnsentMailings() {
      this.calendar.$unsentList.append(this.$el);
      AGN.Lib.CoreInitializer.run(['tooltip', 'scrollable'], this.calendar.$unsentList);
    }

    get calendar() {
      return $('#dashboard-calendar').data('calendar');
    }

    get draggable() {
      return true;
    }

    get templateParams() {
      return { labelId: this.labelId };
    }

    get labelId() {
      return `dashboard-calendar-${this.type}-${this.entityId}`;
    }

    get entityId() {
      throw new Error("entityId() must be implemented in extended class")
    }

    get templateName() {
      return `dashboard-calendar-${this.type}-label`;
    }

    get type() {
      return this.constructor.TYPE;
    }

    get popoverOptions() {
      throw new Error("popoverOptions() must be implemented in extended class");
    }

    initDragging() {
      this.$el.draggable({
        cursor: 'move',
        containment: $('#dashboard-calendar > .tile-body'),
        distance: 10,
        appendTo: 'body',
        zIndex: 2,
        start: () => this.onDragStart(),
        stop: () => this.onDragStop(),
        helper: function (e) { // fix draggable clone dimensions
          const $target = $(e.currentTarget);
          return $target.clone(true).css({'width': $target.width(), 'height': $target.height()});
        },
        revert: ($droppable) => {
          if ($droppable && this.canMoveTo($droppable)) {
            this.moveTo($droppable).always(() => this.$el.css("opacity", 1));
            return false;
          }
          this.$el.css("opacity", 1);
          return true;
        },
      });
    }

    createPopover() {
      Popover.remove(this.$elText);
      Popover.create(this.$elText, this.popoverOptions);
    }

    onDragStart() {
      this.$el.css("opacity", 0.5);
      this.#disableAllPopovers();
      this.#clearSelection();
    }

    onDragStop() {
      this.#enableAllPopovers();
    }

    #disableAllPopovers() {
      this.calendar.labels.forEach(label => label.popover?.disable())
    }

    #enableAllPopovers() {
      this.calendar.labels.forEach(label => label.popover?.enable())
    }

    #clearSelection() {
      let sel;
      if (document.selection && document.selection.empty) {
        document.selection.empty();
      }
      else if (window.getSelection) {
        sel = window.getSelection();
        if (sel && sel.removeAllRanges)
          sel.removeAllRanges();
      }
    }

    canMoveTo($container) {
      return !$container.is(this.$day);
    }

    moveTo($container) {
      const deferred = $.Deferred();
      const lastLabel = this.calendar.getLastLabelOfType($container, this.type);
      if (lastLabel?.length) {
        lastLabel.$el.after(this.$el);
      } else {
        this.calendar.$dayBody($container).append(this.$el);
      }
      deferred.resolve();
      return deferred.promise();
    }

    remove() {
      this.$el.remove();
    }

    static get($label) {
      return $label.data('label');
    }

    static getByEntityId(id, type) {
      return $(`#dashboard-calendar #dashboard-calendar-${type}-${id}`).data('label');
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarLabel = CalendarLabel;
})();
