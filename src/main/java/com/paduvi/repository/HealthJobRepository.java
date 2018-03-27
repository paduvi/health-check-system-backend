package com.paduvi.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.paduvi.model.HealthJob;

@Transactional(readOnly = true)
public interface HealthJobRepository extends CrudRepository<HealthJob, Long> {

	public List<HealthJob> findByServiceIdOrderByExecutedAtDesc(Long serviceId);
}
