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

let countDownDate = new Date().getTime() + 15 * 60 * 1000;

// Update the count down every 1 second
let x = setInterval(function() {

    // Get today's date and time
    let now = new Date().getTime();

    // Find the distance between now and the count down date
    let distance = countDownDate - now;

    // Time calculations for hours, minutes and seconds
    let hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    let minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
    let seconds = Math.floor((distance % (1000 * 60)) / 1000);

    // Display the result in the element with id="demo"
    document.getElementById("js-time").innerHTML =  hours + ":"
        + minutes + ":" + seconds;

    // If the count down is finished, write some text
    if (distance < 0) {
        clearInterval(x);
        document.getElementById("js-time").innerHTML = "EXPIRED";
        document.location.href="/";
    }
}, 1000);