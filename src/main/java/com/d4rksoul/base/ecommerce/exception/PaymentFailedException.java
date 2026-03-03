/**
 *  A pasta exception contém todos os erros personalizados, de forma a evitar que o
 * website crashe em caso de erro feito pelo utilizador, e invês disso, ele lança só uma
 * mensagem de erro a informar qual erro ocorreu.
 *
 *
 *  Estas Exceptions que não são nem HandledException, nem GlobalExceptionHandler, são
 * exceptions personalizadas com erros específicos criado por nós, que depois herda o
 * HandledException e no handleHandleException ser enviada para a ApiError, API que
 * contém os detalhes do erro.
 */

package com.d4rksoul.base.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class PaymentFailedException extends HandledException {
    public PaymentFailedException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }
}
