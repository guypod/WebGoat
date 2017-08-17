$(document).ready(function () {
    $('.showMail').click(function (e) {
        e.preventDefault();
        $(this).parent().find('.contents').toggle()
    });
});