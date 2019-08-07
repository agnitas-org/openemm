module.exports = {
  js: {
    files: ['<%= config.assets %>/js/**/*.js'],
    tasks: ['concat:build']
  },
  js_with_sync: {
    files: ['<%= config.assets %>/js/**/*.js'],
    tasks: ['concat:build', 'shell:sync_js']
  },
  sass: {
    files: ['<%= config.assets %>/sass/**/*.scss'],
    tasks: ['sass:build']
  },
  sass_with_sync: {
    files: ['<%= config.assets %>/sass/**/*.scss'],
    tasks: ['sass:build', 'shell:sync_sass']
  },
  assets: {
    files: ['<%= config.assets %>/core/images/**/*'],
    tasks: ['svg_sprite']
  },
  assets_with_sync: {
    files: ['<%= config.assets %>/core/images/**/*'],
    tasks: ['svg_sprite', 'shell:sync_assets']
  },
  webinf_with_sync:{
    files: ['<%= config.webinf %>/**/*'],
    tasks: ['shell:sync_webinf'],
  },
  grunt: {
    files: ['Gruntfile.js', 'grunt/*.js']
  },
  view: {
    files: '<%= config.jsp %>/**/*.jsp',
    tasks: ['shell:sync_jsp']
  }
}
