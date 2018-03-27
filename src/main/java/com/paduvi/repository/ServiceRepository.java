package com.paduvi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.paduvi.model.Service;
import com.paduvi.model.User;

@Transactional(readOnly = true)
public interface ServiceRepository extends CrudRepository<Service, Long> {

	Service findOneByName(String name);

	@Query("SELECT DISTINCT s.user FROM Service s")
	List<User> findAllDistinctUser();

	@Query("SELECT DISTINCT s.user FROM Service s WHERE s.user.id IN ?1")
	List<User> findAllDistinctUser(List<Long> ids);

	@Modifying
	@Transactional
	@Query("DELETE FROM Service s WHERE s.id IN ?1")
	int deleteByIds(List<Long> ids);
	
	@Query("SELECT COUNT(*) FROM Service s WHERE s.watching IS true AND s.healthy IS true")
	int countRunningService();

	@Query("SELECT COUNT(*) FROM Service s WHERE s.watching IS true AND s.healthy IS false")
	int countStoppedService();
}
