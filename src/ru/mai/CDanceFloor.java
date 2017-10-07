package ru.mai;

public class CDanceFloor implements IDanceFloor {
    private volatile int style = 1;         //пусть тусовка начинается с RnB (в задании не уточнено с чего на начинается (с чего-то/с ничего))
    /*  будем считать (условно), что на танцопле играет:
        1 - RnB,
        2 - Electrohouse,
        3 - Pop,
        0 - не выбрано
    */
    
    public synchronized int getStyle() {
        return style;
    }
    
    public synchronized void setStyle(int style) {
        this.style = style;
    }
}
