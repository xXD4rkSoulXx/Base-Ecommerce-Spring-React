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

package com.aprenderSpring.ecommerceSpring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressRequestDto {
    private String street;

    private String city;

    private String state;

    @JsonProperty("zip_code")
    private String zipCode;

    private String country;
}
