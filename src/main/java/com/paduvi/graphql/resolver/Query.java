package com.paduvi.graphql.resolver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;
import com.paduvi.repository.HealthJobRepository;
import com.paduvi.repository.ServiceRepository;

@Component
public class Query implements GraphQLQueryResolver {

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private HealthJobRepository healthJobRepository;

	public Iterable<Service> services() {
		return serviceRepository.findAll();
	}

	public Service service(Long id) {
		return serviceRepository.findById(id).orElse(null);
	}

	public List<HealthJob> logs(Long serviceId) {
		return healthJobRepository.findByServiceIdOrderByExecutedAtDesc(serviceId);
	}
}
