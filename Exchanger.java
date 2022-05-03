package com.company;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Exchanger<V> {
    /**************************** a  **********************************/
    public Exchanger(){
    }
    private final Lock monitor = new ReentrantLock();
    private Condition myCondition = monitor.newCondition();
    volatile V myObject=null;

    public V myExchanger(V m,long Timeout){
        V a=null;

        monitor.lock();
        try{
                //Date deadline =System.currentDate + Timeout;
                if(myObject==null ){

                    myObject=m;
                    try{
                    while(myObject==m )
                        myCondition.awaitUntil(Time1);
                    }
                    catch (InterruptedException e){
                        System.out.print("interrupted ");
                        Thread.currentThread().interrupt();
                        return null;
                    }

                    a=myObject;
                    return a;
                }
                else{
                    a=myObject;
                    myObject=m;
                    myCondition.signal();

                    return a;
                }
            }
        finally {
            monitor.unlock();
        }

    }

    /****************************  b  **********************************/
    /*
    public V myExchanger(V m){
        V a=null;
        if(myObject==null){
            myObject=m;
            while(myObject==m)
                Thread.yield();

            a=myObject;
            return a;
        }
        else{
            a=myObject;
            myObject=m;
            Thread.yield();

            return a;
        }
    }*/
}
