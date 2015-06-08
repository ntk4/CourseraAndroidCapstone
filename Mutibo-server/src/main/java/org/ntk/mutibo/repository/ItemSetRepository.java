package org.ntk.mutibo.repository;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface ItemSetRepository extends CrudRepository<ItemSet, Long> {
	public final static String FIND_ID_DISTINCT_QUERY = "SELECT distinct i.id FROM ItemSet i WHERE NOT i.inactive = true AND difficulty = ?1";

	Collection<ItemSet> findByName(String name);

	@Query(FIND_ID_DISTINCT_QUERY)
	List<Long> findByInactiveFalseAndDifficulty(int difficulty);

	@Modifying
	@Query("update ItemSet i set i.inactive = true WHERE i.dislikes > 10 AND i.dislikes > i.likes * 2")
	int inactivateDislikedItemSets();
	
	@Modifying
	@Query("update ItemSet i set i.inactive = false WHERE NOT(i.dislikes > 10 AND i.dislikes > i.likes * 2)")
	int activateLikedItemSets();
}