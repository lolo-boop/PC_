package com.company;

import Utils.NodeLinkedList;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static Utils.Timeouts.now;

public class BlockingMessageQueue<E> {

    private static class Request {
        final Condition condition;
        Object myObject;
        Request(Condition condition,Object myObject) {
            this.condition = condition;
            this.myObject=myObject;
        }
    }
    private final NodeLinkedList<BlockingMessageQueue.Request> requests = new NodeLinkedList<>();
    private final Lock monitor = new ReentrantLock();

    private int capacity;
    public BlockingMessageQueue(int capacity){
        this.capacity=capacity;
    }

    public boolean enqueue(E message, long timeout) throws InterruptedException{
        synchronized (monitor){
            long start = now();

            if (capacity==requests.getCount()){
                 try{
                     while(capacity == requests.getCount()/* || now()-start!=timeout*/)
                        monitor.wait();
                 }
                 catch (InterruptedException e){}
                 if(capacity==requests.getCount()){
                     requests.enqueue(new Request(monitor.newCondition(),new MyFuture(message)));
                     return true;
                 }
                 else return false;

            }
            else{
                requests.enqueue(new Request(monitor.newCondition(),message));
                return true;
            }
        }
    }

    public Future<E> dequeue(){
        synchronized (monitor) {
            while (requests.isNotEmpty()) {
                NodeLinkedList.Node<BlockingMessageQueue.Request> headNode = requests.pull();
                //headNode.value.condition.signal();
                MyFuture myFuture=(MyFuture)headNode.value.myObject;
                requests.remove(headNode);
                monitor.notify();
                return myFuture ;
            }
             return null;
        }

    }

    public class MyFuture implements Future<E>{
        private boolean received = false;
        private boolean cancelled = false;
        private  E message;
        public MyFuture(E message){
            this.message=message;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return cancelled=mayInterruptIfRunning;
        }
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        @Override
        public boolean isDone() {
            return received;
        }

        @Override
        public E get() throws InterruptedException, ExecutionException {
            return message;
        }

        @Override
        public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return message;
        }
    }

}
