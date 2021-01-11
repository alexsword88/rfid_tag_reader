package com.yjjj.rfid;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONWriter {
    private FileWriter csvWriter;

    public JSONWriter(String filename) {
        try {
            this.csvWriter = new FileWriter(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(JSONArray jsonarray) {
        try {
            this.csvWriter.write(jsonarray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(JSONObject jsonObject) {
        try {
            this.csvWriter.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine(String[] rowData) {
        try {
            this.csvWriter.append(String.join(",", rowData));
            this.csvWriter.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.csvWriter.flush();
            this.csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
