package com.husttwj.drawablepreview.util;


import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

import java.io.*;


public class FileUtils {

    public static final String CHARSET_NAME = "UTF-8";

    public static final String LOG_FILE_NAME = "draw_preview_log.txt";

    public static String sUserDesktopPath;

    public static String sMainDirPath;

    public static String sTmpFileDirPath;

    public static String sHistoryFileDirPath;


    public static String sPluginInstallDir = null;

    public static String sPluginDir = null;

    public static String sLogFilePath;

    public static void init() {
        if (sLogFilePath != null) {
            return;
        }
        initDir();
        initLogFile();
        initPluginInstallPath();
        OSHelper.getInstance().init();
        LogUtil.d("FileUtils init success");
    }

    private static void initPluginInstallPath() {
        File pluginPath = PluginManager.getPlugin(PluginId.getId("com.husttwj.drawablepreview")).getPath();
        if (pluginPath.exists()) {
            sPluginInstallDir = pluginPath.getParent();
            sPluginDir = pluginPath.getAbsolutePath();
        }
    }

    private static void initDir() {
        final String userHomePath = System.getProperty("user.home");
        sUserDesktopPath = OSHelper.getInstance().getUserDesktopFilePath();
        initMainFileDir(userHomePath);
        initTmpFileDir();
        initHistoryFileDir();

        deleteChildFile(sTmpFileDirPath);
    }

    @NotNull
    public static ByteArrayOutputStream readByteArrayOutputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byte[] buffer = new byte[4096];
            int readLength = -1;
            byteArrayOutputStream = new ByteArrayOutputStream();
            while ((readLength = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, readLength);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                //关闭流
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArrayOutputStream;
    }

    private static void initMainFileDir(String userHomePath) {
        final File file = new File(userHomePath, ".drawable_preview_main");
        sMainDirPath = file.getPath();
        if (!file.exists()) {
            final boolean mkdirs = file.mkdirs();
            if (!mkdirs && sUserDesktopPath.equals(userHomePath)) {
                initMainFileDir(sUserDesktopPath);
            }
        } else if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
    }

    private static void initHistoryFileDir() {
        final File file = new File(sMainDirPath, "historyFile");
        sHistoryFileDirPath = file.getPath();
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
    }

    private static void initTmpFileDir() {
        final File file = new File(sMainDirPath, "tempFile");
        sTmpFileDirPath = file.getPath();
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
    }

    private static void initLogFile() {
        File logFile = new File(sMainDirPath, LOG_FILE_NAME);
        sLogFilePath = logFile.getAbsolutePath();
        if (!logFile.exists()) {
            createLogFile();
        } else {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sLogFilePath), FileUtils.CHARSET_NAME));
                final String timeStart = reader.readLine().trim();
                reader.close();
                final Long lastUpdateTime = Long.valueOf(timeStart);
                if (System.currentTimeMillis() - lastUpdateTime > 3 * 60L * 60 * 24 * 1000) {
                    createLogFile();
                    FileUtils.deleteChildFile(FileUtils.sTmpFileDirPath);
                }
            } catch (Exception e) {
                createLogFile();
            }
        }
    }

    private static void createLogFile() {
        final File file = new File(sLogFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(sLogFilePath));
            bufferedWriter.write(System.currentTimeMillis() + "\n");
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteChildFile(String filePath) {
        deleteChildFile(new File(filePath));
    }

    public static void deleteChildFile(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return;
        }
        final File[] files = file.listFiles();
        for (File f : files) {
            deleteFile(f);
        }
    }

    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
            file.delete();
        } else {
            file.delete();
        }
    }


}