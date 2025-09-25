    (function () {
      const input = document.getElementById('leaveStr');
      const dialogue = document.getElementById('dialogueBox');
      let timer;

      function showDialogue() {
        dialogue.classList.add('show');
        clearTimeout(timer);
        timer = setTimeout(() => {
          dialogue.classList.remove('show');
        }, 6000);
      }

      input.addEventListener('keydown', (e) => {
        // allow numbers, comma, Backspace, Delete, arrows, Tab
        if (
          !(e.key >= '0' && e.key <= '9') &&
          e.key !== ',' &&
          e.key !== 'Backspace' &&
          e.key !== 'Delete' &&
          e.key !== 'ArrowLeft' &&
          e.key !== 'ArrowRight' &&
          e.key !== 'Tab'
        ) {
          e.preventDefault();
          showDialogue();
        }
      });
    })();