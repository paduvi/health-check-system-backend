package com.paduvi.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paduvi.model.NotFoundException;
import com.paduvi.model.User;
import com.paduvi.repository.ServiceRepository;
import com.paduvi.repository.UserRepository;

@RestController
@RequestMapping(value = "/api/user")
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	ServiceRepository serviceRepository;

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody User save(@RequestBody User user) {
		return userRepository.save(user);
	}

	@RequestMapping(value = "/findall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Iterable<User> findAll() {
		return userRepository.findAll();
	}

	@RequestMapping(value = "/findbyid/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody User findById(@PathVariable(name = "id") long id) throws NotFoundException {
		try {
			return userRepository.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new NotFoundException(ex);
		}
	}

	@RequestMapping(value = "/findbyname/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody User findByName(@PathVariable(name = "name") String name) throws NotFoundException {
		User user = userRepository.findOneByName(name);
		if (user == null) {
			throw new NotFoundException();
		}
		return user;
	}

	@RequestMapping(value = "/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody User defaultUser() {
		return new User();
	}

	@RequestMapping(value = "/findbynamecontain/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<User> findByNameContain(@PathVariable(name = "name") String name)
			throws NotFoundException {
		return userRepository.findByNameContaining(name);
	}

	@RequestMapping(value = "/remove", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> remove(@RequestBody List<Long> ids) {
		Map<String, Object> response = new HashMap<>();
		List<User> nonRemovableUsers = serviceRepository.findAllDistinctUser(ids);

		ids.removeAll(nonRemovableUsers.parallelStream().map(User::getId).collect(Collectors.toList()));

		int success = userRepository.deleteByIds(ids);

		response.put("removed", success);
		response.put("failed", nonRemovableUsers.size());

		if (!nonRemovableUsers.isEmpty()) {
			response.put("message", "Detach user from current services before removing!");
			response.put("detail", nonRemovableUsers.parallelStream().map(User::getName).collect(Collectors.toList()));
		}
		return response;
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String count() {
		return "{\"count\":" + userRepository.count() + "}";
	}
}
