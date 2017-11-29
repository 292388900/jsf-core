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
package com.ipd.jsf.registry.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 机房信息
 */
public class Room implements Serializable {

    private static final long serialVersionUID = 1842206020790869564L;

    private int id;

    private int room;

    private String ipSection;

    private String ipRegular;

    public Pattern p;
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the room
     */
    public int getRoom() {
        return room;
    }

    /**
     * @param room the room to set
     */
    public void setRoom(int room) {
        this.room = room;
    }

    /**
     * @return the ipSection
     */
    public String getIpSection() {
        return ipSection;
    }

    /**
     * @param ipSection
     *            the ipSection to set
     */
    public void setIpSection(String ipSection) {
        this.ipSection = ipSection;
    }

    /**
     * @return the ipRegular
     */
    public String getIpRegular() {
        return ipRegular;
    }

    /**
     * @param ipRegular the ipRegular to set
     */
    public void setIpRegular(String ipRegular) {
        this.ipRegular = ipRegular;
    }
    
}
