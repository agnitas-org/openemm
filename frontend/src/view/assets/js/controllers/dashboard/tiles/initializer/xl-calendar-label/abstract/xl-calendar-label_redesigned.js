(() => {
  const Template = AGN.Lib.Template;
  const Popover = AGN.Lib.Popover;

  class CalendarLabel {

    static SELECTOR = '.xl-calendar-label';

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
      return $(`${this.Calendar.DAY_SELECTOR}[data-date="${this.date}"]`);
    }

    get $elText() {
      return this.$el.find(`${CalendarLabel.SELECTOR}__text`);
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
      this.$day.find(`${this.Calendar.DAY_SELECTOR}__body`).append(this.$el);
    }

    attachToUnsentMailings() {
      this.unsentMailings$.append(this.$el);
      AGN.Lib.CoreInitializer.run(['tooltip', 'scrollable'], this.unsentMailings$);
    }

    get unsentMailings$() {
      return $(this.Calendar.UNSENT_MAILINGS_LIST_SELECTOR);
    }

    get draggable() {
      return true;
    }

    get templateParams() {
      return { labelId: this.labelId };
    }

    get labelId() {
      return `xl-calendar-${this.type}-${this.entityId}`;
    }

    get entityId() {
      throw new Error("entityId() must be implemented in extended class")
    }

    get templateName() {
      return `xl-calendar-${this.type}-label`;
    }

    get type() {
      return this.constructor.TYPE;
    }

    get popoverOptions() {
      throw new Error("popoverOptions() must be implemented in extended class");
    }

    get Calendar() {
      return AGN.Lib.Dashboard.XlCalendar;
    }

    initDragging() {
      this.$el.draggable({
        cursor: 'move',
        containment: $('#xl-calendar-tile .tile-body'),
        distance: 20,
        appendTo: 'body',
        zIndex: 2,
        helper: function (e) { // fix draggable clone dimensions
          const $target = $(e.currentTarget);
          return $target.clone(true).css({'width': $target.width(), 'height': $target.height()});
        },
        start: () => this.onDragStart(),
        stop: () => this.onDragStop(),
        revert: dropped => {
          if (!dropped) {
            this.show(); // when dropped not over acceptable
          }
          return false;
        }
      });
    }

    createPopover() {
      Popover.remove(this.$elText);
      Popover.create(this.$elText, this.popoverOptions);
    }

    #toggleVisibility(show) {
      this.$el.css({opacity: show ? '1' : '0'});
    }

    show() {
      this.#toggleVisibility(true);
    }

    hide() {
      this.#toggleVisibility(false);
    }

    onDragStart() {
      this.hide();
      this.#disableAllPopovers();
      this.#clearSelection();
    }

    onDragStop() {
      this.#enableAllPopovers();
    }

    #disableAllPopovers() {
      _.each($(CalendarLabel.SELECTOR), label => Popover.get($(label))?.disable());
    }

    #enableAllPopovers() {
      _.each($(CalendarLabel.SELECTOR), label => Popover.get($(label))?.enable());
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

    drop($container) {
      if (!this.canMoveTo($container)) {
        this.show();
        return;
      }
      this.moveTo($container);
    }

    canMoveTo($container) {
      return !$container.is(this.$day);
    }

    moveTo($container) {
      const lastLabel = this.Calendar.getLastLabelOfType($container, this.type);
      if (lastLabel?.length) {
        lastLabel.$el.after(this.$el);
      } else {
        $container.find(`${this.Calendar.DAY_SELECTOR}__body`).append(this.$el);
      }
      this.show();
    }

    remove() {
      this.$el.remove();
    }

    static get($label) {
      return $label.data('label');
    }

    static getByEntityId(id, type) {
      return $(`#xl-calendar #xl-calendar-${type}-${id}`).data('label');
    }
  }

  AGN.Lib.Dashboard.XlCalendarLabel = CalendarLabel;
})();
