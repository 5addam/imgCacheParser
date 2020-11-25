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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private final String JPG_HEADER_STRING = "-1 -40 -1 ";
    private final String JPG_FOOTER_STRING = "-1 -39";
    final private static String SUB_STRING = "/ l o c a l / i m a g e / i t e m /";
    private int fileSize;
    private int header_count = 0;
    private static final byte[] JPG_HEADER = new byte[]{-40, -1};
  
    private List<Thumbnails> recoveredThumbnails;

    public ThumbnailsRecovery() {
        this.recoveredThumbnails = recoveredThumbnails;
    }

    //Find all indices of a substring in a given string
      private List<Integer> allIndices(String bigString, String subString) {
        List<Integer> listIndex = new ArrayList<>();
        int i = bigString.indexOf(subString, 0);
        while (i >= 0) {
            listIndex.add(i);
            i = bigString.indexOf(subString, i + 1);
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
            List<Integer> jpgHeaderHits = allIndices(str, JPG_HEADER_STRING);
            List<Integer> jpgFooterHits = allIndices(str, JPG_FOOTER_STRING);
//            while (fileInputStream.available() > 2) {
//                byte[] jpgHead = new byte[3];
//                fileInputStream.read(jpgHead, 0, 2);
////                Arrays.sort(jpgHead);
//
//                if (jpgHead[0] == (byte) -1 && jpgHead[1] == (byte) -40 ) {
//                    header_count++;
//                }
//            }

            System.out.println("Headers Found: " + String.valueOf(jpgHeaderHits.size()));
            System.out.println("Footers Found: " + String.valueOf(jpgFooterHits.size()));

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ThumbnailsRecovery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThumbnailsRecovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
