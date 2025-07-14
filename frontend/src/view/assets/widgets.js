const script = document.querySelector('script[src$="/widgets.js"]');

const url = new URL(script.src);
let baseUrl = url.origin;

const pathParts = url.pathname.split('/');

if (pathParts.length !== 3) {
  baseUrl += pathParts.slice(0, -2).join('/');
}

window.setInterval(() => {
  document.querySelectorAll('.emm-widget:not(.emm-widget-rendered)')
    .forEach(widget => {
      const token = widget.dataset.emmWidget;
      if (!token) {
        throw new Error('Widget token missing!');
      }

      widget.innerHTML = `<iframe src="${baseUrl}/widget.action?token=${encodeURIComponent(token)}"></iframe>`;
      widget.classList.add('emm-widget-rendered')
    });
}, 2000);