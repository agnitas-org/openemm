const connect = require('gulp-connect');
const config = require("./config");

module.exports = () => {
  connect.server({
    root: [`${config.docs}/styleguide`, `${config.docs}/styleguide/assets`],
    port: 3333
  });
};
