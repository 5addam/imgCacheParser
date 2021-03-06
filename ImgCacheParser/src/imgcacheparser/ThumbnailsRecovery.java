/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgcacheparser;

import imgcacheparser.models.Thumbnails;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Air
 */
public class ThumbnailsRecovery {

    final private static int MAXPATH = 200; //100 x UTF16 chars = max path size
    private static int fileCount = 0;
//    private final String inputFile = "C:\\Users\\Air\\Desktop\\imgCacheParser\\photo_blob.0";
    private final String inputFile = "/home/saddam/imgCacheParser/imgcache[1].0";
    final private static String rootFolder = "/home/saddam/Pictures/testImages";
//    private final String inputFile = "C:\\Users\\Air\\Desktop\\imgCacheParser\\thumbnail_blob.0";
//    private final String JPG_HEADER_STRING = "-1 -40 -1 ";
//    private final String JPG_FOOTER_STRING = "-1 -39";
//    final private static String SUB_STRING = "/ l o c a l / i m a g e / i t e m /";

    private int fileSize;
    private int header_count = 0;
    List<Byte> fileData = null;
    List<Byte> tempData = null;
    List<Integer> headerListIndex = new ArrayList<>();
    List<Integer> footerListIndex = new ArrayList<>();
    private static final byte[] JPG_HEADER_BYTES = new byte[]{-1, -40, -1};
    private static final byte[] JPG_FOOTER_BYTES = new byte[]{-1, -39};
    private static final byte[] PDF_HEADER_BYTES = new byte[]{37, 80, 68, 70, 45, 49, 46};    //25 50 44 46 2d
    private static final byte[] PDF_FOOTER_BYTES = new byte[]{37, 37, 69, 79, 70, 13, 10};    //25 25 45 4F 46

    private List<Thumbnails> recoveredThumbnails;

    public ThumbnailsRecovery() {
        this.recoveredThumbnails = recoveredThumbnails;
        this.fileData = new ArrayList<>();
        this.tempData = new ArrayList<>();
    }

    public void startScan() {
        File rootDir = new File(rootFolder);
        File[] subFiles = rootDir.listFiles();
        for (File file : subFiles) {
            System.out.println(file.getAbsoluteFile());
//            PdfRecovery(file);
            JpgRecovery(file);
        }
        System.out.println("Number of PDF files found: " + fileCount);
        System.out.println("Header Count: " + headerListIndex.size());
        System.out.println("Footer Count: " + footerListIndex.size());
    }

    //Find all indices of a substring in a given string
    private Map<String, List<Integer>> allIndices(byte[] fileContent, byte[] headerBytes, byte[] footerBytes) {
        List<Integer> headerIndexes = new ArrayList<>();
        List<Integer> footerIndexes = new ArrayList<>();

        for (int i = 0; i < fileContent.length; i++) {
            if (i < fileContent.length) {
                if (fileContent[i] == headerBytes[0] && fileContent[i + 1] == headerBytes[1] && fileContent[i + 2] == headerBytes[2]) {
                    headerIndexes.add(i);
                }
                if (fileContent[i] == footerBytes[0] && fileContent[i + 1] == footerBytes[1]) {
                    footerIndexes.add(i);
                }
            }
        }
        Map<String, List<Integer>> listIndex = new HashMap();
        listIndex.put("headerIndexes", headerIndexes);
        listIndex.put("footerIndexes", footerIndexes);
        return listIndex;
    }
    //Find all indices of a substring in a given string

    private List<Integer> allHeaderBytesIndices(byte[] bigString, byte[] subString) {
        List<Integer> listIndex = new ArrayList<>();
        for (int i = 0; i < bigString.length; i++) {
            if (i < bigString.length) {
                if (bigString[i] == subString[0] && bigString[i + 1] == subString[1] && bigString[i + 2] == subString[2]) {
                    listIndex.add(i);
                }
            }
        }
        return listIndex;
    }

    private List<Integer> allFooterBytesIndices(byte[] bigString, byte[] subString) {
        List<Integer> listIndex = new ArrayList<>();
        for (int i = 0; i < bigString.length; i++) {
            if (i < bigString.length) {
                if (bigString[i] == subString[0] && bigString[i + 1] == subString[1]) {
                    listIndex.add(i);
                }
            }
        }
        return listIndex;
    }

    public void recoverImages() {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(inputFile));

            fileSize = (int) new File(inputFile).length(); // byte count of the file-content

            byte[] filecontent = new byte[fileSize];
            String fileString = "";

            fileInputStream.read(filecontent, 0, (int) fileSize);
            String str;

            str = Arrays.toString(filecontent).replace(",", "");
//            System.out.print(str);
//            List<Integer> jpgHeaderHits = allIndices(str, JPG_HEADER_STRING);
            Map<String, List<Integer>> allIndexes = allIndices(filecontent, JPG_HEADER_BYTES, JPG_FOOTER_BYTES);
            List<Integer> jpgHeaderHits = allIndexes.get("headerIndexes");
            List<Integer> jpgFooterHits = allIndexes.get("footerIndexes");

            System.out.println("Headers Found: " + String.valueOf(jpgHeaderHits.size()));
            System.out.println("Footers Found: " + String.valueOf(jpgFooterHits.size()));

            for (int i = 0; i < jpgHeaderHits.size(); i++) {
                createJpg(fileInputStream, jpgHeaderHits.get(i), jpgFooterHits.get(i), jpgFooterHits.get(i) - jpgHeaderHits.get(i));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ThumbnailsRecovery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThumbnailsRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createJpg(FileInputStream inputStream, Integer jpgStart, Integer jpgEnd, Integer jpgSize) {
        byte[] rawJpgOutput = new byte[jpgSize];
//        List<Byte> arrByte = new ArrayList<>();
        byte[] byteArr = null;
        try {
            inputStream.getChannel().position(jpgStart);
            inputStream.read(rawJpgOutput, 0, rawJpgOutput.length);

            System.out.println("\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        String fileName = String.valueOf(jpgStart) + "_" + String.valueOf(jpgEnd) + "_" + String.valueOf(jpgSize) + ".jpg";
        File myFile = new File(fileName);
        try {
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException ex) {
            Logger.getLogger(ImageRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (FileOutputStream stream = new FileOutputStream(myFile)) {
            stream.write(rawJpgOutput);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageRecovery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void JpgRecovery(File file) {
        byte[] data = new byte[3];
        boolean headerFound = false;
        boolean footerFound = false;
        int startByte;
        byte[] Byte2byte = null;
        FileInputStream is = null;

        try {
            is = new FileInputStream(file.getPath());

            while ((startByte = is.read()) != -1) {
                if ((byte) startByte == -1 && !headerFound) {
                    is.read(data, 0, data.length);
                    if ((data[0] == -40 && data[1] == -1 && data[2] == -32)
                            || (data[0] == -40 && data[1] == -1 && data[2] == -31)
                            || (data[0] == -40 && data[1] == -1 && data[2] == -18)
                            || (data[0] == -40 && data[1] == -1 && data[2] == -40)) {
                        fileData.clear();
                        headerFound = true;
                        footerFound = false;
                        headerListIndex.add(startByte);
                    }
                    if ((byte) startByte == -39) {
                        byte tmp = (byte) is.read();
                        if (tmp == -39) {
                            if (footerFound) {
                                tempData.add((byte) startByte);
                                writeTempByte(data);
                                data = new byte[data.length];
                                fileData.addAll(tempData);
                                tempData.clear();
                            } else {
                                footerFound = true;
                                headerFound = false;
                                footerListIndex.add(startByte);
                                fileData.add((byte) startByte);
                                writeByte(data);
                                data = new byte[data.length];
                            }
                        }

                    }
                }
                if (headerFound) {
                    fileData.add((byte) startByte);
                    if (data[0] != 0 && data[1] != 0) {
                        writeByte(data);
                        data = new byte[data.length];
                    }
                }
                if (footerFound) {
                    tempData.add((byte) startByte);
                    if (data[0] != 0 && data[1] != 0) {
                        writeTempByte(data);
                        data = new byte[data.length];
                    }
                }
            }
            Byte2byte = new byte[fileData.size()];
            for (int b = 0; b < fileData.size(); b++) {
                Byte2byte[b] = fileData.get(b);
            }
            createFile(Byte2byte, ".jpg");
            tempData.clear();
            fileData.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PdfRecovery(File file) {
        byte[] headerBytes = new byte[4];
        byte[] footerBytes = new byte[4];
        byte[] extraFooterBytes = new byte[2];
        boolean headerFound = false;
        boolean footerFound = false;
        boolean multiFooters = false;
        int startByte;
        FileInputStream is = null;
        byte Byte2byte[] = null;
        try {
            is = new FileInputStream(file.getPath());

            while ((startByte = is.read()) != -1) {
                if ((byte) startByte == PDF_HEADER_BYTES[0]) {
                    is.read(headerBytes, 0, headerBytes.length);
                    if (headerBytes[0] == PDF_HEADER_BYTES[1] && headerBytes[1] == PDF_HEADER_BYTES[2] && headerBytes[2] == PDF_HEADER_BYTES[3]
                            && headerBytes[3] == PDF_HEADER_BYTES[4]) {
                        if (footerFound) {
                            Byte2byte = new byte[fileData.size()];
                            for (int b = 0; b < fileData.size(); b++) {
                                Byte2byte[b] = fileData.get(b);
                            }
                            createFile(Byte2byte, ".pdf");
                            tempData.clear();
                        }
                        fileData.clear();
                        headerFound = true;
                        footerFound = false;
                        headerListIndex.add(startByte);
                    } else if (headerBytes[0] == PDF_FOOTER_BYTES[1] && headerBytes[1] == PDF_FOOTER_BYTES[2] && headerBytes[2] == PDF_FOOTER_BYTES[3]
                            && headerBytes[3] == PDF_FOOTER_BYTES[4]) {
                        if (footerFound) {
                            multiFooters = true;
                            writeTempByte(headerBytes);
                            headerBytes = new byte[headerBytes.length];
                            fileData.addAll(tempData);
                            tempData.clear();
                        } else {
                            headerFound = false;
                            footerFound = true;
                            fileCount++;
                            footerListIndex.add(startByte);
                            fileData.add((byte) startByte);
                            writeByte(headerBytes);
                            headerBytes = new byte[headerBytes.length];
                        }
                    }

                }

                if (headerFound) {
                    fileData.add((byte) startByte);
                    if (headerBytes[0] != 0 && headerBytes[1] != 0) {
                        writeByte(headerBytes);
                        headerBytes = new byte[headerBytes.length];
                    }
                }
                if (footerFound) {
                    tempData.add((byte) startByte);
                    if (headerBytes[0] != 0 && headerBytes[1] != 0) {
                        writeTempByte(headerBytes);
                        headerBytes = new byte[headerBytes.length];
                    }

                }
            }
            Byte2byte = new byte[fileData.size()];
            for (int b = 0; b < fileData.size(); b++) {
                Byte2byte[b] = fileData.get(b);
            }
            createFile(Byte2byte, ".pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createFile(byte[] data, String ext) throws IOException {

        String fileName = String.valueOf(data.length) + "_" + String.valueOf(fileCount) + ".jpg";
        File myFile = new File(fileName);
        try {
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException ex) {
            Logger.getLogger(ImageRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (FileOutputStream stream = new FileOutputStream(myFile)) {
            stream.write(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void writeByte(byte[] bytes) {
        for (byte value : bytes) {
            fileData.add(value);
        }

    }

    private void writeTempByte(byte[] bytes) {
        for (byte value : bytes) {
            tempData.add(value);
        }

    }
}
