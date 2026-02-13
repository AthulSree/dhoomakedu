package com.bruhmosuki.dhoomaKedu.service;

import com.bruhmosuki.dhoomaKedu.dao.leavesRepository;
import com.bruhmosuki.dhoomaKedu.entity.leaves;
import org.springframework.stereotype.Service;
import com.bruhmosuki.dhoomaKedu.entity.employee;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
public class leavesService {

    private leavesRepository leavesRepository;

    public leavesService(com.bruhmosuki.dhoomaKedu.dao.leavesRepository leavesRepository) {
        this.leavesRepository = leavesRepository;
    }

    public List<leaves> findAll() {
        return leavesRepository.findAll();
    }

    public leaves findById(int theId) {
        leaves theleaves = null;
        Optional<leaves> result = leavesRepository.findById(theId);

        if (result.isPresent()) {
            theleaves = result.get();
        } else {
            throw new RuntimeException("No leave list available for the id" + theId);
        }
        return theleaves;
    }

    public leaves findByEmpId(employee theEmployeeData) {
        leaves theleaves = null;
        Optional<leaves> result = Optional.ofNullable(leavesRepository.findByEmpId(theEmployeeData));

        if (result.isPresent()) {
            theleaves = result.get();
        } else {
            throw new RuntimeException("No leave list available for the id" + theEmployeeData.getId());
        }
        return theleaves;
    }

    public void save(leaves theLeave) {
        leavesRepository.save(theLeave);
    }

    public void deleteById(int theId) {
        leavesRepository.deleteById(theId);
    }

    public List<leaves> findByMonthPeriod(int month, int year) {
        return leavesRepository.findByLeaveMpMonthAndLeaveMpYear(month, year);
    }

    public leaves findByEmpIdAndLeaveMpMonthAndLeaveMpYear(employee theEmployeeData, int month, int year) {
        return leavesRepository.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(theEmployeeData, month, year);
    }

    public List<leaves> findByEmpIdAndLeaveMpYear(employee theEmployeeData, int year) {
        return leavesRepository.findByEmpIdAndLeaveMpYear(theEmployeeData, year);
    }

    public float findTotalComboLeavesTakenThisYear(employee theEmployeeData) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        System.out.println(">>>>>>>>>>>" + year + "<<<<<<<<<<<<<<");
        List<leaves> result = leavesRepository.findByEmpIdAndLeaveMpYear(theEmployeeData, year);

        if (result == null) {
            throw new RuntimeException("No leave list available for the id" + theEmployeeData.getId());
        }
        
        float totalComboLeavesUsed = 0;
        for (leaves leave : result) {
            System.out.println(">>>>>>>>>>>////////////////////"+leave.getUsedComboLeaves()+"///////////////////////////<<<<<<<<<<<<<<");
            totalComboLeavesUsed += leave.getUsedComboLeaves();
        }

        return totalComboLeavesUsed;
    }

}
