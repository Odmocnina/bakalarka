package core.utils;

import java.io.File;

public class ResultsWriter {
    private String type;
    private File file;

    public ResultsWriter(String type, String filePath) {
        this.type = type;
        if (filePath != null && !filePath.isEmpty()) {
            file = new File(filePath);
        }
    }

    //public void writeNum

    public void writeResults(String data) {
        if (type.equals(Constants.CSV_TYPE)) {

        } else if (type.equals(Constants.TXT_TYPE)) {

        } else if (type.equals(Constants.CONSOLE_TYPE)) {

        } else {
            System.out.println("Unknown output type: " + type);
        }
    }



}
