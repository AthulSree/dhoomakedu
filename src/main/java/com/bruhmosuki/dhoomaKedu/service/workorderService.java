package com.bruhmosuki.dhoomaKedu.service;


import com.bruhmosuki.dhoomaKedu.dao.workorderRepository;
import com.bruhmosuki.dhoomaKedu.entity.workorder;
import org.springframework.stereotype.Service;
import com.bruhmosuki.dhoomaKedu.entity.employee;


import java.util.List;
import java.util.Optional;


@Service
public class workorderService {

    private workorderRepository workorderRepository;

    public workorderService(com.bruhmosuki.dhoomaKedu.dao.workorderRepository workorderRepository) {
        this.workorderRepository = workorderRepository;
    }

    public List<workorder> findAll(){
        return workorderRepository.findAll();
    }

    public workorder findById(int theId){
        Optional<workorder> result = workorderRepository.findById(theId);
        workorder theWorkorder = null;
        if(result.isPresent()){
            theWorkorder = result.get();
        }else{
            throw new RuntimeException("Work order not found for id "+theId);
        }
        return theWorkorder;
    }

    public workorder findByEmpId(employee employeeData){
        return workorderRepository.findByEmpId(employeeData);
    }


    public void save(workorder theWorkorder){
        workorderRepository.save(theWorkorder);
    }

    public void delete(int theInt){
        workorderRepository.deleteById(theInt);
    }
    
}
