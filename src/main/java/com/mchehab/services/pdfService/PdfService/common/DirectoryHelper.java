package com.mchehab.services.pdfService.PdfService.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.system.ApplicationHome;
import com.mchehab.services.pdfService.PdfService.PdfServiceApplication;


public class DirectoryHelper {

    private static  Logger _logger = LoggerFactory.getLogger(DirectoryHelper.class);
  

    public static String getRootDirectory() {
        ApplicationHome home = new ApplicationHome(PdfServiceApplication.class);
        String path = home.getSource().getParent();
        if(path.contains("target"))
            return  Paths.get(path).getParent().toString();
        return path;
    }

    public static String joinFromRoot(String path) {
        String out =  getRootDirectory() + "/" + path;
        _logger.info("Joining path and returning {}", out);
        return out;
    }

    public static Boolean createIfNotExists(String directory) {

        File dir = new File(joinFromRoot(directory));
        if (!dir.exists()) {
            _logger.info("Creating directory {}", dir.toString());
            dir.mkdirs();
        }
        return true;
    }

    public static String createFromString(String container, String path, String input) throws IOException {
        FileWriter fileWriter = new FileWriter( joinFromRoot(container) + "/" + path );
        fileWriter.write( input ) ;
        fileWriter.flush();
        fileWriter.close();
        return joinFromRoot(container) + "/" + path;
    }

    public static void clean(String path){
        File file = new File(path);
        file.delete();
    }

    public static String toUrl( String parent, String file , Settings settings ){
        return parent + "/" +file;      
    }

    public static String getUnixFile(){
        return "file:///"; 
    }

    public static String getLocalUrl(String path){
        return getUnixFile() + "/" + getRootDirectory()  + "/"  + path;
    }
}