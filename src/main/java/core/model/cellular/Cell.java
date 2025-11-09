package core.model.cellular;

import core.model.CarParams;

/********************************************
 * class representing a cell in cellular road
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class Cell {

    /** boolean if cell is occupied **/
    private boolean occupied;

    /** boolean if cell is head of the car **/
    private boolean isHead;

    /** car parameters if occupied **/
    private CarParams carParams;

    /**
     * constructor for cell, initially unoccupied
     **/
    public Cell() {
        this.occupied = false;
    }

    /**
     * getter for occupied
     *
     * @return boolean occupied
     **/
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * setter for occupied
     *
     * @param occupied boolean occupied
     **/
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    /**
     * getter for isHead
     *
     * @return boolean isHead
     **/
    public boolean isHead() {
        return isHead;
    }

    /**
     * setter for isHead
     *
     * @param head boolean isHead
     **/
    public void setHead(boolean head) {
        isHead = head;
    }

    /**
     * getter for carParams
     *
     * @return CarParams carParams
     **/
    public CarParams getCarParams() {
        return carParams;
    }

    /**
     * setter for carParams
     *
     * @param carParams CarParams carParams
     **/
    public void setCarParams(CarParams carParams) {
        this.carParams = carParams;
    }
}
