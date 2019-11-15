module.exports = {
  "default": ['serve'],
  "serve": ['build', 'focus:dev'],
  "serve_vagrant": ['build', 'focus:vagrant'],
  "uglify:build": ['uglify:build_js', 'uglify:build_birtjs'],
  "uglify:compile": ['uglify:compile_js', 'uglify:compile_birtjs'],
  "build": ['sass:build', 'concat:build', 'svg_sprite'],
  "compile": ['svg_sprite', 'sass:build', 'cssmin:compile', 'uglify:compile', 'usebanner:css', 'usebanner:js', 'usebanner:birtjs'],
  "styleguide": ['svg_sprite', 'uglify:build', 'sass:build', 'clean:styleguide','copy:assets_for_styleguide', 'shell:styleguide', 'clean:styleguide_source_assets'],
  "compile_js": ['uglify:build_js', 'uglify:compile_js', 'usebanner:js'],
  "compile_birtjs": ['uglify:build_birtjs', 'uglify:compile_birtjs', 'usebanner:birtjs'],
  "compile_css": ['sass:build', 'cssmin:compile', 'usebanner:css']
};
