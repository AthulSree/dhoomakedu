package com.bruhmosuki.dhoomaKedu.exceptions;

public class CustomAppException extends RuntimeException {

    public CustomAppException(String message) {
        super(message);
    }

    public CustomAppException(String message, Throwable cause) {
        super(message, cause);
    }

}





// 1. നിന്റെ app‑നു വേണ്ടി സ്വന്തം exception type define ചെയ്യുന്നു (class CustomAppException extends RuntimeException).
// 2. ഇതൊരു “signal type” ആണ്: generic error അല്ല, “custom app error” എന്ന് Spring‑ന് തിരിച്ചറിയാൻ.
//      GlobalExceptionHandler‑ൽ @ExceptionHandler(CustomAppException.class) match ചെയ്യാൻ ഈ type ആവശ്യമാണ്.

// >> അത് work ആയത്:
// 1. showError() ൽ ഇതേ type throw ചെയ്തു.
// 2. @ControllerAdviceൽ അതേ type handle ചെയ്യുന്ന method ഉണ്ടായിരുന്നു.
// 3. throw type, handler type same ആയതുകൊണ്ട് Spring direct ആയി ആ handler method invoke ചെയ്തു.
// 4. CustomAppException file ഇല്ലെങ്കിൽ:
//      compile തന്നെ fail ആവും (class not found), അല്ലെങ്കിൽ
//      വേറെ exception throw ചെയ്താൽ ഈ specific handler hit ആവില്ല (unless Exception.class പോലുള്ള generic handler ഉണ്ടെങ്കിൽ).