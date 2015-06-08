package org.ntk.mutibo.repository;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface ItemRepository extends CrudRepository<Item, Long>{

	Collection<Item> findByName(String name);
	
	long countByExternalId(long externalId);
	
	Item findByExternalId(long externalId);
}