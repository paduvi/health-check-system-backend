package com.paduvi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.paduvi.model.User;

@Transactional(readOnly = true)
public interface UserRepository extends CrudRepository<User, Long> {

	List<User> findByNameContaining(String name);

	User findOneByName(String name);

	@Transactional
	@Modifying
	@Query("DELETE FROM User u WHERE u.id IN ?1")
	int deleteByIds(List<Long> ids);
}
