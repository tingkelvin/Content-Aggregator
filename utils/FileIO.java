package utils;

import java.io.File;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileIO {
    public static String readTxt(String filedir) throws Exception {
        StringBuilder content = new StringBuilder();

        File myObj = new File(filedir);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            content.append(data);
            content.append("\n");
        }
        content.setLength(content.length() - 1);
        myReader.close();

        return content.toString();
    }

    public static void writeTxt(String fileName, String content) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + "-Content.txt", false));
        writer.write(content);
        writer.close();
    }
}
