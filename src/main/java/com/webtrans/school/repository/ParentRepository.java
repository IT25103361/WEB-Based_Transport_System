package com.webtrans.school.repository;

import com.webtrans.school.model.Parent;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByEmail(String email);
}