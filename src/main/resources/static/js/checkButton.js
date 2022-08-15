const btnRegister = document.querySelector('.js-btn-registration');
const tumblers = document.querySelectorAll('.js-tumbler');
btnRegister.disabled = true;
tumblers.forEach((tumbler) => {
    tumbler.addEventListener('change', () => {
        if (!tumblers[0].checked && !tumblers[1].checked) {
            btnRegister.disabled = true;
        } else if (!tumblers[0].checked || !tumblers[1].checked) {
            btnRegister.disabled = true;
        } else if (tumblers[0].checked && tumblers[1].checked) {
            btnRegister.disabled = false;
        }
    })
});

