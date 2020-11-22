/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgcacheparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

/**
 *
 * @author Air
 */
public class ImageRecovery {
//    final private static String subString = "\\x2F\\x00\\x6C\\x00\\x6F\\x00\\x63\\x00\\x61\\x00\\x6C\\x00\\x2F\\x00\\x69\\x00\\x6D\\x00\\x61\\x00\\x67\\x00\\x65\\x00\\x2F\\x00\\x69\\x00\\x74\\x00\\x65\\x00\\x6D\\x00\\x2F\\x00";

    final private static String SUB_STRING = "/ l o c a l / i m a g e / i t e m /";
//    final private static String SUB_STRING = "47 0 108 0 111 0 99 0 97 0 108 0 47 0 105 0 109 0 97 0 103 0 101 0 47 0 105 0 116 0 101 0 109 0 47";
//    final private static String SUB_STRING = "4701080111099097010804701050109097010301010470105011601010109047";
    final private static String HEADER_STRING = "-40 -1";
    final private static int MAXPATH = 200; //100 x UTF16 chars = max path size
    private final String inputFile = "C:\\Users\\mufassirmughal\\Desktop\\imgCacheParser\\imgcache[1].0";
    private int fileSize;
    private int header_count = 0;

    public ImageRecovery() {
    }

//    Find all indices of a substring in a given string
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

            byte[] filecontent = new byte[(int) fileSize];
            String fileString = "";

            fileInputStream.read(filecontent, 0, (int) fileSize);
            String str;

            str = new String(filecontent, StandardCharsets.US_ASCII);
//            String filestr = Arrays.toString(filecontent).replace(",", "");
//            String fileStr = filestr.replace(" ", "");
//            System.out.println(Arrays.toString(filecontent));

            List<Integer> picHits = allIndices(str, SUB_STRING);

            System.out.println("Paths found: " + picHits.size());
            
            Collections.sort(picHits);
            for (Integer hit : picHits) {
                boolean jpgfound = false;
                int charcount = 0;
                int jpgstart = 0;
                String pathname = "";

                fileInputStream.getChannel().position(hit);
                fileInputStream.getChannel().position(hit - 0x4);
                
                byte[] picSize = new byte[4];

                fileInputStream.read(picSize, 0, 4);

                System.out.println(Arrays.toString(picSize));
                System.out.println(ByteBuffer.wrap(picSize).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffl);
                
                

                fileInputStream.getChannel().position(hit);
//                System.out.println(fileInputStream.getChannel().position());

                while (!jpgfound) {
                    byte[] jpg_head = new byte[2];
                    fileInputStream.read(jpg_head, 0, 2);
                    charcount += 2;
                    if (charcount > MAXPATH) {
                        System.out.println("Max no. of char read for path - skipping..");
                        break;
                    }
                    Arrays.sort(jpg_head);
                    if (jpg_head[0] == (byte) -40 && jpg_head[1] == (byte) -1 ) {
                        System.out.println(Arrays.toString(jpg_head));
                        header_count++;
                        jpgfound = true;
                        System.out.println("JPG Start Pos: "+ Integer.toString((int) (fileInputStream.getChannel().position()-2)));
                    }
                }
//                int fromByteArray = ((picSize[0] & 0xFF) << 24)
//                        | ((picSize[1] & 0xFF) << 16)
//                        | ((picSize[2] & 0xFF) << 8)
//                        | ((picSize[3] & 0xFF) << 0);
//
//                System.out.println("INT: " + fromByteArray);
            }
            System.out.println("No. of Headers Found: " + header_count);

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.US_ASCII)));
    }

}
