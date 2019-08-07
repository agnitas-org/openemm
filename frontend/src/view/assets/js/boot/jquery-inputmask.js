$.extend(true, $.inputmask.defaults.aliases, {
  'datetime': {
    regex: {
      quartspre: new RegExp("0|1|3|4"),
      quartspost: new RegExp("00|15|30|45"),
      halfspre: new RegExp("0|3"),
      halfspost: new RegExp("00|30"),
    },
    definitions: {
      'q': {
        "validator": function (chrs, maskset, pos, strict, opts) {
            var isValid = opts.regex.quartspost.test(chrs);

            if (!strict && !isValid) {
              if (opts.regex.quartspost.test(chrs[0] + "5")) {
                return {
                  c: "5"
                }
              } else {
                return {
                  c: "0"
                }
              }
            }

            return true
        },
        "cardinality": 2,
        "prevalidator": [{
          validator: function (chrs, maskset, pos, strict, opts) {
            var isValid = opts.regex.quartspre.test(chrs);

            isValid = false;
            if (!strict && !isValid) {
              var completedVal = parseInt(chrs + "0"),
                acceptableVals = ["00", "15", "30", "45"],
                diffVal = 100,
                acceptedVal,
                command;

              for(var i = 0; i < acceptableVals.length; i++) {
                var num = acceptableVals[i];
                    parsedNum = parseInt(num)

                if (Math.abs(parsedNum - completedVal) < diffVal) {
                  diffVal = Math.abs(parsedNum - completedVal)
                  acceptedVal = num
                }
              }

              command = {
                caret: pos+2,
                insert: [{
                  "pos": pos,
                  "c":  acceptedVal[0]
                },
                {
                  "pos": pos+1,
                  "c": acceptedVal[1]
                }]
              }

              return command;
            }
            return true;
          }, cardinality: 1
        }]
      },
      'u': {
        "validator": function (chrs, maskset, pos, strict, opts) {
            var isValid = opts.regex.halfspost.test(chrs);

            if (!strict && !isValid) {
              return {
                c: "0"
              }
            }

            return true
        },
        "cardinality": 2,
        "prevalidator": [{
          validator: function (chrs, maskset, pos, strict, opts) {
            var isValid = opts.regex.halfspre.test(chrs);

            isValid = false;
            if (!strict && !isValid) {
              var completedVal = parseInt(chrs + "0"),
                acceptableVals = ["00", "30"],
                diffVal = 100,
                acceptedVal,
                command;

              for(var i = 0; i < acceptableVals.length; i++) {
                var num = acceptableVals[i];
                    parsedNum = parseInt(num)

                if (Math.abs(parsedNum - completedVal) < diffVal) {
                  diffVal = Math.abs(parsedNum - completedVal)
                  acceptedVal = num
                }
              }

              command = {
                caret: pos+2,
                insert: [{
                  "pos": pos,
                  "c":  acceptedVal[0]
                },
                {
                  "pos": pos+1,
                  "c": acceptedVal[1]
                }]
              }

              return command;
            }
            return true;
          }, cardinality: 1
        }]
      }
    }
  },
  'h:quarts t': {
    mask: "h:q t\\m",
    placeholder: "hh:mm xm",
    alias: "datetime",
    hourFormat: "12"
  },
  'h:quarts': {
    mask: "h:q",
    placeholder: "hh:mm",
    alias: "datetime",
    hourFormat: "24"
  },
  'h:halfs t': {
    mask: "h:u t\\m",
    placeholder: "hh:mm xm",
    alias: "datetime",
    hourFormat: "12"
  },
  'h:halfs': {
    mask: "h:u",
    placeholder: "hh:mm",
    alias: "datetime",
    hourFormat: "24"
  },
  'h:00 t': {
    mask: "h:00 t\\m",
    placeholder: "hh:00 xm",
    alias: "datetime",
    hourFormat: "12"
  },
  'h:00': {
    mask: "h:00",
    placeholder: "hh:00",
    alias: "datetime",
    hourFormat: "24"
  }
});
