package com.hiriver.position.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hiriver.position.store.BinlogPositionStore;
import com.hiriver.unbiz.mysql.lib.protocol.binlog.BinlogFileBinlogPosition;
import com.hiriver.unbiz.mysql.lib.protocol.binlog.GTidBinlogPosition;
import com.hiriver.unbiz.mysql.lib.protocol.binlog.extra.BinlogPosition;

/**
 * 抽象的存储的同步点实现
 * 
 * @author hexiufeng
 *
 */
public abstract class AbstractBinlogPositionStore implements BinlogPositionStore {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBinlogPositionStore.class);

    @Override
    public void store(BinlogPosition binlogPosition, String channelId) {
        storeImpl(binlogPosition.toBytesArray(), channelId);
    }

    @Override
    public BinlogPosition load(String channelId) {
        byte[] posBuf = loadImpl(channelId);
        if (posBuf == null) {
            LOG.info("can't load binlog pos from store in {}", channelId);
            return null;
        }
        String line = new String(posBuf);
        line = line.trim();
        
        try{
            return new GTidBinlogPosition(line);
        }catch(RuntimeException e){
            LOG.info("loaded binlog pos [{}] from store may be binlog name+pos in {},",line, channelId);
        }
        
        String[] array = line.split(":");
        if (array.length != 2) {
            LOG.info("loaded binlog pos [{}] from store is incorrect in {},",line, channelId);
            return null;
        }
        
        return new BinlogFileBinlogPosition(array[0], Long.parseLong(array[1]));
    }

    /**
     * 存储同步点
     * 
     * @param posBuf 二进制化的同步点
     * @param channelId 指定的数据流
     */
    protected abstract void storeImpl(byte[] posBuf, String channelId);

    /**
     * 加载二进制化的同步点
     * 
     * @param channelId 指定的数据流
     * @return 同步点
     */
    protected abstract byte[] loadImpl(String channelId);
}
