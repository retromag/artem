fetch('http://localhost:8080/api/coin/BTC/margin')
    .then(response => {
        // response.json();
        return response.json();
        // console.log('response.json();', response.json());
        // console.log('--------');
        // console.log('response.json();', response.margin);
    }).then(data => {
    console.log('data', data);
});

const dropdownTop = document.querySelector('.js-top-dropdown');
const headerDropdownTop = document.querySelector('.js-top-dropdown-header');
const dropdownOptionTop = document.querySelectorAll('.js-top-dropdown-option');
const coinNameHeaderTop = document.querySelector('.js-top-dropdown-title');
const mainCoinImgTop = document.querySelector('.js-top-main-coin-image');
const mainCoinAbbrTop = document.querySelector('.js-top-main-coin-abbr');



dropdownOptionTop.forEach((option) => {
    option.addEventListener('click', () => {
        const coinName = option.querySelector('[data-coin-name]').textContent;
        const coinAbbr = option.querySelector('[data-coin-abbr]').textContent;
        const coinSrcImg = option.querySelector('[data-coin-img]').getAttribute('src');
        mainCoinImgTop.setAttribute('src', `${coinSrcImg}`);

        coinNameHeaderTop.textContent = coinName;
        mainCoinAbbrTop.textContent = coinAbbr;

        console.log('coinName',coinName);
        console.log('coinAbbr',coinAbbr);
        console.log('coinSrcImg',coinSrcImg);


        dropdownTop.classList.remove('opened');
    });
})

headerDropdownTop.addEventListener('click', () => {
    dropdownTop.classList.toggle('opened');
});

const headerDropdownBottom = document.querySelector('.js-bottom-dropdown-header');
const dropdownBottom = document.querySelector('.js-bottom-dropdown');
const dropdownOptionBottom = document.querySelectorAll('.js-bottom-dropdown-option');
const coinNameHeaderBottom = document.querySelector('.js-bottom-dropdown-title');
const mainCoinImgBottom = document.querySelector('.js-bottom-main-coin-image');
const mainCoinAbbrBottom = document.querySelector('.js-bottom-main-coin-abbr');


dropdownOptionBottom.forEach((option) => {
    option.addEventListener('click', () => {
        const coinName = option.querySelector('[data-coin-name]').textContent;
        const coinAbbr = option.querySelector('[data-coin-abbr]').textContent;
        const coinSrcImg = option.querySelector('[data-coin-img]').getAttribute('src');
        mainCoinImgBottom.setAttribute('src', `${coinSrcImg}`);

        coinNameHeaderBottom.textContent = coinName;
        mainCoinAbbrBottom.textContent = coinAbbr;

        // console.log('coinName',coinName);
        // console.log('coinAbbr',coinAbbr);
        // console.log('coinSrcImg',coinSrcImg);


        dropdownBottom.classList.remove('opened');
    });
})

headerDropdownBottom.addEventListener('click', () => {
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