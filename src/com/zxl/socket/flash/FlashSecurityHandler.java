package com.zxl.socket.flash;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-29
 */
public class FlashSecurityHandler extends SimpleChannelUpstreamHandler {
    private final static Logger log = Logger
            .getLogger(FlashSecurityHandler.class);
    private static ChannelBuffer channelBuffer = ChannelBuffers
            .copiedBuffer(
                    "<?xml version=\"1.0\"?>"
                            + "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">"
                            + "<cross-domain-policy> "
                            + "   <site-control permitted-cross-domain-policies=\"master-only\"/>"
                            + "   <allow-access-from domain=\"*\" to-ports=\"*\" />"
                            + "</cross-domain-policy>", CharsetUtil.UTF_8);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        ChannelFuture f = e.getChannel().write(channelBuffer);
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        log.warn("Exception now ...");
        e.getChannel().close();
    }
}
