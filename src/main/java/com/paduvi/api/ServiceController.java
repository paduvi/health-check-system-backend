package com.paduvi.api;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paduvi.model.HealthJob;
import com.paduvi.model.NotFoundException;
import com.paduvi.model.Service;
import com.paduvi.model.User;
import com.paduvi.repository.ServiceRepository;
import com.paduvi.serviceworker.HealthDispatcher;

@RestController
@RequestMapping(value = "/api/service")
public class ServiceController {

	@Autowired
	ServiceRepository serviceRepository;

	@Autowired
	HealthDispatcher healthDispatcher;

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Service save(@RequestBody Service service) {
		service = serviceRepository.save(service);

		if (service.getLastChecked() == null) {
			healthDispatcher.insertJobToQueue(new HealthJob(service), 0);
		}

		return service;
	}

	@RequestMapping(value = "/findall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Iterable<Service> findAll() {
		return serviceRepository.findAll();
	}

	@RequestMapping(value = "/findbyid/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Service findById(@PathVariable(name = "id") long id) throws NotFoundException {
		try {
			return serviceRepository.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new NotFoundException(ex);
		}
	}

	@RequestMapping(value = "/findbyname/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Service findById(@PathVariable(name = "name") String name) throws NotFoundException {
		Service service = serviceRepository.findOneByName(name);
		if (service == null) {
			throw new NotFoundException();
		}
		return service;
	}

	@RequestMapping(value = "/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Service defaultService() {
		return new Service();
	}

	@RequestMapping(value = "/finduser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<User> findAllDistinctUser() {
		return serviceRepository.findAllDistinctUser();
	}

	@RequestMapping(value = "/remove", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String remove(@RequestBody List<Long> ids) {
		int success = serviceRepository.deleteByIds(ids);
		return "{\"removed\":" + success + "}";
	}

	@RequestMapping(value = "/count/running", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String countRunning() {
		return "{\"count\":" + serviceRepository.countRunningService() + "}";
	}

	@RequestMapping(value = "/count/stopped", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String countStopped() {
		return "{\"count\":" + serviceRepository.countStoppedService() + "}";
	}
}
