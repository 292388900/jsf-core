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
package com.ipd.jsf.worker.thread.alias;

import com.ipd.jsf.worker.dao.ClientDao;
import com.ipd.jsf.worker.dao.IfaceAliasDAO;
import com.ipd.jsf.worker.dao.ServerDao;
import com.ipd.jsf.worker.util.PropertyUtil;
import com.ipd.jsf.version.common.domain.IfaceAliasVersion;
import com.ipd.jsf.worker.common.SingleWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Title: DeleteRedundantAliasWorker
 * Description: DeleteRedundantAliasWorker 删除没用的alias
 */
public class DeleteRedundantAliasWorker extends SingleWorker {

    private static Logger logger = LoggerFactory.getLogger(DeleteRedundantAliasWorker.class);

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private IfaceAliasDAO aliassVersionDAO;

    private static long TIME_BEFORE = 30L * 24L * 60L * 60L * 1000L;//30天前

    private static boolean isDelete = false;//我可真的删了啊。。。

    @Override
    public boolean run() {

        String isDeleteStr = PropertyUtil.getProperties("deleteRedundantAliasWorker.delete");//是否真删除
        if(isDeleteStr != null && isDeleteStr.equals("true")){
            isDelete = true;
        }

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            Date before = new Date(now.getTime() - TIME_BEFORE);//30天前
            logger.info("处理更新日期在指定时间之前的alias:" + simpleDateFormat.format(before));

            //查询更新日期在指定时间之前的alias
            List<IfaceAliasVersion> ifaceAliasVersionList = aliassVersionDAO.getAliasVersionBeforeTime(simpleDateFormat.format(before));
            if (ifaceAliasVersionList != null) {
                logger.info("需要处理的alias个数 。ifaceAliasVersionList.size():" + ifaceAliasVersionList.size());

                for (IfaceAliasVersion ifaceAliasVersion : ifaceAliasVersionList) {
                    //查看该接口，alias的provider个数
                    int pNum = serverDao.getCountByIfaceAndAlias(ifaceAliasVersion.getInterfaceId(), ifaceAliasVersion.getAlias());
                    if (pNum == 0) {
                        //查看该接口，alias的consumer个数
                        int cNum = clientDao.getCountByIfaceAndAlias(ifaceAliasVersion.getInterfaceId(), ifaceAliasVersion.getAlias());
                        if (cNum == 0) {
                            logger.info("删除alias: " + ifaceAliasVersion + " isDelete:" + isDelete);
                            if(isDelete){
                                aliassVersionDAO.deleteAlias(ifaceAliasVersion);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("DeleteRedundantAliasWorker 执行完成！");

        return true;
    }

    //插入alias 备用
    private void insertAlias(IfaceAliasVersion ifaceAliasVersion) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ifaceAliasVersion.setUpdateTimestamp(simpleDateFormat.parse(ifaceAliasVersion.getUpdateTime()).getTime());
            aliassVersionDAO.insertAlias(ifaceAliasVersion);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("还原alias成功：" + ifaceAliasVersion);
    }

    @Override
    public String getWorkerType() {
        return "deleteRedundantAliasWorker";
    }
}
