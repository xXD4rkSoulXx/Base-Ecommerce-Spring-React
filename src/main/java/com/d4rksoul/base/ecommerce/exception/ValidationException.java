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

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends HandledException {
  private final Map<String, String> errors;

  public ValidationException(String message, Map<String, String> errors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.errors = errors;
  }
}
