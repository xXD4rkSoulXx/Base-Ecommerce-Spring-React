/**
 *  A pasta exception contém todos os erros personalizados, de forma a evitar que o
 * website crashe em caso de erro feito pelo utilizador, e invês disso, ele lança só uma
 * mensagem de erro a informar qual erro ocorreu.
 *
 *
 *  O HandledException é uma classe abstrata para que várias Exceptions personalizadas herdem
 * dela, tornando o código melhor. Consequentemente, esta classe passará para o
 * handleHandledException do GlobalExceptionHandler para comunicar com a ApiError, API
 * responsável pelos detalhes dos erros.
 */

package com.d4rksoul.base.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class HandledException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    protected HandledException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
