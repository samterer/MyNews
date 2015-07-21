package com.hzpd.utils;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ZJMA_DataCache<K, E> {
	private int mCapacity;
	private LinkedHashMap<K, E> mCache;
	private final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

	public ZJMA_DataCache(int capacity) {
		mCapacity = capacity;
		mCache = new LinkedHashMap<K, E>(mCapacity) {
			private static final long serialVersionUID = -9165777183357349715L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, E> eldest) {
				return size() > mCapacity;
			}
		};
	}

	public E objectForKey(K key) {
		mReadWriteLock.readLock().lock();
		final E result = mCache.get(key);
		mReadWriteLock.readLock().unlock();

		return result;
	}

	public void putObjectForKey(final K key, final E value) {
		if (key != null && value != null) {
			mReadWriteLock.writeLock().lock();
			mCache.put(key, value);
			mReadWriteLock.writeLock().unlock();
		}
	}

	public boolean containsKey(final K key) {
		mReadWriteLock.readLock().lock();
		final boolean result = mCache.containsKey(key);
		mReadWriteLock.readLock().unlock();

		return result;
	}

	public void clear() {
		mReadWriteLock.writeLock().lock();
		mCache.clear();
		mReadWriteLock.writeLock().unlock();
	}
}