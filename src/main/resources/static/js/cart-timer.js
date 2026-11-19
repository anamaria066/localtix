// US-9 AC3: countdown of the 10 minutes of reservation, shown on the cart and checkout pages.
// The element with id "cart-timer" has the seconds left (calculated by the server) in data-seconds.
(function () {
    const timer = document.getElementById('cart-timer');
    if (!timer) {
        return;
    }
    const WARNING_SECONDS = 60;
    let seconds = parseInt(timer.dataset.seconds, 10);

    function show() {
        const minutes = Math.floor(seconds / 60);
        const rest = seconds % 60;
        timer.textContent = minutes + ':' + String(rest).padStart(2, '0');
        timer.classList.toggle('lt-timer-warning', seconds <= WARNING_SECONDS);
    }

    show();
    const interval = setInterval(function () {
        seconds--;
        if (seconds <= 0) {
            clearInterval(interval);
            seconds = 0;
            show();
            // The seats are released: reload the cart so it shows what is still reserved
            window.location.href = timer.dataset.expiredUrl;
            return;
        }
        show();
    }, 1000);
})();
