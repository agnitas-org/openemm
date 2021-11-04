(function () {

    AGN.Lib.FormBuilder = AGN.Lib.FormBuilder || {};

    AGN.Lib.FormBuilder.Utils = {
        pushControl: function (controls, options) {
            if (!Array.isArray(controls)) {
                return;
            }
            controls.push(function (superControlClass) {
                var NewControl = (function (controlClass) {
                    var superConstructor = controlClass.prototype.constructor;

                    function NewControl() {
                        return superConstructor.apply(this, arguments);
                    }

                    NewControl.prototype = Object.create(controlClass && controlClass.prototype, {
                        constructor: {value: NewControl, writable: true, configurable: true}
                    });

                    NewControl.__proto__ = controlClass;

                    if (options.build) {
                        NewControl.prototype.build = options.build;
                    }

                    if (options.onRender) {
                        NewControl.prototype.onRender = options.onRender;
                    }

                    Object.defineProperty(NewControl, 'definition', {get: options.definition});

                    return NewControl;
                })(superControlClass);

                // register this control for the following types & text subtypes
                superControlClass.register(options.newControlName, NewControl);
                return NewControl;
            });
        }
    };
})();
