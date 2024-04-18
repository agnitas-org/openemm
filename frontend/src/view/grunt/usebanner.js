module.exports = function(grunt){
  return {
    css: {
      files:  {
        src: ['<%= config.assets %>/application.min.css']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/application.css'),
        linebreak: true
      }
    },
    css_redesigned: {
      files:  {
        src: ['<%= config.assets %>/application.redesigned.min.css']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/application.css'),
        linebreak: true
      }
    },
    landing: {
      files:  {
        src: ['<%= config.assets %>/landing.min.css']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/application.css'),
        linebreak: true
      }
    },
    js: {
      files:  {
        src: ['<%= config.assets %>/application.min.js']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/application.js'),
        linebreak: true
      }
    },
    js_redesigned: {
      files:  {
        src: ['<%= config.assets %>/application.redesigned.min.js']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/application.js'),
        linebreak: true
      }
    },
    birtjs: {
      files:  {
        src: ['<%= config.assets %>/birt.min.js']
      },
      options: {
        position: 'top',
        banner: grunt.file.read('./assets/banner/birt.js'),
        linebreak: true
      }
    }
  }
}
