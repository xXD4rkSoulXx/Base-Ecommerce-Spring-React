package com.aprenderSpring.ecommerceSpring.DTO;

import com.aprenderSpring.ecommerceSpring.Enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminSignUpDto {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private UserRole role;

    @JsonProperty("address_id")
    private Long addressId;
}
