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

package com.ipd.jsf.version.common.dao;


import java.util.Date;
import java.util.List;

import com.ipd.jsf.version.common.domain.IfaceAliasVersion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.version.common.domain.IfaceAlias;

@Repository
public interface AliasVersionDAO {
    public int batchCreate(@Param("aliasList") List<IfaceAlias> aliasList, @Param("version") long version, @Param("time") Date time) throws Exception;

    public int updateConfVersion(@Param("interfaceId") int interfaceId, @Param("version") Date version) throws Exception;

    public List<IfaceAliasVersion> getAliasVersionByIfaceId(@Param("interfaceId") int interfaceId) throws Exception;

}
