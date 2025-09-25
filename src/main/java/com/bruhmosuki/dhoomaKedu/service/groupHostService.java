package com.bruhmosuki.dhoomaKedu.service;

import com.bruhmosuki.dhoomaKedu.dao.groupHostRepository;
import com.bruhmosuki.dhoomaKedu.entity.groupHost;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class groupHostService {

    private groupHostRepository groupHostRepository;

    public groupHostService(groupHostRepository groupHostRepository) {
        this.groupHostRepository = groupHostRepository;
    }

    public List<groupHost> findAll(){
        return groupHostRepository.findAll();
    }

    public groupHost findById(Integer id){
        Optional<groupHost> groupHost = groupHostRepository.findById(id);
        groupHost grpHost = null;
        if(groupHost.isPresent()){
            grpHost = groupHost.get();
        }else {
            throw new RuntimeException("No Host Found for -"+id);
        }
        return grpHost;
    }

    public groupHost save(groupHost groupHost){
        return groupHostRepository.save(groupHost);
    }


}
