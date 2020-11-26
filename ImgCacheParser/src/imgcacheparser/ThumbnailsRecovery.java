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
//    private final String inputFile = "C:\\Users\\Air\\Desktop\\imgCacheParser\\photo_blob.0";
    private final String inputFile = "C:\\Users\\Air\\Desktop\\imgCacheParser\\imgcache[1].0";
//    private final String inputFile = "C:\\Users\\Air\\Desktop\\imgCacheParser\\thumbnail_blob.0";
//    private final String JPG_HEADER_STRING = "-1 -40 -1 ";
//    private final String JPG_FOOTER_STRING = "-1 -39";
//    final private static String SUB_STRING = "/ l o c a l / i m a g e / i t e m /";
    private int fileSize;
    private int header_count = 0;
    private static final byte[] JPG_HEADER_BYTES = new byte[]{-1, -40, -1};
    private static final byte[] JPG_FOOTER_BYTES = new byte[]{-1, -39};

    private List<Thumbnails> recoveredThumbnails;

    public ThumbnailsRecovery() {
        this.recoveredThumbnails = recoveredThumbnails;
    }

    //Find all indices of a substring in a given string
    private Map<String, List<Integer>> allIndices(byte[] fileContent, byte[] headerBytes, byte[] footerBytes) {
        List<Integer> headerIndexes = new ArrayList<>();
        List<Integer> footerIndexes = new ArrayList<>();

        for (int i = 0; i < fileContent.length; i++) {
            if (i < fileContent.length) {
                if (fileContent[i] == headerBytes[0] && fileContent[i + 1] == headerBytes[1] && fileContent[i + 2] == headerBytes[2]) {
                    headerIndexes.add(i);
                } if (fileContent[i] == footerBytes[0] && fileContent[i + 1] == footerBytes[1]) {
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
        try {
            inputStream.getChannel().position(jpgStart);
            inputStream.read(rawJpgOutput, 0, rawJpgOutput.length);
//            System.out.println(Arrays.toString(rawJpgOutput));
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
}
