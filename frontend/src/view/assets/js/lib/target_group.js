(function() {
    var TargetGroup;

    TargetGroup = function() {};

    TargetGroup.getComplexityColor = function(complexity) {
        var color;

        if(complexity < 10) {
            color = 'green';
        } else if(complexity >= 10 && complexity < 17) {
            color = 'yellow';
        } else {
            color = 'red';
        }

        return color;
    };

    AGN.Lib.TargetGroup = TargetGroup;
})();