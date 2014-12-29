/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.server;

/**
 * GhostRequestServerHandler
 * Created on 12/21/14.
 */
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jp.ac.keio.sfc.ht.memsys.ghost.actor.Gateway;
import jp.ac.keio.sfc.ht.memsys.ghost.cache.RemoteCacheContainer;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostRequestTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.datatypes.GhostResponseTypes;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.requests.*;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.util.Util;
import jp.ac.keio.sfc.ht.memsys.ghost.nqueen.NQueenTaskImpl;
import org.infinispan.client.hotrod.RemoteCache;

import java.util.List;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class GhostRequestServerHandler extends ChannelInboundHandlerAdapter {
    private Gateway gateway;

    public GhostRequestServerHandler(Gateway g) {
        gateway = g;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        GhostRequest m = (GhostRequest)msg;
        GhostResponse res = null;
        if (m.TYPE.equals(GhostRequestTypes.INIT)) {
            String appId = gateway.registerApplication(m.PARAMS.getData(BundleKeys.APP_NAME));
            Bundle bundle = new Bundle();
            bundle.putData(BundleKeys.APP_ID, appId);
            res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.INIT, bundle);
        } else if (m.TYPE.equals(GhostRequestTypes.REGISTERTASK)) {
            gateway.registerTask(m);
            res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.REGISTERTASK, null);
        } else if (m.TYPE.equals(GhostRequestTypes.EXECUTE)) {
            gateway.executeTask(m);
            res = new GhostResponse(GhostResponseTypes.SUCCESS, GhostRequestTypes.EXECUTE, null);
        } else {
            System.out.println("[Ghost Request Server Handler] UNKNOWN REQUEST");
        }
        ctx.write(res);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

