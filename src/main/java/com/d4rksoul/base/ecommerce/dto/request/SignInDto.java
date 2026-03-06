/*
 *  A pasta dto contém todos os requests e responses.
 *
 *  Request seria o que o utilizador envia ou decide, por exemplo, dados como nome, morada,
 * quantidade de produtos comprados, id do produto que deu review, entre outros.
 *
 *  Não se coloca Requests dados que é definido automaticamente como role que já vai ser USER,
 * totalPrice que é a soma de todos os preços; calculado pelo sistema, createdAt que é
 * uma data gerada automaticamente, userID que o sistema vai buscar o id do user logado, entre outros.
 * */

package com.d4rksoul.base.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignInDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
