package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bruhmosuki.dhoomaKedu.entity.workorder;

public interface workorderRepository extends JpaRepository<workorder,Integer> {

    workorder findByEmpId(employee emp);


}
