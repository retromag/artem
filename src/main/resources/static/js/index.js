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

topInput.addEventListener('input', () => {
    const topInputValue = topInput.value;

    if (topInputValue !== '') {
        fetch(`http://localhost:8080/api/app/get/?amount=${topInputValue}&firstSymbol=${mainCoinAbbrTop.textContent}&secondSymbol=${mainCoinAbbrBottom.textContent}`)
            .then(response => {
                return response.json();
            }).then(data => {

            bottomInput.value = data;
        });
    } else {
        bottomInput.value = '';
    }
})

const removeItemFromDropdown = (dropdown, coinName) => {
    dropdown.forEach((option) => {
        option.classList.remove('hidden');
        if(option.querySelector('[data-coin-name]').textContent === coinName) {
            console.log('bottomOption', option.querySelector('[data-coin-name]').textContent);
            option.classList.add('hidden');
        }
    })
}

const setCoinInHeader = (option, imgHeader, coinNameHeader, coinAbbrHeader) => {
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

const searchQuery = [mainCoinAbbrTop.textContent, mainCoinAbbrBottom.textContent];
fetch(`https://api.binance.com/api/v1/exchangeInfo`)
    .then(response => {
        return response.json();
    }).then(data => {
    let res = '';
    data.symbols.forEach(el => {
        if (el.symbol === searchQuery.join('') || el.symbol === searchQuery.reverse().join('')) {
            res = el.symbol;

            fetch(`https://api.binance.com/api/v3/ticker/price?symbol=${res}`)
                .then(response => {
                    return response.json();
                }).then(data => {
                console.log('data btc', data);
                let firstPart = searchQuery[0];
                let secondPart = searchQuery[1];
                if (searchQuery.join('') !== data.symbol) {
                    firstPart = searchQuery[1];
                    secondPart = searchQuery[0];
                }
                rateElement.textContent = `1${firstPart} - ${data.price}${secondPart} `
            });

        }
        // else {
        //     fetch(`https://api.binance.com/api/v3/ticker/price?symbols=[${currentCoinAbbr}USDT,USDT${mainCoinAbbrBottom.textContent}]`)
        //         .then(response => {
        //             return response.json();
        //         }).then(data => {
        //         console.log('data btc usdt', data);
        //         // console.log('data btc', data.symbols);
        //     });
        // }
    })
})

dropdownOptionTop.forEach((option) => {
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgTop, coinNameHeaderTop, mainCoinAbbrTop);

        //removing item from other dropdown
        dropdownTop.classList.remove('opened');
        const coinNameHeaderTopTextContent = coinNameHeaderTop.textContent;
        removeItemFromDropdown(dropdownOptionBottom, coinNameHeaderTopTextContent);

        //calculating course

        const searchQuery = [currentCoinAbbr, mainCoinAbbrBottom.textContent];
        fetch(`https://api.binance.com/api/v1/exchangeInfo`)
            .then(response => {
                return response.json();
            }).then(data => {
            let res = '';
            data.symbols.forEach(el => {
                if (el.symbol === searchQuery.join('') || el.symbol === searchQuery.reverse().join('')) {
                    res = el.symbol;

                    fetch(`https://api.binance.com/api/v3/ticker/price?symbol=${res}`)
                        .then(response => {
                            return response.json();
                        }).then(data => {
                        console.log('data btc', data);
                        let firstPart = searchQuery[0];
                        let secondPart = searchQuery[1];
                        if (searchQuery.join('') !== data.symbol) {
                            firstPart = searchQuery[1];
                            secondPart = searchQuery[0];
                        }
                        rateElement.textContent = `1${firstPart} - ${data.price}${secondPart} `
                    });

                }
                // else {
                //     fetch(`https://api.binance.com/api/v3/ticker/price?symbols=[${currentCoinAbbr}USDT,USDT${mainCoinAbbrBottom.textContent}]`)
                //         .then(response => {
                //             return response.json();
                //         }).then(data => {
                //         console.log('data btc usdt', data);
                //         // console.log('data btc', data.symbols);
                //     });
                // }
            })
            })
    });
})

headerDropdownTop.addEventListener('click', () => {
    dropdownBottom.classList.remove('opened');
    dropdownTop.classList.toggle('opened');
});

dropdownOptionBottom.forEach((option) => {
    option.addEventListener('click', () => {
        //find and set coin from option to header
        const currentCoinAbbr = setCoinInHeader(option, mainCoinImgBottom, coinNameHeaderBottom, mainCoinAbbrBottom);

        //removing item from dropdown
        dropdownBottom.classList.remove('opened');
        const coinNameHeaderBottomTextContent = coinNameHeaderBottom.textContent;
        removeItemFromDropdown(dropdownOptionTop, coinNameHeaderBottomTextContent);

        //calculating course

        const searchQuery = [currentCoinAbbr, mainCoinAbbrTop.textContent];
        fetch(`https://api.binance.com/api/v1/exchangeInfo`)
            .then(response => {
                return response.json();
            }).then(data => {
            let res = '';
            data.symbols.forEach(el => {
                if (el.symbol === searchQuery.join('') || el.symbol === searchQuery.reverse().join('')) {
                    res = el.symbol;

                    fetch(`https://api.binance.com/api/v3/ticker/price?symbol=${res}`)
                        .then(response => {
                            return response.json();
                        }).then(data => {
                        console.log('data btc', data);
                        let firstPart = searchQuery[0];
                        let secondPart = searchQuery[1];
                        if (searchQuery.join('') !== data.symbol) {
                            firstPart = searchQuery[1];
                            secondPart = searchQuery[0];
                        }
                        rateElement.textContent = `1${firstPart} - ${data.price}${secondPart} `
                    });

                }
                // else {
                //     fetch(`https://api.binance.com/api/v3/ticker/price?symbols=[${currentCoinAbbr}USDT,USDT${mainCoinAbbrBottom.textContent}]`)
                //         .then(response => {
                //             return response.json();
                //         }).then(data => {
                //         console.log('data btc usdt', data);
                //         // console.log('data btc', data.symbols);
                //     });
                // }
            })
        })
    });
})

headerDropdownBottom.addEventListener('click', () => {
    dropdownTop.classList.remove('opened');
    dropdownBottom.classList.toggle('opened');
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