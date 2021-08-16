(function() {
    AGN.Lib.ContextMenu = {
        create: function(params) {
            $.each(params, function(selector, config) {
                $.contextMenu({
                    selector: selector,
                    build: function($element, event) {
                        return build(config, $element, event);
                    }
                });
            });
        }
    };

    function build(config, $element, event) {
        if (config.shown === false || _.isFunction(config.shown) && config.shown($element, event) === false) {
            return false;
        }

        var definitions = config.items;

        if (_.isFunction(definitions)) {
            definitions = definitions($element, event);
        }

        var items = {};
        var count = 0;

        $.each(definitions, function(key, options) {
            if (!_.isFunction(options.shown) || options.shown($element, event) !== false) {
                var item = items[key] = {
                    name: options.name,
                    icon: options.icon
                };

                if (_.isFunction(options.disabled)) {
                    item.disabled = options.disabled($element, event) === true;
                } else if (options.disabled === true) {
                    item.disabled = true;
                }

                if (_.isFunction(options.clicked)) {
                    item.callback = function(k, opts) {
                        options.clicked($element, k, opts);
                    };
                }

                count++;
            }
        });

        return count ? {items: items} : false;
    }
})();
