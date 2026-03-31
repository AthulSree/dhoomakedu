(function () {
    const input = document.getElementById('leaveStr');
    const dialogue = document.getElementById('dialogueBox');
    let timer;

    function showDialogue() {
        if (!dialogue) return;
        dialogue.classList.add('show');
        clearTimeout(timer);
        timer = setTimeout(() => {
            dialogue.classList.remove('show');
        }, 6000);
    }

    if (input) {
        input.addEventListener('keydown', (e) => {
            // allow numbers, comma, Backspace, Delete, arrows, Tab, Space, Enter
            const allowedKeys = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', ' ', 'Enter', ','];
            const isNumber = e.key >= '0' && e.key <= '9';
            
            if (!isNumber && !allowedKeys.includes(e.key)) {
                e.preventDefault();
                showDialogue();
            } else if (['Tab', ' ', 'Enter'].includes(e.key)) {
                // Feature requested: see what Shammy says on these keys
                showDialogue();
            }
        });
    }

    // Modal Logic
    function toggleModal() {
        const modal = document.getElementById('instructionModal');
        const notice = document.getElementById('helpNotice');
        
        if (!modal) return;
        
        // Hide notice permanently when modal is engaged
        if (notice) notice.style.display = 'none';

        if (modal.style.display === 'flex') {
            modal.classList.remove('show');
            setTimeout(() => modal.style.display = 'none', 300);
        } else {
            modal.style.display = 'flex';
            setTimeout(() => modal.classList.add('show'), 10);
        }
    }

    // Auto-hide notice after some time
    window.addEventListener('load', () => {
        setTimeout(() => {
            const notice = document.getElementById('helpNotice');
            if (notice) {
                notice.style.opacity = '0';
                setTimeout(() => { if(notice) notice.style.display = 'none'; }, 500);
            }
        }, 8000);
    });

    // Close on clicking outside the content
    window.addEventListener('click', (event) => {
        const modal = document.getElementById('instructionModal');
        if (event.target == modal) {
            toggleModal();
        }
    });

    // Expose to global scope for onclick attributes
    window.toggleModal = toggleModal;
})();