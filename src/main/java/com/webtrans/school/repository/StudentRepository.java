package com.webtrans.school.repository;

import com.webtrans.school.model.Student;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByParentId(Long parentId);
}