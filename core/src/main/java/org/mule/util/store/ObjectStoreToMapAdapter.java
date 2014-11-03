package org.mule.util.store;

import org.mule.api.MuleRuntimeException;
import org.mule.api.store.ListableObjectStore;
import org.mule.api.store.ObjectStoreException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectStoreToMapAdapter<ValueType extends Serializable> implements Map<Serializable, ValueType>
{

    private final ListableObjectStore<ValueType> objectStore;

    public ObjectStoreToMapAdapter(final ListableObjectStore<ValueType> listableObjectStore)
    {
        this.objectStore = listableObjectStore;
    }

    @Override
    public int size()
    {
        try
        {
            return this.objectStore.allKeys().size();
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public boolean isEmpty()
    {
        try
        {
            return this.objectStore.allKeys().isEmpty();
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public boolean containsKey(Object key)
    {
        try
        {
            return this.objectStore.contains((Serializable) key);
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException("Map adapter for object store does not support contains value");
    }

    @Override
    public ValueType get(Object key)
    {
        try
        {
            if (!this.objectStore.contains((Serializable) key))
            {
                return null;
            }
            return this.objectStore.retrieve((Serializable) key);
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public ValueType put(Serializable key, ValueType value)
    {
        ValueType previousValue = null;
        try
        {
            if (this.objectStore.contains(key))
            {
                previousValue = objectStore.retrieve(key);
                objectStore.remove(key);
            }
            if (value != null)
            {
                objectStore.store(key, value);
            }
            return previousValue;
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public ValueType remove(Object key)
    {
        return null;
    }

    @Override
    public void putAll(Map<? extends Serializable, ? extends ValueType> mapToAdd)
    {
        for (Serializable key : mapToAdd.keySet())
        {
            put(key, mapToAdd.get(key));
        }
    }

    @Override
    public void clear()
    {
        try
        {
            objectStore.clear();
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public Set<Serializable> keySet()
    {
        try
        {
            final List<Serializable> allKeys = objectStore.allKeys();
            return new HashSet(allKeys);
        }
        catch (ObjectStoreException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public Collection<ValueType> values()
    {
        throw new UnsupportedOperationException("ObjectStoreToMapAdapter does not support values() method");
    }

    @Override
    public Set<Entry<Serializable, ValueType>> entrySet()
    {
        throw new UnsupportedOperationException("ObjectStoreToMapAdapter does not support entrySet() method");
    }

}
