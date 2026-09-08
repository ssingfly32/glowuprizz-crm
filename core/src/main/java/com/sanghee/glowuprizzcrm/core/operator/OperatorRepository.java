package com.sanghee.glowuprizzcrm.core.operator;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Long> {

    Optional<Operator> findByEmail(String email);
}
