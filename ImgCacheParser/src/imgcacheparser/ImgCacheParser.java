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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Air
 */
public class ImgCacheParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       ImageRecovery rm = new ImageRecovery();
       rm.recoverImages();
       rm.displayData();
        // TODO code application logic here
    }
}
