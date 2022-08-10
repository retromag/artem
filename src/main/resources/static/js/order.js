console.log('works');
const inputOrderWallet = document.querySelector('.js-input-wallet');
const inputOrderSum = document.querySelector('.js-input-sum');

const btnCopyWallet = document.querySelector('.js-copy-wallet');
const btnCopySum = document.querySelector('.js-copy-sum');

const copyTextFromInput = (elementForCopying) => {
    elementForCopying.select();
    /* Copy the text inside the text field */
    document.execCommand("copy");
};

btnCopyWallet.addEventListener('click', () => {
    copyTextFromInput(inputOrderWallet);
});
btnCopySum.addEventListener('click', () => {
    copyTextFromInput(inputOrderSum);
});
