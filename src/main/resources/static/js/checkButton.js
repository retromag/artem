const btnRegister = document.querySelector('.js-btn-registration');
const tumbler = document.querySelector('.js-tumbler');
btnRegister.disabled = true;

tumbler.addEventListener('change', () => {
    if (!tumbler.checked) {
        btnRegister.disabled = true;
    } else if (tumbler.checked) {
        btnRegister.disabled = false;
    }
})

