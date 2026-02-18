package com.aprenderSpring.ecommerceSpring.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
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
