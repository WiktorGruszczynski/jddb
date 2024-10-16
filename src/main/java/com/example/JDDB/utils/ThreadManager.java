package com.example.JDDB.utils;

public class ThreadManager {

    public void awaitThreadPool(Iterable<Thread> threads){
        while (true){
            boolean runningThread = false;

            for (Thread thread: threads){
                if (thread.isAlive()){
                    runningThread = true;
                    break;
                }
            }

            if (!runningThread) break;
        }
    }
}
