package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.entity.leaves;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface leavesRepository extends JpaRepository<leaves,Integer> {

    List<leaves> findByLeaveMpMonthAndLeaveMpYear(int month, int year);

    leaves findByEmpIdAndLeaveMpMonthAndLeaveMpYear(employee emp, int month, int year);

    leaves findByEmpId(employee emp);
}
