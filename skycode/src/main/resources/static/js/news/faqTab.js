$(document).ready(function () {
    $('.nyFaqTab').click(function () {
        $('.nyFaqTab').removeClass('active');
        $(this).addClass('active');
        let selectedCategory = $(this).attr('data-category');

        if (selectedCategory === 'all') {
            $('.nyFaqTable tbody tr').show();
        } else {
            $('.nyFaqTable tbody tr').hide();
            $('.nyFaqTable tbody tr[data-category="' + selectedCategory + '"]').show();
        }
    });
});
