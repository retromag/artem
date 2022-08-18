// const env = 'http://localhost:8080';
// const env = 'https://helmetswap.herokuapp.com';
const env = 'https://helmet-swap.com';

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
//
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

const removeItemFromDropdown = (dropdown, coinName) => {
    dropdown.forEach((option) => {
        option.classList.remove('hidden');
        if(option.querySelector('[data-coin-name]').textContent === coinName) {
            option.classList.add('hidden');
        }
    })
}

const setCoinInHeader = (option, imgHeader, coinNameHeader, coinAbbrHeader, testInput) => {
    topInput.value = '';
    bottomInput.value = '';
    const coinName = option.querySelector('[data-coin-name]').textContent;
    const coinAbbr = option.querySelector('[data-coin-abbr]').textContent;
    const coinSrcImg = option.querySelector('[data-coin-img]').getAttribute('src');

    imgHeader.setAttribute('src', `${coinSrcImg}`);
    coinNameHeader.textContent = coinName;
    coinAbbrHeader.textContent = coinAbbr;

    return coinAbbr;
}

//get course and set in rate element
const getCourse = async (firstSymbol, secondSymbol) => {
    const response = await fetch(`${env}/api/app/get/price/?firstSymbol=${firstSymbol}&secondSymbol=${secondSymbol}`);
    const data = await response.json();
    rateElement.textContent = `1 ${firstSymbol} - ${data} ${secondSymbol}`;
    hiddenInputRateElement.value = `1 ${firstSymbol} - ${data} ${secondSymbol}`;
}
//get min and max amount of coins and set them to appropriate element
const getMinAndMaxAmountOfCoins = async (coinAbbr) => {
    const responseMinAmount = await fetch(`${env}/api/coin/min/amount/?symbol=${coinAbbr}`);
    const dataMinAmount = await responseMinAmount.json();
    minCoinAmount.textContent = `${dataMinAmount} ${coinAbbr}`;

    const responseMaxAmount = await fetch(`${env}/api/coin/max/amount/?symbol=${coinAbbr}`);
    const dataMaxAmount = await responseMaxAmount.json();
    maxCoinAmount.textContent = `${dataMaxAmount} ${coinAbbr}`;
}

testInputTop.value = mainCoinAbbrTop.textContent;
testInputBottom.value = mainCoinAbbrBottom.textContent;

removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTop.textContent);
removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottom.textContent);

getCourse(mainCoinAbbrTop.textContent, mainCoinAbbrBottom.textContent);
getMinAndMaxAmountOfCoins(mainCoinAbbrTop.textContent);

topInput.addEventListener('input', async () => {
    // topInput.value = '';
    if (topInput.value !== '') {
        const response = await fetch(`${env}/api/app/get/taken/?amount=${+topInput.value}&firstSymbol=${mainCoinAbbrTop.textContent}&secondSymbol=${mainCoinAbbrBottom.textContent}`);
        const data = await response.json();
        console.log('data from topInput', topInput.value);
        console.log('data top', data);
        bottomInput.value = data;
        if (topInput.value === '') {
            bottomInput.value = '';
        }
    } else {
        console.log('bootom input val = 0', topInput.value);
        bottomInput.value = '';
    }
});

bottomInput.addEventListener('input', async () => {
    if (bottomInput.value !== '') {
        const response = await fetch(`${env}/api/app/get/given/?amount=${+bottomInput.value}&firstSymbol=${mainCoinAbbrBottom.textContent}&secondSymbol=${mainCoinAbbrTop.textContent}`);
        const data = await response.json();
        console.log('data from bottomInput', bottomInput.value);
        console.log('data bottom', data);
        topInput.value = data;
        if (bottomInput.value === '') {
            topInput.value = '';
        }
    } else {
        topInput.value = '';
    }
})


dropdownOptionTop.forEach((option) => {
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgTop, coinNameHeaderTop, mainCoinAbbrTop, testInputBottom);
        testInputTop.value = currentCoinAbbr;

        //removing item from other dropdown
        dropdownTop.classList.remove('opened');
        removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTop.textContent);

        //calculating course
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
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgBottom, coinNameHeaderBottom, mainCoinAbbrBottom, testInputTop);
        testInputBottom.value = currentCoinAbbr;

        //removing item from dropdown
        dropdownBottom.classList.remove('opened');
        removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottom.textContent);

        //calculating course
        getCourse(mainCoinAbbrTop.textContent, currentCoinAbbr);

        //get and set min and max coins value
        getMinAndMaxAmountOfCoins(mainCoinAbbrTop.textContent);
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
