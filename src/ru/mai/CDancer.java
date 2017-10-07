package ru.mai;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class CDancer implements Runnable{
    private CCollector collector1;
    private CDanceFloor danceFloor;
    private String id;
    private CyclicBarrier barrier1 = null;
    private Semaphore mutex;
    
    private Random randForDanceStyle;
    private int danceStyle;
    private int musicDuration;

    CDancer(CDanceFloor danceFloor, String id, CyclicBarrier barrier1, Semaphore mutex, CCollector collector1, int musicDuration){
        randForDanceStyle = new Random();
        danceStyle = randForDanceStyle.nextInt(3) + 1;      //[1;3] отрезок;
        /*  будем считать (условно), что танцор танцует:
            1 - RnB,
            2 - Electrohouse,
            3 - Pop
        */
        this.danceFloor = danceFloor;
        this.id = id;
        this.mutex = mutex;
        this.barrier1 = barrier1;
        this.collector1 = collector1;
        this.musicDuration = musicDuration;
    }

    @Override
    public void run(){
        while (true) {
            dance();
            sleep();
        }
    }
    
    public synchronized void dance(){
        try {
            mutex.acquire();
            if (danceStyle == danceFloor.getStyle()) {                                
                System.out.println("DANCER-"+Integer.parseInt(id)+" WITH DANCE STYLE = " + danceStyle + " IS DANCING...");
                collector1.wakeUp(true);
                mutex.release();
                awaitAtBarrier();
                wait();                             //поэтому syncronized     
            }
            else{
                System.out.println("DANCER-"+Integer.parseInt(id)+" WITH DANCE STYLE = " + danceStyle + " IS DRINKING VODKA...");
                collector1.wakeUp(true);
                mutex.release();
                awaitAtBarrier();
                wait();
            }
        } catch (InterruptedException ex) {}
    }
    private void awaitAtBarrier(){
        //System.out.println(/*Thread.currentThread().getName()*/"DANCER-"+Integer.parseInt(id)+" WITH DANCE STYLE = " + danceStyle + " is waiting at barrier...");
        try {
            this.barrier1.await();
        } catch (InterruptedException | BrokenBarrierException ex) {}
    }
    
    private void sleep(){
        try{
            Thread.currentThread().sleep(musicDuration);
        } catch (InterruptedException e) {} 
    }
    
    public synchronized void wakeUp() {
        notify();
    }
}
