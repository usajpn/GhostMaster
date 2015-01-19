/*
 * Copyright (c) 2015. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.server;

import akka.util.Timeout;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostResponseTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.GhostResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.nio.channels.Channel;
import java.util.concurrent.TimeUnit;

/**
 * Created by aqram on 1/19/15.
 */
public class ResponseWaitHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception{

        if(event instanceof Future){
        }else{
            System.err.println("Unhandled event");
            throw new Exception();
        }

        Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
        Future<Object> f = (Future<Object>)event;
        GhostResponse result = (GhostResponse) Await.result(f, timeout.duration());
        GhostResponse res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.EXECUTE, null);

        ctx.write(res);
    }

}
