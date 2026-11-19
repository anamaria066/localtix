// US-11: shows the card number in 4 groups of 4 digits ("4111 1111 1111 1111") while typing.
// Only the display changes: PaymentService removes the spaces before validating the number.
(function () {
    const input = document.getElementById('cardNumber');
    if (!input) {
        return;
    }
    const MAX_DIGITS = 16;
    const GROUP_SIZE = 4;

    function format(value) {
        const digits = value.replace(/\D/g, '').substring(0, MAX_DIGITS);
        const groups = digits.match(new RegExp('.{1,' + GROUP_SIZE + '}', 'g'));
        return groups ? groups.join(' ') : '';
    }

    input.addEventListener('input', function () {
        input.value = format(input.value);
    });

    // The number kept after a failed payment is formatted too
    input.value = format(input.value);
})();
