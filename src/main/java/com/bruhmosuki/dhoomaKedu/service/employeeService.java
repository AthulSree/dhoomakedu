package com.bruhmosuki.dhoomaKedu.service;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.stereotype.Service;
import com.bruhmosuki.dhoomaKedu.dao.employeeRepository;
import com.bruhmosuki.dhoomaKedu.dto.employeeLeaveDto;

import java.util.List;
import java.util.Optional;

@Service
public class employeeService {

    //    create repository obj
    private final employeeRepository employeeRepository;

    //    create constructor for initialising repository obj
    public employeeService(employeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    //    findall fn
    public List<employee> findAll(){
        return employeeRepository.findAll();
    }

    //    findbyid fn
    public employee findById(int theId){
        Optional<employee> result = employeeRepository.findById(theId);

        employee the_employee = null;

        if(result.isPresent()){
            the_employee = result.get();
        }else{
            throw new RuntimeException("No Employee Found for -"+theId);
        }
        return the_employee;
    }

    //    save fn
    public void save(employee the_employee){
        employeeRepository.save(the_employee);
    }

    //    delete fn
    public void deleteById(int theId){
        employeeRepository.deleteById(theId);
    }

    public List<employeeLeaveDto> getAllEmployeesLeaveDetails(){
        return employeeRepository.findEmployeeLeaveDetails();
    }

}
