package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.dto.employeeLeaveDto;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface employeeRepository extends JpaRepository<employee, Integer> {
        @Query(value = "SELECT " +
                        "E.ID, " +
                        "E.FIRST_NAME AS firstName, " +
                        "E.LAST_NAME AS lastName, " +
                        "E.AS_GROUP AS asGroup, " +
                        "CAST( CASE WHEN (L.AT_SIDE_A IS NOT NULL AND L.AT_SIDE_A != '') THEN 'Y' ELSE 'N' END AS CHAR(1) ) AS at1, "
                        +
                        "CAST( CASE WHEN (L.AT_SIDE_B IS NOT NULL AND L.AT_SIDE_B != '') THEN 'Y' ELSE 'N' END AS CHAR(1) ) AS at2, "
                        +
                        "L.LEAVE_LIST AS leaveList, " +
                        "W.AGENCY AS agency, " +
                        "W.JOIN_DATE AS joinDate, " +
                        "W.PROJECT_NUM AS projectNum, " +
                        "W.WO_TO_DATE AS woToDate " +
                        "FROM employee_master E " +
                        "LEFT JOIN leave_list L ON E.ID = L.EMP_ID AND EXISTS (SELECT 1 FROM month_period M WHERE M.MONTH=L.LEAVE_MONTH AND M.YEAR=L.LEAVE_YEAR) "
                        +
                        "LEFT JOIN work_order W ON W.EMP_ID = E.ID", nativeQuery = true)
        List<employeeLeaveDto> findEmployeeLeaveDetails();

}
