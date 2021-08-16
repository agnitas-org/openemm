(function() {
    function Constraints(rules) {
        this.rules = rules;
    }

    Constraints.prototype.isConstraint = function (value) {
        return _.isBoolean(value) || _.isFunction(value);
    };

    Constraints.prototype.evaluate = function (constraint, source, target) {
        if (constraint === true || constraint === false) {
            return constraint;
        }

        if (_.isFunction(constraint)) {
            return constraint(source, target);
        }

        return undefined;
    };

    Constraints.prototype.getRulesToCheck = function (source, target) {
        return [{from: source.type, to: [target.type, '*']}, {from: '*', to: [target.type, '*']}];
    };

    Constraints.prototype.getRelevantConstraints = function (source, target) {
        var isConstraint = this.isConstraint;
        var rulesToCheck = this.getRulesToCheck(source, target);

        var rules = this.rules;
        var constraints = [];

        rulesToCheck.forEach(function (rule) {
            var isRuleMissing = true;

            if (_.has(rules, rule.from)) {
                var value = rules[rule.from];

                if (isConstraint(value)) {
                    constraints.push(value);
                    isRuleMissing = false;
                } else {
                    rule.to.forEach(function (to) {
                        if (_.has(rules[rule.from], to)) {
                            value = rules[rule.from][to];

                            if (isConstraint(value)) {
                                constraints.push(value);
                                isRuleMissing = false;
                            }
                        }
                    });
                }
            }

            if (isRuleMissing && rule.from != '*') {
                constraints.push(false);
            }
        });

        return constraints;
    };

    Constraints.prototype.check = function (source, target) {
        var constraints = this.getRelevantConstraints(source, target);
        var result = false;

        for (var i = 0; i < constraints.length; i++) {
            if (this.evaluate(constraints[i], source, target)) {
                result = true;
            } else {
                return false;
            }
        }

        return result;
    };

    AGN.Lib.WM.ConnectionConstraints = Constraints;
})();
