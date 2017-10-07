package ru.mai;

import static java.lang.Thread.State.WAITING;
import java.util.Random;

public class CCollector implements Runnable {        //выступает в роли монитора
    private volatile boolean active;    //значение переменной будет изменяться разными потоками
    private boolean runsFirstTime;      //запускается в первый раз?
    private int logger;                 //скок потоков обратилось
    
    private CEngine engine;
    private Thread[]threads;
    
    private boolean extFromLoop;
    private int numOfCycles;             //как рабочее время
    private int currentNumOfCycles;
    
    private Random randForDanceFloorStyle;
    private int newDanceFloorStyle;
    
    CCollector(CEngine engine){
        active = false;
        runsFirstTime = true;
        logger = 0;
        currentNumOfCycles = 1;
        
        this.engine = engine;
        numOfCycles = engine.getNumOfCycles();        
        threads = new Thread[engine.getN()];
        
        randForDanceFloorStyle = new Random();
    }
    
    @Override
    public void run() {     
        if (runsFirstTime){     //в принципе проверка runsFirstTime не особо нужна т.к. есть while(true) далее, оставлено для большей понятности
            for (int i = 0; i < engine.getN(); i++) {
                threads[i] = new Thread(engine.getArrayOfD()[i]);
                //threads[i].setPriority(MIN_PRIORITY);
                threads[i].start();
            }
            runsFirstTime = false;
        }
        
        while (/*!Thread.currentThread().isInterrupted()*/true){    //активного ожидания нет благодаря wait() в методе standBy();
            standBy();
        }
    }
    
    public synchronized void standBy(){
        if (active == false) {
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        //____________________________________________________________что делать при активации

        if (logger==engine.getN()){             //набрались все потоки
            System.out.println("\nDANCE FLOOR STYLE: "+ engine.getDanceFloor().getStyle());
            extFromLoop = false;
            while (!extFromLoop) {
                try {
                    Thread.currentThread().sleep(50); //после того, как зарегистрировались все потоки-танцоры, проверяй, что все дотанцевали и допили и ждут следующего стиля в следующей итерации с таким интервалом               
                } catch (InterruptedException ex) {}
                /*try {
                    engine.getMutex().release();
                } catch (InterruptedException ex) {}*/
                //if ((threads[0].getState()==WAITING)&&(threads[1].getState()==WAITING)&&(threads[2].getState()==WAITING))
                
                //{
                    System.out.println("CYCLE: " + currentNumOfCycles);
                    
                    if (currentNumOfCycles==numOfCycles){               //если набралось изначально с cengine заданное кол-во циклов - останавливаем
                        System.out.println("\nВЫПОЛНЕНО ЦИКЛОВ: " + currentNumOfCycles); 
                        for (int i = 0; i < engine.getN(); i++) {
                            threads[i].stop();                
                            //threads[i].interrupt();
                            //System.out.print(threads[i].getState()+"\t");
                        }
                        engine.wakeUp();
                        //System.out.println(engine.getThr().getState());//показывает runnable т.к. тот еще не окончил работу
                        Thread.currentThread()./*interrupt();*/stop();
                        //System.out.println("\n"+Thread.currentThread().getState());
                    }
                    else{                                               //иначе будим потоки танцоров
                        engine.getDanceFloor().setStyle(randForDanceFloorStyle.nextInt(3) + 1); //новый стиль танца на новой итерации
                        for (int i = 0; i<engine.getN();i++){
                        engine.getArrayOfD()[i].wakeUp();               //без всяких приоритеов
                        }
                    }
                    currentNumOfCycles++;                                                 //УВЕЛИЧИВАЕМ КОЛ ВО ЦИКЛОВ      
                    //System.out.println(currentNumOfCycles); 
                    extFromLoop = true;
                    //System.out.println("\n next numOfCycle will be " + currentNumOfCycles);
                    System.out.println("_________________________________________________________________\n");
                //}
            }
            logger = 0;                                                                   //сбрасываем счетчик кол-ва выполненых потоков с 5 до 0
        }     
        //______________________________________________________________________
        active = false;                                                                 //чтобы поток CCollector заснул при проверке потом
    }
    
    public synchronized void wakeUp(boolean register){
        if (register){
            logger++;
        }
        active = true;
        notify();   
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
