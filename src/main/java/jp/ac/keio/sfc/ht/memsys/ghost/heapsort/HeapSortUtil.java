/*
 * Copyright (c) 2014. Ghost Project
 *
 * Ghost is a project of the Memsys research group
 * (http://memsys.ht.sfc.keio.ac.jp/front/)
 * at Hide Tokuda Laboratory in Keio University, Japan.
 * Ghost project started in Open Research Forum 2014.
 */

package jp.ac.keio.sfc.ht.memsys.ghost.heapsort;

import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;

import java.security.SecureRandom;
import java.util.Random;

/**
 * HeapSortUtil
 * Created on 11/30/14.
 */
public class HeapSortUtil {

    public static OffloadableData genData(String ID, String seq){

        OffloadableData data = new OffloadableData(ID, seq);

        double[] body = new double[10240];

        Random rnd = new SecureRandom();
        for(int i = 0; i<body.length; i++){
            body[i] = rnd.nextDouble();
        }

        data.putData(HeapSortTaskKeys.DATA, body);

        return data;
    }
}
