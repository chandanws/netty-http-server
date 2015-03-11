package com.henko.server.handler;

import com.henko.server.dao.ConnectDao;
import com.henko.server.dao.UniqueReqDao;
import com.henko.server.model.Connect;
import com.henko.server.model.UniqueReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import static com.henko.server.dao.impl.DaoFactory.*;

public class ServerTrafficHandler extends ChannelTrafficShapingHandler {

    /** Don`t execute doAccounting() method **/
    private static final int CHECK_INTERVAL = 0;

    private ConnectDao _connectDao;
    private Connect _connect;

    private UniqueReqDao _requestDao;
    private UniqueReq _uniqueReq;

    private double _durationMillis;
    private long _receivedBytes;
    private long _sendBytes;

    private ServerTrafficHandler(long checkInterval) {
        super(checkInterval);
    }

    public ServerTrafficHandler(Connect connect, UniqueReq uniqueReq) {
        this(CHECK_INTERVAL);

        this._connectDao = getDaoFactory(H2).getConnectionDao();
        this._requestDao = getDaoFactory(H2).getUniqueReqDao();
        this._connect = connect;
        this._uniqueReq = uniqueReq;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        long startConnTimeStamp = System.currentTimeMillis();
        _connect.setTimestamp(startConnTimeStamp);
        _uniqueReq.setLastConn(startConnTimeStamp);

        super.handlerAdded(ctx);
    }

    @Override
    public synchronized void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        _saveConnectData();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void _saveConnectData() {
        long stopConnTimeStamp = System.currentTimeMillis();
        _durationMillis = _parseDurationMillis(stopConnTimeStamp);

        _receivedBytes = this.trafficCounter().cumulativeReadBytes();
        _sendBytes = this.trafficCounter().cumulativeWrittenBytes();
        long speed = _parseSpeed();

        _connect.setReceivedBytes(_receivedBytes);
        _connect.setSendBytes(_sendBytes);
        _connect.setSpeed(speed);

        _connectDao.insertConnect(_connect);
        _requestDao.addOrIncrementCount(_uniqueReq);
    }

    private long _parseDurationMillis(long stopConnTimeStamp) {
        long result = stopConnTimeStamp - _connect.getTimestamp();

        if (result == 0) return 1;

        return result;
    }

    private long _parseSpeed() {
        double durationSec = _durationMillis / 1000.0;

        return (long) ((_receivedBytes + _sendBytes) / durationSec);
    }
}
