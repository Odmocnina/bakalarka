package core.model.cellular;

import core.model.CarParams;

public class Cell {

    /** boolean if cell is occupied **/
    private boolean occupied;
    private boolean isHead;
    private CarParams carParams;

    public Cell() {
        this.occupied = false;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean isHead() {
        return isHead;
    }
    public void setHead(boolean head) {
        isHead = head;
    }

    public CarParams getCarParams() {
        return carParams;
    }
    public void setCarParams(CarParams carParams) {
        this.carParams = carParams;
    }
}
