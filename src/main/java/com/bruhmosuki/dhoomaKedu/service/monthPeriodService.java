package com.bruhmosuki.dhoomaKedu.service;


import com.bruhmosuki.dhoomaKedu.dao.monthPeriodRepository;
import com.bruhmosuki.dhoomaKedu.entity.monthPeriod;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@Service
public class monthPeriodService {

    private monthPeriodRepository theMonthPeriodRepository;

    public monthPeriodService(monthPeriodRepository theMonthPeriodRepository) {
        this.theMonthPeriodRepository = theMonthPeriodRepository;
    }

    public List<monthPeriod> findAll(){
        List<monthPeriod> periods = theMonthPeriodRepository.findAll();

        if (periods == null || periods.isEmpty()) {
            LocalDate now = LocalDate.now();
            monthPeriod defaultPeriod = new monthPeriod();
            defaultPeriod.setMonth(now.getMonthValue());
            defaultPeriod.setYear(now.getYear());
            return Collections.singletonList(defaultPeriod); // return a list with 1 element
        }

        return periods;
    }

    public void Save(monthPeriod theMonthPeriod){
        theMonthPeriodRepository.save(theMonthPeriod);
    }

    public void DeleteAll(){
        theMonthPeriodRepository.deleteAll();
    }
}
