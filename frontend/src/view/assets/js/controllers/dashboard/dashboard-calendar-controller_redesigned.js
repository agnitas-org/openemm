AGN.Lib.Controller.new('dashboard-calendar', function() {
  const Label = AGN.Lib.Dashboard.DashboardCalendarLabel;
  const DashboardCalendar = AGN.Lib.Dashboard.DashboardCalendar;
  let calendar;

  this.addDomInitializer('dashboard-calendar', function() {
    calendar = new DashboardCalendar($('#dashboard-calendar'), this.config);
    new AGN.Lib.Dashboard.News();
  });

  this.addDomInitializer('unsent-list', function() {
    calendar.initUnsentList(this.config.mailings, this.config.planned);
  });

  this.addAction({change: 'toggle-scroll'}, $el => calendar.toggleScroll($el.prop('checked')));
  this.addAction({change: 'switch-mode'}, $el => calendar.toggleMode($el.prop('checked')));
  this.addAction({click: 'show-more-mailings'}, $el => calendar.showMoreMailings($el));
  this.addAction({click: 'jump-to-today'}, () => calendar.jumpToToday());
  this.addAction({keyup: 'search'}, $el => calendar.search($el.val().toLowerCase()));
  this.addAction({dblclick: 'show-comment'}, $el => Label.get($el).showModal());
  this.addAction({change: 'toggle-comments'}, $el => calendar.commentsManager.toggle($el.prop('checked')));
  this.addAction({click: 'create-comment'}, $el => calendar.commentsManager.create($el.data('date')));
  this.addAction({click: 'delete-comment'}, $el => calendar.commentsManager.remove($el.data('comment-id')));
  this.addAction({click: 'save-comment'}, $el => calendar.commentsManager.save($el.data('comment-id')));
});
