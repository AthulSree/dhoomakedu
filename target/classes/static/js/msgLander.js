
$(function () {
    $("#hostdd").select2();

    //MPR grp selection
    $(document).on("click", "#grp_dd_mpr", function () {
        $('#hostdd').val(['1', '3', '4', '5', '6']).trigger('change');
    })

    //Tea grp selection
    $(document).on("click", "#grp_dd_tea", function () {
        $('#hostdd').val(['1', '3', '5', '6']).trigger('change');
    })

    //Crex grp selection
    $(document).on("click", "#grp_dd_crex", function () {
        $('#hostdd').val(['3']).trigger('change');
    })

    // Character counter
    $('#msgContainer').on('input', function () {
        const length = $(this).val().length;
        $('#charCount').text(`${length} / 500`);
    });

})