package ru.mai;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CEngine implements Runnable{
    private Random randN;
    private int  n;
    private final int numOfCycles = 10;         //число циклов (каждый по 5 условных секунд) работы (смены стиля музыки)
    private final int musicDuration = 1000;     //пусть 1000ms = условные 5 секунд, чисто симуляционное время
    
    private Runnable barrierAction;
    private CyclicBarrier barrier1;
    private Semaphore mutex;            //одноместный семафор на каждого танцора
    private volatile CDancer[]arrayOfD;
    private CDanceFloor danceFloor;
    private CCollector collector1;
    private Thread collectorThread;
    
    private Thread thr;

    CEngine(){
        randN = new Random();
        n = randN.nextInt(15) + 1;      //[1;10] отрезок для выбора количества танцоров
        thr = new Thread(this);
        thr.start();
    }
    @Override
    public void run() {
        barrierAction = () -> {
            //System.out.println("BarrierAction executed...");
        };
        barrier1 = new CyclicBarrier(n, barrierAction);
        
        mutex = new Semaphore(1);            

        setDanceFloor(new CDanceFloor());
        arrayOfD = new CDancer[n];

        collector1 = new CCollector(this);     

        for (int i = 0; i < n; i++) {
            arrayOfD[i] = new CDancer(getDanceFloor(), Integer.toString(i), barrier1, mutex, collector1, musicDuration);
        }
        //______________________________________________________________________
        collectorThread = new Thread(collector1);
        collectorThread.start();
        //______________________________________________________________________
        standBy();
        
        if(collectorThread.isAlive()) {
            try {
                collectorThread.join();
                System.out.println("\nCCOLLECTOR THREAD: ЗАКОНЧИЛ РАБОТУ");     //иногда раньше, иногда позже
            } catch (InterruptedException ex) {
                Logger.getLogger(CEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }           
        
        /*System.out.println("СENGINE THREAD: ВЫВОДИМ РЕЗУЛЬТАТЫ");
        Iterator iter = collector1.getList().iterator();
        while(iter.hasNext()) {                 
                IData tmp = (IData)iter.next();
                int a=tmp.getA();
                int b=tmp.getB();
                System.out.println(a+"\t"+b);
        }*/
        //CEngine поток задуман как поток для вывода зарегистрированных (например, в списке) результатов, идея в делегировании ответственностей
        //это облегчит жизнь при расширении функционала программы
        //но т.к. в задании сказано "поведение персонажей следует выводить на экран текстом" 
        //что не конкретизизует, когда выводить полезную информацию (во время/после работы), то было решено выводить во время работы
        System.out.println("CENGINE THREAD: ЗАКОНЧИЛ РАБОТУ");  
    }
    
    public int getN(){
        return n;
    }

    public int getNumOfCycles() {
        return numOfCycles;
    }

    public CDancer[]getArrayOfD() {
        return arrayOfD;
    }
    
    private synchronized void standBy() {
        try {
            wait();
        } catch (InterruptedException ex) {}
    }
    
    public synchronized void wakeUp() {
        notify();
    }

    public Thread getThr() {
        return thr;
    }
    
    public CDanceFloor getDanceFloor() {
        return danceFloor;
    }

    public void setDanceFloor(CDanceFloor danceFloor) {
        this.danceFloor = danceFloor;
    }

    public Semaphore getMutex() {
        return mutex;
    }
}
