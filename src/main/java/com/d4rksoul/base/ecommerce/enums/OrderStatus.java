/*
 *  A pasta enums é responsável por definir todos os estados possíveis, bloqueando
 * valores fora do definido.
 * */

package com.d4rksoul.base.ecommerce.enums;

public enum OrderStatus {
    // SHIPPED e DELIVERED é para entregas físicas
    CREATED, CANCELLED, FAILED, PAID, SHIPPED, DELIVERED, REFUNDED
}
