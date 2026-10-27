document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
      const message = form.dataset.confirm || 'Bạn có chắc chắn muốn thực hiện thao tác này?';
      if (!window.confirm(message)) event.preventDefault();
    });
  });
  document.querySelectorAll('[data-auto-dismiss]').forEach((alert) => {
    window.setTimeout(() => bootstrap.Alert.getOrCreateInstance(alert).close(), 4500);
  });
});
