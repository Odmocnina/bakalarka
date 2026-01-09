package core.model;

public class Parameter {

    /** minimum value of parameter **/
    public double minValue;

    /** maximum value of parameter **/
    public double maxValue;

    /** range of parameter **/
    double range;

    public String name;

    /**
     * constructor for parameter
     *
     * @param minValue minimum value of parameter
     * @param maxValue maximum value of parameter
     */
    public Parameter(String name, double minValue, double maxValue) {
        this.name = name;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.range = maxValue - minValue;
    }

    /**
     * function to check if parameter range is valid
     *
     * @return boolean whether parameter range is valid
     */
    public boolean checkIfValid() {
        return minValue <= maxValue && range >= 0;
    }
}
