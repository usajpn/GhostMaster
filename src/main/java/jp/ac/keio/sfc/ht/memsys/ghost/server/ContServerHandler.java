/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
//import siftdemo.SIFTThread;

import java.io.FileOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ContServerHandler.java
 * Created on 11/27/14.
 */
public class ContServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    LinkedBlockingQueue mQueue;
    int port;

    public ContServerHandler(LinkedBlockingQueue queue, int p){
        super();
        mQueue = queue;
        port = p;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {

        final HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

        while (decoder.hasNext()) {
            final InterfaceHttpData data = decoder.next();
            if (data != null) {
                try {
                    MemoryFileUpload file = (MemoryFileUpload) data;

                    // Start SIFT Thread
//                    Thread t = new Thread(new SIFTThread(file.get(), mQueue));
//                    t.start();
                    writeToTestFile(file.get());
                } finally {
                    data.release();
                }
            }
        }
    }

    private void writeToTestFile(byte[] data){

        FileOutputStream fos = null;

        try{
            // create new file output stream
            if (port == 2556) {
                fos=new FileOutputStream(Constants.ANDROID_IMG_FILE_OUT);
            } else {
                fos=new FileOutputStream(Constants.CLOUD_IMG_OUT);
            }

            // writes bytes to the output stream
            fos.write(data);

            // flushes the content to the underlying stream
            fos.flush();

            fos.close();
            // create new file input stream
        }catch(Exception ex) {
            // if an error occurs
            ex.printStackTrace();
        }
    }
}
