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
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;

/**
 * HeapSortTaskImpl
 * Created on 11/30/14.
 */
public class HeapSortTaskImpl implements OffloadableTask {
    private static final String TASK_NAME = "HEAPSORT";

    private double[] heap;
    private int num;

    @Override
    public OffloadableData run(OffloadableData offloadableData) {
        double[] vals = (double[])offloadableData.getData(HeapSortTaskKeys.DATA);

        if (vals == null) {
            System.out.println("[HeapSortTaskImpl] vals is null");
        }

        heap = new double[vals.length];
        num = 0;

        for(int i=0; i<vals.length; i++){
            insert(vals[i]);
        }

        for(int i = 0; num>0; i++){
            vals[i] = deletemin();
        }

        OffloadableData result = new OffloadableData(offloadableData.NAME_SPACE);
        result.putData(HeapSortTaskKeys.DATA, vals);


        return null;
    }

    private void insert(double d){

        heap[num++]=d;
        int i=num,j=i/2;
        while(i>1 && heap[i-1]<heap[j-1]){
            double t=heap[i-1];
            heap[i-1]=heap[j-1];
            heap[j-1]=t;
            i=j;
            j=i/2;
        }

    }

    private double deletemin(){

        double r=heap[0];
        heap[0]=heap[--num];
        int i=1,j=i*2;
        while(j<=num){
            if(j+1<=num && heap[j-1]>heap[j]) j++;
            if(heap[i-1]>heap[j-1]){
                double t=heap[i-1];
                heap[i-1]=heap[j-1];
                heap[j-1]=t;
            }
            i=j;
            j=i*2;
        }
        return r;

    }

    @Override
    public String getName() {
        return TASK_NAME;
    }
}
