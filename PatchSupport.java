package com.kdeveloper.utils;

import com.kdgdev.nxt.utils.FileUtils;
import com.kdgdev.nxt.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kirill on 29.08.14.
 */
public class PatchSupport {

    public static List<String> getPatchList(File in, File gitDir) throws IOException {
        List<String> fileLines = IOUtils.readLines(new FileInputStream(in), Charset.forName("UTF-8"));
        List<String> patchList = new ArrayList<>();
        Pattern mainPattern = Pattern.compile("include\\s+\\\"(.*)\\\"\\s{0,}\\;");

        for(String line : fileLines) {
            Matcher matcher = mainPattern.matcher(line);
            if(matcher.matches()) {
                patchList.add(FileUtils.fixSeparator(matcher.group(1).replace("%translation_dir%", gitDir.getCanonicalPath())));
            }
        }

        return patchList;
    }


}
