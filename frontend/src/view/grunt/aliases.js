module.exports = {
  "default": ['serve'],
  "serve": ['build', 'focus:dev'],
  "serve_vagrant": ['build', 'focus:vagrant'],
  "uglify:build": ['uglify:build_js', 'uglify:build_birtjs'],
  "uglify:compile": ['uglify:compile_js', 'uglify:compile_birtjs'],
  "build": ['sass:build', 'uglify:build_js', 'uglify:build_birtjs', 'svg_sprite'],
  "compile": ['svg_sprite', 'sass:build', 'cssmin:compile', 'uglify:compile', 'usebanner:css', 'usebanner:js', 'usebanner:birtjs'],
  "styleguide": ['svg_sprite', 'uglify:build', 'sass:build', 'clean:styleguide','copy:assets_for_styleguide', 'shell:styleguide', 'clean:styleguide_source_assets'],
  "docs": ['connect:docs:keepalive'],
  "compile_js": ['compile_js_classic', 'compile_js_redesigned'],
  "compile_js_classic": ['uglify:build_js', 'uglify:compile_js', 'usebanner:js'],
  "compile_js_redesigned": ['uglify:build_js_redesigned', 'uglify:compile_js_redesigned', 'usebanner:js_redesigned'],
  "compile_birtjs": ['uglify:build_birtjs', 'uglify:compile_birtjs', 'usebanner:birtjs'],
  "compile_css": ['sass:build', 'cssmin:compile', 'usebanner:css', 'compile_forms_css'],
  "compile_css_redesigned": ['sass:build_redesigned', 'cssmin:compile_redesigned', 'usebanner:css_redesigned'],
  "compile_landing_css": ['sass:build_landing', 'cssmin:compile_landing', 'usebanner:landing'],
  "compile_forms_css": ['sass:build_formcss']
};
