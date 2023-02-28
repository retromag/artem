const coinsAmountName = document.querySelectorAll('.js-coin-amount');
console.log('works', coinsAmountName);

coinsAmountName.forEach((coin) => {
    if (coin.textContent.startsWith('UAH') ||
        coin.textContent.startsWith('RUB') ||
        coin.textContent.startsWith('PLN') ||
        coin.textContent.startsWith('EUR') ||
        coin.textContent.startsWith('USD') &&
        coin.textContent !== 'USDT'
    ) {
        coin.textContent = coin.textContent.substring(0, 3);
    }
})

