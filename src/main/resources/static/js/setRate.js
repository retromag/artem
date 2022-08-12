const rateElements = document.querySelectorAll('.js-set-rate');
const rateInput = document.querySelector('.js-value-rate');

rateElements.forEach((rate) => {
    rate.addEventListener('click', () => {
        rateInput.value = rate.getAttribute('data-rate');
    })
})