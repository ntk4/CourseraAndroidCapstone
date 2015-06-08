package org.ntk.mutibo.exception;

public class EntryAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -2677022133589547975L;
    private Class<?> entity;
    private long id;

    public EntryAlreadyExistsException(Class<?> entity, long id) {
        this.entity = entity;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Entry " + id + " of entity " + entity + " already exists";
    }
    
    public Class<?> getEntity() {
        return entity;
    }

    public long getId() {
        return id;
    }

}
