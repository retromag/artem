const rateElements = document.querySelectorAll('.js-set-rate');
const rateInput = document.querySelectorAll('.js-value-rate');
console.log('rateElements', rateElements);
console.log('rateInput', rateInput);

rateElements.forEach((rate) => {
    rate.addEventListener('click', () => {
        console.log('rate', rate);
        console.log('rate.getAttribute(\'data-coin-name\')', rate.getAttribute('data-rate'));
        console.log(rateInput);
        const value = rate.getAttribute('data-rate');
        rateInput.value ='hybhjnk';
    })
})