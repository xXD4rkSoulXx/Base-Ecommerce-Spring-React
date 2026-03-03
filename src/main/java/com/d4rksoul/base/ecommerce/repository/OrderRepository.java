/*
 *  A pasta repository é responsável por definir as operações SQL.
 *
 *  O extends JpaRepository já adiciona métodos como busca pelo Id, salvar ou eliminar pelo Id.
 *
 *  Métodos adicionais como procurar pelo nome, ou outras que não envolvam o Id da própria tabela,
 * têm que ser específicadas dentro da interface, onde os nomes dos métodos têm de ser com nomenclaturas
 * específicas.
 *
 *  Para casos de ter a necessidade de ter vários métodos, para várias consultas, utiliza-se o
 * JpaSpecificationExecutor, e depois na pasta specification configuram-se os métodos, evitando assim a
 * necessidade de criar grande quantidade de métodos.
 * */

package com.d4rksoul.base.ecommerce.repository;

import com.d4rksoul.base.ecommerce.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
}
