package com.zxl.socket.server.transport;

import com.zxl.socket.server.IOHandler;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-4-3
 */
public class BlankIO implements IOClient {

    private static BlankIO blankIO = null;

    public static BlankIO getInstance() {
        if (blankIO == null)
            blankIO = new BlankIO();

        return blankIO;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#send(java.lang.String)
     */
    @Override
    public void send(String message) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#sendEncoded(java.lang.
     * String)
     */
    @Override
    public void sendEncoded(String message) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#heartbeat()
     */
    @Override
    public void heartbeat(final IOHandler ioHandler) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#disconnect()
     */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#getSessionID()
     */
    @Override
    public String getSessionID() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#getCTX()
     */
    @Override
    public ChannelHandlerContext getCTX() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#getId()
     */
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#isOpen()
     */
    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#setOpen(boolean)
     */
    @Override
    public void setOpen(boolean open) {
    }

    /* (non-Javadoc)
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#getNamespace()
     */
    @Override
    public String getNamespace() {
        return null;
    }
}