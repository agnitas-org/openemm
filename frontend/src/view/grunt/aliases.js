module.exports = {
  "default": ['serve'],
  "serve": ['build', 'focus:dev'],
  "serve_vagrant": ['build', 'focus:vagrant'],
  "build": ['sass:build', 'concat:build', 'svg_sprite'],
  "compile": ['svg_sprite', 'sass:build', 'cssmin:compile', 'uglify:compile', 'usebanner:css', 'usebanner:js', 'usebanner:birtjs'],
  "styleguide": ['svg_sprite', 'uglify:build', 'sass:build', 'clean:styleguide','copy:assets_for_styleguide', 'shell:styleguide', 'clean:styleguide_source_assets']
}
