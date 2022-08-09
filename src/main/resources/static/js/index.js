const dropdownTop = document.querySelector('.js-top-dropdown');
const headerDropdownTop = document.querySelector('.js-top-dropdown-header');
const dropdownOptionTop = document.querySelectorAll('.js-top-dropdown-option');
const coinNameHeaderTop = document.querySelector('.js-top-dropdown-title');
const mainCoinImgTop = document.querySelector('.js-top-main-coin-image');
const mainCoinAbbrTop = document.querySelector('.js-top-main-coin-abbr');


const headerDropdownBottom = document.querySelector('.js-bottom-dropdown-header');
const dropdownBottom = document.querySelector('.js-bottom-dropdown');
const dropdownOptionBottom = document.querySelectorAll('.js-bottom-dropdown-option');
const coinNameHeaderBottom = document.querySelector('.js-bottom-dropdown-title');
const mainCoinImgBottom = document.querySelector('.js-bottom-main-coin-image');
const mainCoinAbbrBottom = document.querySelector('.js-bottom-main-coin-abbr');

const rateElement = document.querySelector('.js-rate');

const topInput = document.querySelector('.js-top-input');
const bottomInput = document.querySelector('.js-bottom-input');

/////
const testInputTop = document.querySelector('.test_input-top');
const testInputBottom = document.querySelector('.test_input-bottom');
/////
testInputTop.value = mainCoinAbbrTop.textContent;
testInputBottom.value = mainCoinAbbrBottom.textContent;
const removeItemFromDropdown = (dropdown, coinName) => {
    console.log('coinName', coinName);
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
const coinNameHeaderTopTextContent = coinNameHeaderTop.textContent;
removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTopTextContent);
const coinNameHeaderBottomTextContent = coinNameHeaderBottom.textContent;
removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottomTextContent);
const getCourse = (firstSymbol, secondSymbol) => {
    fetch(`http://localhost:8080/api/app/get/price/?firstSymbol=${firstSymbol}&secondSymbol=${secondSymbol}`)
        .then(response => {
            return response.json();
        }).then(data => {
        rateElement.textContent = `1 ${firstSymbol} - ${data} ${secondSymbol}`;
    });
}
getCourse(mainCoinAbbrTop.textContent, mainCoinAbbrBottom.textContent);

topInput.addEventListener('input', () => {
    const topInputValue = topInput.value;

    if (topInputValue !== '') {
        fetch(`http://localhost:8080/api/app/get/taken/?amount=${topInputValue}&firstSymbol=${mainCoinAbbrTop.textContent}&secondSymbol=${mainCoinAbbrBottom.textContent}`)
            .then(response => {
                return response.json();
            }).then(data => {
            bottomInput.value = data;
        });
    } else {
        bottomInput.value = '';
    }
})


dropdownOptionTop.forEach((option) => {
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgTop, coinNameHeaderTop, mainCoinAbbrTop, testInputBottom);
        testInputTop.value = currentCoinAbbr;
        //removing item from other dropdown
        dropdownTop.classList.remove('opened');
        const coinNameHeaderTopTextContent = coinNameHeaderTop.textContent;
        removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTopTextContent);

        //calculating course
        getCourse(currentCoinAbbr, mainCoinAbbrBottom.textContent);
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
        const coinNameHeaderBottomTextContent = coinNameHeaderBottom.textContent;
        removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottomTextContent);

        //calculating course
        getCourse(mainCoinAbbrTop.textContent, currentCoinAbbr);
    });
})

headerDropdownBottom.addEventListener('click', () => {
    dropdownTop.classList.remove('opened');
    dropdownBottom.classList.toggle('opened');
});

const buttonsExchangeShortcut = document.querySelectorAll('.js-shortcut-btn-exchange');

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

        //set for fropdown header value

        console.log('coinNameFrom', coinNameFrom);
        console.log('coinNameTo', coinNameTo);

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
    })
});

(function() {
    'use strict';

    $('.input-file').each(function() {
        var $input = $(this),
            $label = $input.next('.js-labelFile'),
            labelVal = $label.html();

        $input.on('change', function(element) {
            var fileName = '';
            if (element.target.value) fileName = element.target.value.split('\\').pop();
            fileName ? $label.addClass('has-file').find('.js-fileName').html(fileName) : $label.removeClass('has-file').html(labelVal);
        });
    });

})();