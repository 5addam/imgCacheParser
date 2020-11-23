/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgcacheparser.models;

/**
 *
 * @author Air
 */
public class Thumbnails {
    private Integer jpgStart;
    private Integer jpgEnd;
    private Integer jpgSize;
    private String pathName;
    private String outputFileName;
    byte[] rawOutput;

    public Thumbnails() {
        
    }

    public Thumbnails(Integer jpgStart, Integer jpgEnd, Integer jpgSize, String pathName, String outputFileName, byte[] rawOutput) {
        this.jpgStart = jpgStart;
        this.jpgEnd = jpgEnd;
        this.jpgSize = jpgSize;
        this.pathName = pathName;
        this.outputFileName = outputFileName;
        this.rawOutput = rawOutput;
    }

    public Integer getJpgStart() {
        return jpgStart;
    }

    public Integer getJpgEnd() {
        return jpgEnd;
    }

    public Integer getJpgSize() {
        return jpgSize;
    }

    public String getPathName() {
        return pathName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public byte[] getRawOutput() {
        return rawOutput;
    }
    
}


