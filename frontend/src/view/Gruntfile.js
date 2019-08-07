module.exports = function(grunt) {
  'use strict';

  require('time-grunt')(grunt);

  var path = require('path');

  require('load-grunt-config')(grunt, {
      configPath: path.join(process.cwd(), 'grunt'),
      init: true,
      data: {
        config: grunt.file.readJSON('grunt_config.json')
      }
  });

};
