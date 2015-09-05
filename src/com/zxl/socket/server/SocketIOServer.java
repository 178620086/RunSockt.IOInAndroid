package com.zxl.socket.server;

import android.util.Log;
import com.zxl.socket.MainServer;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * 调用入口
 *
 * @author yongboy
 * @version 1.0
 * @time 2012-3-23
 */
public class SocketIOServer {

    private ServerBootstrap bootstrap;
    private Channel serverChannel;
    private int port;
    private boolean running;

    public SocketIOServer(int port) {
        this.port = port;
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
                pipeline.addLast("encoder", new HttpResponseEncoder());

                pipeline.addLast("handler", new SocketIOTransportAdapter());
                return pipeline;
            }
        });

        bootstrap.setOption("child.reuseAddress", true);
        // bootstrap.setOption("child.tcpNoDelay", true);
        // bootstrap.setOption("child.keepAlive", true);

        // Bind and start to accept incoming connections.
        this.serverChannel = bootstrap.bind(new InetSocketAddress(port));
        this.running = true;

        Log.i("提示", "Server Started at port [" + port + "]");
    }

    public void stop() {
        if (!this.running)
            return;

        Log.i("提示", "Server shutting down.");
        handlerShutdown();
        this.serverChannel.close();
        this.bootstrap.releaseExternalResources();
        Log.i("提示", "**SHUTDOWN**");
        this.running = false;
    }

    private void handlerShutdown() {
        for (IOHandler handler : MainServer.getAllHandlers()) {
            handler.OnShutdown();
        }
    }
}