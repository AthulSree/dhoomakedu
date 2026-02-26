package com.bruhmosuki.dhoomaKedu.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomAppException.class)
    public String handleCustomAppException(CustomAppException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "customMessage"; // Return the name of the error view
    }
}



// @ControllerAdvice + @ExceptionHandler ആണ് key.

// >> എങ്ങനെ catch ആകുന്നു:

// 1. Request /employee/manage hit ചെയ്യുന്നു.
// 2. Controller methodൽ showError() call ചെയ്ത് CustomAppException throw ചെയ്യുന്നു.
// 3. Spring MVC dispatcher ആ exception intercept ചെയ്യും.
// 4. App contextൽ ഉള്ള എല്ലാ @ControllerAdvice classes scan ചെയ്ത്,
//      @ExceptionHandler(CustomAppException.class) match ചെയ്യുന്ന method നോക്കും.
// 5. Match കിട്ടിയാൽ handleCustomAppException(...) execute ചെയ്യും.
// 6. അതാണ് modelൽ message set ചെയ്ത് "error" view return ചെയ്യുന്നത്.
// 7. അതുകൊണ്ട് ഇത് “manual try-catch” അല്ല; Spring framework-level exception resolution ആണ്.

// 8. Note: IllegalStateException throw ചെയ്താൽ ഈ handler catch ചെയ്യില്ല (type mismatch). അതിന് separate handler വേണം.