package com.kdeveloper.utils;

import com.kdgdev.nxt.utils.FSUtils;
import com.kdgdev.nxt.utils.FileUtils;
import com.kdgdev.nxt.utils.IOUtils;
import com.kdgdev.nxt.utils.MethodData;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kirill on 29.08.14.
 */
public class PatchData {

    private LinkedHashMap<String, String> replaceAllFiles = new LinkedHashMap<>();
    private LinkedHashMap<String, String> copyFile = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashMap<String, String>> replaceinfile = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashMap<String, MethodData>> replacemethod = new LinkedHashMap<>();
    public String appfile = null;
    public String apppackage = null;
    public String mapping = null;

    public PatchData(File in) throws IOException {
        List<String> fileLines = IOUtils.readLines(new FileInputStream(in), Charset.forName("UTF-8"));
        Pattern appfile_p = Pattern.compile("^\\s{0,}appfile\\s+\\<(.*)\\>\\s{0,}\\;");
        Pattern apppackage_p = Pattern.compile("^\\s{0,}apppackage\\s+\\<(.*)\\>\\s{0,}\\;");
        Pattern replaceall_p = Pattern.compile("^\\s{0,}replaceinall\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s{0,}\\;");
        Pattern replacefile_p = Pattern.compile("^\\s{0,}replaceinfile\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s{0,}\\;");
        Pattern copyfile_p = Pattern.compile("^\\s{0,}copyfile\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s{0,}\\;");
        Pattern mapping_p = Pattern.compile("^\\s{0,}mapping\\s+\\\"(.*)\\\"\\s{0,}\\;");
        Pattern replacemethod_p = Pattern.compile("^\\s{0,}methodreplace\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s{0,}\\;");
        for (String line : fileLines) {
            Matcher appfile_m = appfile_p.matcher(line);
            if (appfile_m.matches()) {
                appfile = appfile_m.group(1);
                continue;
            }

            Matcher apppackage_m = apppackage_p.matcher(line);
            if (apppackage_m.matches()) {
                apppackage = apppackage_m.group(1);
                continue;
            }

            Matcher replaceall_m = replaceall_p.matcher(line);
            if (replaceall_m.matches()) {
                replaceAllFiles.put(replaceall_m.group(1), replaceall_m.group(2));
                continue;
            }

            Matcher copyfile_m = copyfile_p.matcher(line);
            if (copyfile_m.matches()) {
                copyFile.put(FileUtils.fixSeparator(copyfile_m.group(1)), FileUtils.fixSeparator(copyfile_m.group(2)));
                continue;
            }

            Matcher replacefile_m = replacefile_p.matcher(line);
            if (replacefile_m.matches()) {
                if (replaceinfile.containsKey(replacefile_m.group(1))) {
                    replaceinfile.get(replacefile_m.group(1)).put(replacefile_m.group(2), replacefile_m.group(3));
                } else {
                    LinkedHashMap<String, String> replacer = new LinkedHashMap<>();
                    replacer.put(replacefile_m.group(2), replacefile_m.group(3));
                    replaceinfile.put(replacefile_m.group(1), replacer);
                }
            }

            Matcher mapping_m = mapping_p.matcher(line);
            if (mapping_m.matches()) {
                mapping = mapping_m.group(1);
            }

            Matcher replacemethod_m = replacemethod_p.matcher(line);
            if (replacemethod_m.matches()) {
                MethodData methodData = new MethodData(replacemethod_m.group(3), replacemethod_m.group(4), replacemethod_m.group(5), replacemethod_m.group(6));
                if (replacemethod.containsKey(replacemethod_m.group(1))) {
                    replacemethod.get(replacemethod_m.group(1)).put(replacemethod_m.group(2), methodData);
                } else {
                    LinkedHashMap<String, MethodData> replacer = new LinkedHashMap<>();
                    replacer.put(replacemethod_m.group(2), methodData);
                    replacemethod.put(replacemethod_m.group(1), replacer);
                }
            }

        }
    }

    @Override
    public String toString() {
        System.out.println(appfile);
        System.out.println(apppackage);
        System.out.println(mapping);
        System.out.println(copyFile);
        System.out.println(replaceAllFiles);
        System.out.println(replaceinfile);
        return super.toString();
    }

    private static final char[] hexChar = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static String unicodeEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >> 7) > 0) {
                sb.append("\\u");
                sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
                sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
                sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
                sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void patch(File here, File smali) throws IOException {
        if (appfile == null && apppackage == null) throw new RuntimeException("Where I can patch it?!");

        for (String fileFrom : copyFile.keySet()) {
            String fileTo = copyFile.get(fileFrom);
            Path pFileFrom = Paths.get(fileFrom.replace("%here%", here.getCanonicalPath()).replace("%smali%", smali.getCanonicalPath()));
            Path pFileTo = Paths.get(fileTo.replace("%here%", here.getCanonicalPath()).replace("%smali%", smali.getCanonicalPath()));
            LOGGER.info("Copying file: " + pFileFrom + " to " + pFileTo);
            Files.copy(pFileFrom, pFileTo, StandardCopyOption.REPLACE_EXISTING);
        }

        List<File> files = FSUtils.walk(smali.getCanonicalPath(), ".smali");
        for (File f : files) {
            for (String key : replaceAllFiles.keySet()) {
                String replaceable_origin = new String(IOUtils.readFile(f), "UTF-8");
                String replaceable = new String(IOUtils.readFile(f), "UTF-8");
                replaceable = replaceable.replace(StringEscapeUtils.unescapeJson(key), unicodeEscape(StringEscapeUtils.unescapeJson(replaceAllFiles.get(key))));
                if (!replaceable_origin.equals(replaceable)) {
                    LOGGER.info("Replacing " + StringEscapeUtils.unescapeJson(key) + " to " + unicodeEscape(StringEscapeUtils.unescapeJson(replaceAllFiles.get(key))) + " in " + f.getName());
                    IOUtils.writeFile(replaceable.getBytes("UTF-8"), f);
                }
            }
        }

        for (String key : replaceinfile.keySet()) {
            File patchable = new File(FileUtils.fixSeparator(key.replace("%here%", here.getCanonicalPath()).replace("%smali%", smali.getCanonicalPath())));
            if (!patchable.exists()) continue;
            LinkedHashMap<String, String> stringStringHashMap = replaceinfile.get(key);
            for (String key_r : stringStringHashMap.keySet()) {
                String replaceable_origin = new String(IOUtils.readFile(patchable), "UTF-8");
                String replaceable = new String(IOUtils.readFile(patchable), "UTF-8");
                replaceable = replaceable.replace(StringEscapeUtils.unescapeJson(key_r), unicodeEscape(StringEscapeUtils.unescapeJson(stringStringHashMap.get(key_r))));
                if (!replaceable_origin.equals(replaceable)) {
                    LOGGER.info("Replacing " + StringEscapeUtils.unescapeJson(key_r) + " to " + unicodeEscape(StringEscapeUtils.unescapeJson(stringStringHashMap.get(key_r))) + " in " + patchable.getName());
                    IOUtils.writeFile(replaceable.getBytes("UTF-8"), patchable);
                }
            }
        }

        for (String key : replacemethod.keySet()) {
            File patchable = new File(FileUtils.fixSeparator(key.replace("%here%", here.getCanonicalPath()).replace("%smali%", smali.getCanonicalPath())));
            if (!patchable.exists()) continue;
            LinkedHashMap<String, MethodData> stringStringHashMap = replacemethod.get(key);
            for (String key_r : stringStringHashMap.keySet()) {
                File methodFile = new File(FileUtils.fixSeparator(key_r.replace("%here%", here.getCanonicalPath()).replace("%smali%", smali.getCanonicalPath())));
                String replaceable_origin = new String(IOUtils.readFile(patchable), "UTF-8");
                String replaceable = new String(IOUtils.readFile(patchable), "UTF-8");
                String method = new String(IOUtils.readFile(methodFile), "UTF-8");

                MethodData md = stringStringHashMap.get(key_r);
                String pat = Pattern.quote(".method " + md.getAccess() + " " + md.getName() + "(" + (md.getParameters().equals("<none>") ? "" : md.getParameters()) + ")" + md.getReturn());
                Pattern ptn = Pattern.compile(pat + "([\\s\\S]*?)\\.end method");
                Matcher m = ptn.matcher(replaceable);
                replaceable = m.replaceAll(unicodeEscape(Matcher.quoteReplacement(method)));
                if (!replaceable_origin.equals(replaceable)) {
                    LOGGER.info("Replacing " + md.getName() + "(" + md.getParameters() + ") in " + patchable.getName());
                    IOUtils.writeFile(replaceable.getBytes("UTF-8"), patchable);
                }
            }
        }

    }

    private final static Logger LOGGER = Logger.getLogger(PatchData.class.getName());
}
