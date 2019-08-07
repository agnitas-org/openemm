module.exports = {
  sync_jsp: {
    command: 'vagrant ssh -c \'rsync -rav --include="*.jsp" --include "*/*.jsp" --exclude="node_modules" --exclude="assets" /vagrant/src/view/ <%= config.tomcat %>\''
  },
  sync_js: {
    command: 'vagrant ssh -c \'rsync -av --include="*.js" /vagrant/src/view/<%= config.assets %>/ <%= config.tomcat %>/assets/\''
  },
  sync_sass: {
    command: 'vagrant ssh -c \'rsync -av --include="*.css" /vagrant/src/view/<%= config.assets %>/ <%= config.tomcat %>/assets/\''
  },
  sync_webinf: {
    command: 'vagrant ssh -c \'rsync -rav /vagrant/src/view/WEB-INF/ <%= config.tomcat %>/WEB-INF\''
  },
  sync_assets: {
    command: 'vagrant ssh -c \'rsync -rav --include="*.svg" --include="*.png" --include="*.jpg" /vagrant/src/view/<%= config.assets %>/ <%= config.tomcat %>/assets/\''
  },
  styleguide: {
    command: 'hologram -c \'<%= config.docs %>/src/hologram_config.yml\''
  }
}
