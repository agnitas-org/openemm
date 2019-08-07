AGN.Lib.Controller.new('messenger-message-stat', function () {

    this.addAction({'change': 'statistic-date-change'}, function () {
        var startDay = $('#startDate').val();
        var endDay = $('#endDate').val();

        $('#startDate').pickadate('picker').set('max', endDay);
        $('#endDate').pickadate('picker').set('min', startDay);

        AGN.Lib.DomInitializer.try('message-stat');
    });

});