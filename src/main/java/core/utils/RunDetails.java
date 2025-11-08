package core.utils;

public class RunDetails {

    public int duration;

    public double timeStep;

    public boolean showGui;

    public String outputFile;

    public boolean drawCells;

    public boolean[] log = new boolean[] { true, true, true, true, true, true };

    public boolean writingResults() {
        return this.outputFile != null && !this.outputFile.isEmpty();
    }


}
