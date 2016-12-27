package utilities;

import java.lang.ref.*;
import java.util.*;

public class WeakObj {

    public static void demo(String[] args) {

        ReferenceQueue aReferenceQueue = new ReferenceQueue();
        Object anObject = new Object();

        //create a WeakReference obj with a reference q
        WeakReference ref = new WeakReference(anObject, aReferenceQueue);
        String extraData = new String("Extra Data");
        HashMap aHashMap = new HashMap();

        //Associate extraData (value) with weak reference
        // (key) in aHashMap
        aHashMap.put(ref, extraData);

        //Clear the strong reference to anObject，这样it能被gc
        anObject = null;

        //Clear the strong reference to extraData
        extraData = null;

        //Run the garbage collector, and
        //下面是我自己改的，不知道理解对否
        System.gc();
        if (ref.isEnqueued())
            aHashMap.remove(ref);
        //这时extraData能被gc了！
    }
}