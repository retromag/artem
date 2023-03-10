// const env = 'http://localhost:8080';
// const env = 'https://cryptocurrency-exchanger.herokuapp.com';
const env = 'https://hungry-swap.com';


//top dropdown elements
const dropdownTop = document.querySelector('.js-top-dropdown');
const headerDropdownTop = document.querySelector('.js-top-dropdown-header');
const dropdownOptionTop = document.querySelectorAll('.js-top-dropdown-option');
const coinNameHeaderTop = document.querySelector('.js-top-dropdown-title');
const mainCoinImgTop = document.querySelector('.js-top-main-coin-image');
const mainCoinAbbrTop = document.querySelector('.js-top-main-coin-abbr');
const topInput = document.querySelector('.js-top-input');
// elements for min and max amounts of coins for top dropdown
const minCoinAmount = document.querySelector('.js-min-coins-amount');
const maxCoinAmount = document.querySelector('.js-max-coins-amount');

//bottom dropdown elements
const headerDropdownBottom = document.querySelector('.js-bottom-dropdown-header');
const dropdownBottom = document.querySelector('.js-bottom-dropdown');
const dropdownOptionBottom = document.querySelectorAll('.js-bottom-dropdown-option');
const coinNameHeaderBottom = document.querySelector('.js-bottom-dropdown-title');
const mainCoinImgBottom = document.querySelector('.js-bottom-main-coin-image');
const mainCoinAbbrBottom = document.querySelector('.js-bottom-main-coin-abbr');
const bottomInput = document.querySelector('.js-bottom-input');

//rate element in bottom dropdown
const rateElement = document.querySelector('.js-rate');
const hiddenInputRateElement = document.querySelector('.js-hidden-input-rate');

//buttons exchange in shortcuts
const buttonsExchangeShortcut = document.querySelectorAll('.js-shortcut-btn-exchange');

/////
const testInputTop = document.querySelector('.js-hidden_input-top');
const testInputBottom = document.querySelector('.js-hidden_input-bottom');
/////

// elements: label and input for changing information about according to currency
const labelWallet = document.querySelector('.wallet_label');
const inputWallet = document.querySelector('#props_wallet');

//
const emailInput = document.querySelector('#props_email');
const exchangeBtn = document.querySelector('.js-btn-exchange');

console.log('emailInput',emailInput);

//data attributes of inputs with full short name
// const dataFullTopAbbr = mainCoinAbbrBottom..getAttribute("data-value");
const removeItemFromDropdown = (dropdown, coinName) => {
    dropdown.forEach((option) => {
        option.classList.remove('hidden');
        if(option.querySelector('[data-coin-name]').textContent === coinName) {
            option.classList.add('hidden');
        }
    })
}

const setCoinInHeader = (option, imgHeader, coinNameHeader, coinAbbrHeader, testInput,dataValueAbbr) => {
    topInput.value = '';
    bottomInput.value = '';
    const coinName = option.querySelector('[data-coin-name]').textContent;
    let coinAbbr = option.querySelector('[data-coin-abbr]').getAttribute('data-coin-abbr');
    const coinSrcImg = option.querySelector('[data-coin-img]').getAttribute('src');
    coinAbbrHeader.setAttribute(dataValueAbbr, coinAbbr);

    const coinAbbrFullData = coinAbbrHeader.getAttribute(dataValueAbbr);

        imgHeader.setAttribute('src', `${coinSrcImg}`);

    if (coinAbbrFullData.startsWith('UAH') ||
        coinAbbrFullData.startsWith('RUB') ||
        coinAbbrFullData.startsWith('PLN') ||
        coinAbbrFullData.startsWith('EUR') ||
        (coinAbbrFullData.startsWith('USD')  &&
        coinAbbrFullData !== 'USDT')
    ) {
        let shortCoinAbbr = coinAbbr.substring(0, 3);
        coinAbbrHeader.textContent = shortCoinAbbr;
    } else {
        coinAbbrHeader.textContent = coinAbbr;
    }

    coinNameHeader.textContent = coinName;

    return coinAbbr;
}

//get course and set in rate element
const getCourse = async (firstSymbol, secondSymbol) => {
    const response = await fetch(`${env}/api/app/get/price/?firstSymbol=${firstSymbol}&secondSymbol=${secondSymbol}`);
    const data = await response.json();
    hiddenInputRateElement.value = `1 ${firstSymbol} - ${data} ${secondSymbol}`;
    if (firstSymbol.startsWith('UAH') ||
        firstSymbol.startsWith('RUB') ||
        firstSymbol.startsWith('PLN') ||
        firstSymbol.startsWith('EUR') ||
        firstSymbol.startsWith('USD') && firstSymbol !== 'USDT'
    ) {
        firstSymbol = firstSymbol.substring(0, 3);
    }

    if (secondSymbol.startsWith('UAH') ||
        secondSymbol.startsWith('RUB') ||
        secondSymbol.startsWith('PLN') ||
        secondSymbol.startsWith('EUR') ||
        secondSymbol.startsWith('USD') && secondSymbol !== 'USDT'
    ) {
        secondSymbol = secondSymbol.substring(0, 3);
    }

    rateElement.textContent = `1 ${firstSymbol} - ${data} ${secondSymbol}`;



}
//get min and max amount of coins and set them to appropriate element
const getMinAndMaxAmountOfCoins = async (coinAbbr) => {
    const responseMinAmount = await fetch(`${env}/api/coin/min/amount/?symbol=${coinAbbr}`);
    const dataMinAmount = await responseMinAmount.json();


    const responseMaxAmount = await fetch(`${env}/api/coin/max/amount/?symbol=${coinAbbr}`);
    const dataMaxAmount = await responseMaxAmount.json();

    if (coinAbbr.startsWith('UAH') ||
        coinAbbr.startsWith('RUB') ||
        coinAbbr.startsWith('PLN') ||
        coinAbbr.startsWith('EUR') ||
        coinAbbr.startsWith('USD') &&
        coinAbbr !== 'USDT'
    ) {
        coinAbbr = coinAbbr.substring(0, 3);
    }
    minCoinAmount.textContent = `${dataMinAmount} ${coinAbbr}`;
    maxCoinAmount.textContent = `${dataMaxAmount} ${coinAbbr}`;

}

testInputTop.value = mainCoinAbbrTop.textContent;
testInputBottom.value = mainCoinAbbrBottom.textContent;

removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTop.textContent);
removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottom.textContent);

getCourse(mainCoinAbbrTop.textContent, mainCoinAbbrBottom.textContent);
getMinAndMaxAmountOfCoins(mainCoinAbbrTop.getAttribute('data-top-abbr'));

topInput.addEventListener('input', async () => {
    if (topInput.value !== '') {
        const response = await fetch(`${env}/api/app/get/taken/?amount=${+topInput.value}&firstSymbol=${mainCoinAbbrTop.getAttribute('data-top-abbr')}&secondSymbol=${mainCoinAbbrBottom.getAttribute('data-bottom-abbr')}`);
        const data = await response.json();
        bottomInput.value = data;
        if (topInput.value === '') {
            bottomInput.value = '';
        }
    } else {
        bottomInput.value = '';
    }
});

bottomInput.addEventListener('input', async () => {
    if (bottomInput.value !== '') {
        const response = await fetch(`${env}/api/app/get/given/?amount=${+bottomInput.value}&firstSymbol=${mainCoinAbbrBottom.getAttribute('data-bottom-abbr')}&secondSymbol=${mainCoinAbbrTop.getAttribute('data-top-abbr')}`);
        const data = await response.json();
        topInput.value = data;
        if (bottomInput.value === '') {
            topInput.value = '';
        }
    } else {
        topInput.value = '';
    }
})


dropdownOptionTop.forEach((option) => {
    let coinAbbrInOption = option.querySelector('.dropdown_coin_abbr');
    if (coinAbbrInOption.textContent.startsWith('UAH') ||
        coinAbbrInOption.textContent.startsWith('RUB') ||
        coinAbbrInOption.textContent.startsWith('PLN') ||
        coinAbbrInOption.textContent.startsWith('EUR') ||
        coinAbbrInOption.textContent.startsWith('USD') &&
        coinAbbrInOption.textContent !== 'USDT'
    ) {
        coinAbbrInOption.textContent = coinAbbrInOption.textContent.substring(0, 3);
    }

    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgTop, coinNameHeaderTop, mainCoinAbbrTop, testInputBottom, 'data-top-abbr');
        testInputTop.value = currentCoinAbbr;

        //removing item from other dropdown
        dropdownTop.classList.remove('opened');
        removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTop.textContent);

        //calculating course
        // if ()
        getCourse(currentCoinAbbr, mainCoinAbbrBottom.textContent);

        //get and set min and max coins value
        getMinAndMaxAmountOfCoins(currentCoinAbbr);
    });
})

headerDropdownTop.addEventListener('click', () => {
    dropdownBottom.classList.remove('opened');
    dropdownTop.classList.toggle('opened');
});

dropdownOptionBottom.forEach((option) => {
    let coinAbbrInOption = option.querySelector('.dropdown_coin_abbr');
    if (coinAbbrInOption.textContent.startsWith('UAH') ||
        coinAbbrInOption.textContent.startsWith('RUB') ||
        coinAbbrInOption.textContent.startsWith('PLN') ||
        coinAbbrInOption.textContent.startsWith('EUR') ||
        coinAbbrInOption.textContent.startsWith('USD') &&
        coinAbbrInOption.textContent !== 'USDT'
    ) {
        coinAbbrInOption.textContent = coinAbbrInOption.textContent.substring(0, 3);
    }
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgBottom, coinNameHeaderBottom, mainCoinAbbrBottom, testInputTop, 'data-bottom-abbr');
        testInputBottom.value = currentCoinAbbr;

        if (currentCoinAbbr.startsWith('UAH') ||
            currentCoinAbbr.startsWith('RUB') ||
            currentCoinAbbr.startsWith('PLN') ||
            currentCoinAbbr.startsWith('EUR') ||
            currentCoinAbbr.startsWith('USD') &&
            currentCoinAbbr !== 'USDT'
        ) {
            inputWallet.placeholder = '5610591081018250';
        } else {
            inputWallet.placeholder = '0x8a7E45F7c6723f644EeA5E0Cc45B70F27D9b812e';
        }

        //removing item from dropdown
        dropdownBottom.classList.remove('opened');
        removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottom.textContent);

        //calculating course
        getCourse(mainCoinAbbrTop.textContent, currentCoinAbbr);

        //get and set min and max coins value
        getMinAndMaxAmountOfCoins(mainCoinAbbrTop.getAttribute('data-top-abbr'));
    });
})

headerDropdownBottom.addEventListener('click', () => {
    dropdownTop.classList.remove('opened');
    dropdownBottom.classList.toggle('opened');
});

buttonsExchangeShortcut.forEach((button) => {

    button.addEventListener('click', (event) => {
        // event.preventDefault();
        const coinAbbrFrom = button.getAttribute('data-shortcut-from');
        const coinAbbrTo = button.getAttribute('data-shortcut-to');

        const currentItem = button.closest('.js-shortcut_item');

        const coinImgSrcFrom = currentItem.querySelector('.js-coin-from').getAttribute('src');
        const coinImgSrcTo = currentItem.querySelector('.js-coin-to').getAttribute('src');
        const coinNameFrom = currentItem.querySelector('.js-money-from').textContent;
        const coinNameTo = currentItem.querySelector('.js-money-to').textContent;

        //set for dropdown header value

        testInputTop.value = coinAbbrFrom;
        testInputBottom.value = coinAbbrTo;

        coinNameHeaderTop.textContent = coinNameFrom;
        coinNameHeaderBottom.textContent = coinNameTo;

        mainCoinImgTop.src = coinImgSrcFrom;
        mainCoinImgBottom.src = coinImgSrcTo;

        mainCoinAbbrTop.textContent = coinAbbrFrom;
        mainCoinAbbrBottom.textContent = coinAbbrTo;

        //removing from dropdowns selected options
        removeItemFromDropdown(dropdownOptionTop, coinNameTo);
        removeItemFromDropdown(dropdownOptionBottom, coinNameFrom);

        //get course
        getCourse(coinAbbrFrom, coinAbbrTo);

        //get and set min and max coins value
        getMinAndMaxAmountOfCoins(coinAbbrFrom);

        topInput.value = '';
        bottomInput.value = '';
    })
});

const validateInputs = () => {
    if (topInput.value === '' ||
        bottomInput.value === '' ||
        emailInput.value === '' ||
        inputWallet.value === ''
    ) {
        exchangeBtn.disabled = true;
    } else {
        exchangeBtn.disabled = false;
    }
}
validateInputs();
topInput.addEventListener('input', () => {
    validateInputs();
})
bottomInput.addEventListener('input', () => {
    validateInputs();
})
emailInput.addEventListener('input', () => {
    validateInputs();
})
inputWallet.addEventListener('input', () => {
    validateInputs();
})