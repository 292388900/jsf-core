/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.worker.common.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractWorkerEventMulticaster implements WorkerEventMulticaster{

    private final ListenerRetriever defaultRetriever = new ListenerRetriever();

    private final Map<ListenerCacheKey,ListenerRetriever> retrieverCache = new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>();

    private ReentrantLock lock = new ReentrantLock();


    @Override
    public void addWorkerListener(WorkerListener listener) {
        try{
            lock.lock();
            defaultRetriever.cacheListeners.add(listener);
            retrieverCache.clear();
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void removeWorkerListener(WorkerListener listener) {
        try{
            lock.lock();
            defaultRetriever.cacheListeners.remove(listener);
            retrieverCache.clear();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void removeAllListeners() {
        try{
            lock.lock();
            defaultRetriever.cacheListeners.clear();
            retrieverCache.clear();
        }finally {
            lock.unlock();
        }
    }


    /**
     * 返回指定事件的相关的所有监听器
     *
     * @param event
     * @return
     */
    protected Collection<WorkerListener> getCacheListeners(WorkerEvent event){
        Class<? extends WorkerEvent> eventType = event.getClass();
        Class sourceType = event.getSource().getClass();
        ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
        ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
        if (retriever != null) {
            return retriever.getCacheListeners();
        }
        else {
            retriever = new ListenerRetriever();
            LinkedList<WorkerListener> allListeners = new LinkedList<WorkerListener>();
            try {
                lock.lock();
                for (WorkerListener listener : this.defaultRetriever.cacheListeners) {
                    if (supportsEvent(listener, eventType, sourceType)) {
                        retriever.cacheListeners.add(listener);
                        allListeners.add(listener);
                    }
                }
                this.retrieverCache.put(cacheKey, retriever);
            }finally {
                lock.unlock();
            }
            return allListeners;
        }
    }

    protected boolean supportsEvent(
            WorkerListener listener, Class<? extends WorkerEvent> eventType, Class sourceType) {

        return isSupportedEvent(listener,eventType);
    }


    private boolean isSupportedEvent(WorkerListener listener, Class<? extends WorkerEvent> eventType){
        Type[] types = listener.getClass().getGenericInterfaces();
        if ( types != null && types.length > 0 ){
            Type type = types[0];//  WorkerListener<E extends WorkerEvent>
            if ( type instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType) type;
                Type[] tmpArr = pt.getActualTypeArguments();
                if ( tmpArr != null && tmpArr.length > 0 && tmpArr[0] instanceof Class ){
                    Class clz = (Class) tmpArr[0];
                    if ( eventType.isAssignableFrom( clz )){
                        return true;
                    }else{
                        return false;
                    }
                }
            }
            return false;
        }else {
            return false;
        }
    }


    /**
     * 根据事件类型和sourceType组成缓存ListenerRetriever对象的key
     */
    private static class ListenerCacheKey {


        private final Class eventType;

        private final Class sourceType;

        public ListenerCacheKey(Class eventType, Class sourceType) {
            this.eventType = eventType;
            this.sourceType = sourceType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            ListenerCacheKey otherKey = (ListenerCacheKey) other;
            return (this.eventType.equals(otherKey.eventType) && this.sourceType.equals(otherKey.sourceType));
        }

        @Override
        public int hashCode() {
            return this.eventType.hashCode() * 29 + this.sourceType.hashCode();
        }
    }


    /**
     * WorkerListener 容器
     *
     */
    private class ListenerRetriever {

        public final Set<WorkerListener> cacheListeners;


        public ListenerRetriever() {
            this.cacheListeners = new LinkedHashSet<WorkerListener>();
        }


        public Collection<WorkerListener> getCacheListeners() {
            List<WorkerListener> listeners = new LinkedList<WorkerListener>();
            for ( WorkerListener listener : this.cacheListeners ){
                listeners.add(listener);
            }
            return listeners;
        }
    }
}
