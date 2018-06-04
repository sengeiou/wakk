package com.ubtrobot.dance;

import android.content.Context;
import android.content.res.AssetManager;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DanceFileHelper {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DanceFileHelper");

    private final String FILE_NAME = "dance.json";
    private final String DIRECTORY;
    private final String ABSOLUTE_PATH;
    private final String ASSETS_FILE_NAME;

    private Context mContext;

    public DanceFileHelper(Context context, String assetsDanceFileName) {
        mContext = context.getApplicationContext();
        ASSETS_FILE_NAME = assetsDanceFileName;

        String appAbsolutePath = mContext.getFilesDir().getAbsolutePath();
        DIRECTORY = appAbsolutePath + "/dance";
        ABSOLUTE_PATH = DIRECTORY + "/" + FILE_NAME;
    }

    public void downloadFile() {
        // TODO 下载舞蹈文件
    }

    public String read() {
        String file = readFile();
        if (file.length() == 0) {
            writeFile(readAssetsFile());
            file = readFile();
        }

        return file;
    }

    private String readFile() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(ABSOLUTE_PATH);
            br = new BufferedReader(fr);
            StringBuffer buffer = new StringBuffer();
            try {
                for (String next = br.readLine();
                     next != null && next.length() >= 0 && buffer.append(next) != null;
                     next = br.readLine()) {
                }
            } catch (IOException e) {
                LOGGER.e("File:"+ ABSOLUTE_PATH +" read error.");
            }

            return buffer.toString();
        } catch (FileNotFoundException e) {
            LOGGER.e("File:"+ ABSOLUTE_PATH +" is not exits.");
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }

            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    fr = null;
                }
            }
        }

        return "";
    }

    private String readAssetsFile() {
        AssetManager manager = mContext.getAssets();
        InputStream inputStream;
        try {
            inputStream =  manager.open(ASSETS_FILE_NAME);
        } catch (IOException e) {
            LOGGER.e("Dance file(assets) is not exits.");
            return "";
        }

        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder builder = new StringBuilder();

        try {
            for (String next = reader.readLine();
                 next != null && next.length() > 0 && builder.append(next) != null;
                 next = reader.readLine()) {
            }

            return builder.toString();
        } catch (IOException e) {
            LOGGER.e("Please check dance json file.");
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    reader = null;
                }
            }

            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    streamReader = null;
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    inputStream = null;
                }
            }
        }

        return "";
    }

    private void writeFile(String fileContent) {
        if (!createFile()) {
            return;
        }

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(ABSOLUTE_PATH, false);
            bw = new BufferedWriter(fw);
            bw.write(fileContent);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            LOGGER.e("File writer fail.");
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }

            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    fw = null;
                }
            }
        }
    }

    private boolean createFile() {
        File file = new File(DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }

        String path = DIRECTORY + "/" + FILE_NAME;
        File dir = new File(path);
        if (dir.exists()) {
            return true;
        }

        try {
            return dir.createNewFile();
        } catch (IOException e) {
            LOGGER.e("File:" + path + " create fail.");
        }

        return false;
    }
}
