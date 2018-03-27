package com.paduvi.graphql.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.google.common.collect.Lists;
import com.paduvi.model.HealthJob;
import com.paduvi.model.NotFoundException;
import com.paduvi.model.Service;
import com.paduvi.repository.ServiceRepository;
import com.paduvi.serviceworker.HealthDispatcher;

@Component
public class Mutation implements GraphQLMutationResolver {

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	HealthDispatcher healthDispatcher;

	public Service createService(Service service) {
		service = serviceRepository.save(service);
		
		healthDispatcher.insertJobToQueue(new HealthJob(service), 0);

		return service;
	}

	public Service updateService(Long id, Service service) {
		boolean exist = serviceRepository.existsById(id);
		if (!exist) {
			throw new NotFoundException("The service to be updated was not found");
		}
		service.setId(id);
		return serviceRepository.save(service);
	}

	public Boolean deleteService(Long id) {
		boolean exist = serviceRepository.existsById(id);
		if (!exist) {
			throw new NotFoundException("The service to be deleted was not found");
		}
		int count = serviceRepository.deleteByIds(Lists.newArrayList(id));
		return count > 0;
	}
}
