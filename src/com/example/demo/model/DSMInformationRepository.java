package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DSMInformationRepository extends JpaRepository<dsmdata, Long> {
	//Text
	
	
}
