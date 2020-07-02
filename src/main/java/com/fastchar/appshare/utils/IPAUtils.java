package com.fastchar.appshare.utils;

import com.dd.plist.*;
import com.fastchar.core.FastFile;
import com.fastchar.utils.FastFileUtils;
import org.xml.sax.SAXException;

import javax.sound.midi.Soundbank;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 17:05
 */
public class IPAUtils {

    private static File getPlistFile(File file, String unzipDirectory) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        File result = null;
        File unzipFile = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            unzipFile = new File(unzipDirectory + "/" + name);
            if (unzipFile.exists()) {
                unzipFile.delete();
            }
            unzipFile.mkdir();
            Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
            ZipEntry entry = null;
            String entryName = null;
            String names[] = null;
            int length;
            while (zipEnum.hasMoreElements()) {
                entry = zipEnum.nextElement();
                entryName = new String(entry.getName());
                names = entryName.split("\\/");
                length = names.length;
                for (int v = 0; v < length; v++) {
                    if (entryName.endsWith(".app/Info.plist")) { // 为Info.plist文件,则输出到文件
                        input = zipFile.getInputStream(entry);
                        result = new File(unzipFile.getAbsolutePath() + "/Info.plist");
                        output = new FileOutputStream(result);
                        byte[] buffer = new byte[1024 * 8];
                        int readLen = 0;
                        while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
                            output.write(buffer, 0, readLen);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
        }
        return result;
    }


    private static File getMobileProvisionFile(File file, String unzipDirectory) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        File result = null;
        File unzipFile = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            unzipFile = new File(unzipDirectory + "/" + name);
            if (unzipFile.exists()) {
                unzipFile.delete();
            }
            unzipFile.mkdir();
            Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
            ZipEntry entry = null;
            String entryName = null;
            String names[] = null;
            int length;
            while (zipEnum.hasMoreElements()) {
                entry = zipEnum.nextElement();
                entryName = new String(entry.getName());
                names = entryName.split("\\/");
                length = names.length;
                for (int v = 0; v < length; v++) {
                    if (entryName.endsWith(".app/embedded.mobileprovision")) {
                        input = zipFile.getInputStream(entry);
                        result = new File(unzipFile.getAbsolutePath() + "/embedded.mobileprovision");
                        output = new FileOutputStream(result);
                        byte[] buffer = new byte[1024 * 8];
                        int readLen = 0;
                        while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
                            output.write(buffer, 0, readLen);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
        }
        return result;
    }


    private static File getIconFile(File file, String unzipDirectory, String iconName) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        File result = null;
        File unzipFile = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            unzipFile = new File(unzipDirectory + "/" + name);
            if (unzipFile.exists()) {
                unzipFile.delete();
            }
            unzipFile.mkdir();
            Enumeration<? extends ZipEntry> zipEnum = zipFile.entries();
            ZipEntry entry = null;
            String entryName = null;
            String names[] = null;
            int length;
            while (zipEnum.hasMoreElements()) {
                entry = zipEnum.nextElement();
                entryName = entry.getName();
                names = entryName.split("\\/");
                length = names.length;
                for (int v = 0; v < length; v++) {
                    if (entryName.contains(iconName)) {
                        input = zipFile.getInputStream(entry);
                        result = new File(unzipFile.getAbsolutePath() + "/appIcon.png");
                        output = new FileOutputStream(result);
                        byte[] buffer = new byte[1024 * 8];
                        int readLen = 0;
                        while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
                            output.write(buffer, 0, readLen);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
        }
        return result;
    }

    public static File getPlistFile(File ipaFile) throws IOException {
        try {
            int byteread = 0;
            String filename = ipaFile.getAbsolutePath().replaceAll(".ipa", ".zip");
            File zipFile = new File(filename);
            if (ipaFile.exists()) {
                if (!zipFile.exists()) {
                    InputStream inStream = new FileInputStream(ipaFile);
                    FileOutputStream fs = new FileOutputStream(zipFile);
                    byte[] buffer = new byte[1444];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    fs.close();
                }
                return getPlistFile(zipFile, zipFile.getParent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getMobileProvisionFile(File ipaFile) throws IOException {
        try {
            int byteread = 0;
            String filename = ipaFile.getAbsolutePath().replaceAll(".ipa", ".zip");
            File zipFile = new File(filename);
            if (ipaFile.exists()) {
                if (!zipFile.exists()) {
                    InputStream inStream = new FileInputStream(ipaFile);
                    FileOutputStream fs = new FileOutputStream(zipFile);
                    byte[] buffer = new byte[1444];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    fs.close();
                }
                return getMobileProvisionFile(zipFile, zipFile.getParent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getAppIcon(File ipaFile, File plistFile) throws IOException {
        try {
            int byteread = 0;
            String filename = ipaFile.getAbsolutePath().replaceAll(".ipa", ".zip");
            File newfile = new File(filename);
            if (ipaFile.exists()) {
                if (!newfile.exists()) {
                    InputStream inStream = new FileInputStream(ipaFile);
                    FileOutputStream fs = new FileOutputStream(newfile);
                    byte[] buffer = new byte[1444];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    fs.close();
                }
                NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plistFile);
                NSDictionary CFBundleIcons = (NSDictionary) rootDict.get("CFBundleIcons");
                if (CFBundleIcons != null) {
                    if (CFBundleIcons.containsKey("CFBundlePrimaryIcon")) {
                        NSDictionary cfBundlePrimaryIcon = (NSDictionary) CFBundleIcons.get("CFBundlePrimaryIcon");
                        NSArray cfBundleIconFiles = (NSArray) cfBundlePrimaryIcon.get("CFBundleIconFiles");
                        return getIconFile(newfile, newfile.getParent(), cfBundleIconFiles.getArray()[cfBundleIconFiles.count() - 1].toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void clearFile(File ipaFile) {
        try {
            String filename = ipaFile.getAbsolutePath().replaceAll(".ipa", ".zip");
            File zipFile = new File(filename);
            FastFileUtils.forceDelete(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static NSDictionary parseMobileProvisionFile(File file) {
        try {
            StringBuilder plistInfo = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String line = null;
                boolean plist = false;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("<plist")) {
                        plist = true;
                        plistInfo.append("<plist version=\"1.0\">");
                        continue;
                    }
                    if (line.startsWith("</plist")) {
                        plistInfo.append("</plist>");
                        break;
                    }
                    if (plist) {
                        plistInfo.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (NSDictionary) PropertyListParser.parse(plistInfo.toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
