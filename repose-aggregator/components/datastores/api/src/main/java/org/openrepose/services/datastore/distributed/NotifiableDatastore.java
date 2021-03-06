package org.openrepose.services.datastore.distributed;

import org.openrepose.services.datastore.DatastoreOperationException;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public interface NotifiableDatastore {

    boolean remove(String key, boolean notify) throws DatastoreOperationException;
    void put(String key, Serializable value, boolean notify) throws DatastoreOperationException;
    void put(String key, Serializable value, int ttl, TimeUnit timeUnit, boolean notify) throws DatastoreOperationException;
}
