module.exports = {
    docs: {
        options: {
            port: 3333,
            // That's a temporary trick to expose /assets directory so the application.css is available at / path.
            base: ['../../docs/styleguide', '../../docs/styleguide/assets']
        }
    }
}
