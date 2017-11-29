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
package com.ipd.jsf.worker.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;

public interface CommonDAO<T> {

    /**
     * @param entity
     * @return Integer the id
     * @throws Exception
     */
    Integer insert(T entity) throws Exception;

    /**
     * <p>batch insert
     *
     * @param coll A Collecton, Maybe a List or Set or another
     * @throws Exception
     */
    void batchInsert(final Collection<T> coll) throws Exception;

    /**
     * @param params Maybe a entity object.
     *               more usual, it's a Map or parameterization Object
     * @return
     * @throws Exception
     */
    int update(Object params) throws Exception;

    /**
     * @param coll A Collecton, Maybe a List or Set or another
     * @throws Exception
     */
    void batchUpdate(final Collection coll) throws Exception;

    /**
     * @param id usual, it's a Integer Object
     * @return
     * @throws Exception
     */
    int delete(Object id) throws Exception;

    /**
     * @param params maybe an Array with the ids or a List ...
     * @throws Exception
     */
    void batchDelete(@Param(value = "params") Object params) throws Exception;

    /**
     * count the total number by the query params
     *
     * @param params Maybe a entity object.
     *               more usual, it's a Map or parameterization Object
     * @return
     */
    int count(@Param(value = "params") Object params);

    /**
     * paging query
     *
     * @param params Maybe a entity object.
     *               more usual, it's a Map or parameterization Object
     * @param offset
     * @param size
     * @return
     */
    Collection list(@Param(value = "params") Object params, @Param(value = "offset") int offset, @Param(value = "size") int size);

    /**
     * @param params Maybe a entity object.
     *               more usual, it's a Map or parameterization Object
     * @return
     */
    Collection listAll(Object params);

    /**
     * @param params usual, it's an id(Integer) Object or a parameterization Object
     * @return T the entity
     */
    T get(Object params);

    /**
     * @param params usual, it's a Map or parameterization Object
     * @return Object usual it's a VO or BO Object
     */
    Object find(Object params);

    /**
     * @param params usual, it's the Integer id
     * @return if int > 0 , indicate exists
     */
    int isExists(Object params);

}