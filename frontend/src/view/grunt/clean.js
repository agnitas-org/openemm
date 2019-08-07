module.exports = {
  styleguide: {
    src: ['<%= config.docs %>/styleguide/**'],
    options: {
      force: true
    }
  },
  styleguide_source_assets: {
    src: ['<%= config.docs %>/src/assets/**'],
    options: {
      force: true
    }
  }
}
