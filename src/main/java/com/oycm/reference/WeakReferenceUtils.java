package com.oycm.reference;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ouyangcm
 * create 2025/4/28 16:09
 */
public class WeakReferenceUtils {

    public static void main(String[] args) {

        // 引用队列
        ReferenceQueue<BigObject> referenceQueue = new ReferenceQueue<BigObject>();
        List<WeakReference<BigObject>> referenceList = new ArrayList<>();
        List<BigObject> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            // 创建弱引用对象，并指向引用队列
            WeakReference<BigObject> userWeakReference = new WeakReference<>(new BigObject(), referenceQueue);

            //System.gc();

            list.add(userWeakReference.get());

            // WeakReference 引用对象对象被指向强引用，不能被回收
            System.gc();

            referenceList.add(userWeakReference);
            Reference<? extends BigObject> temp = referenceQueue.poll();
            System.out.println("引用队列情况: " + i + ": " + temp + ", " + (temp != null ? temp.get() : "null") + "; 强引用情况: " + list.get(i));
        }
        for (int i = 0; i < referenceList.size(); i++) {
            if (referenceList.get(i).get() != null){
                System.out.println(i + " hava " + referenceList.get(i));
            }else {
                System.out.println(i + "引用被回收 " + referenceList.get(i));
            }
        }
        //System.gc();
        System.out.println(referenceQueue.poll());

    }

    static class  BigObject {
        private int[] ints = new int[1024*1024];
    }
}
