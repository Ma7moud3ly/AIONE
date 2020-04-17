package aione.ma7moud3ly.com;

import android.app.Activity;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import aione.ma7moud3ly.com.MainActivity;

public class CopyBinary {
    public String BinPath = "";
    public String DataDir = "";
    public String Binary = "";

    public CopyBinary(String Binary) {
        this.DataDir = MainActivity.data_dir;
        this.Binary = Binary + "_" + arch();
        this.BinPath = this.DataDir + "/" + this.Binary;
    }

    public CopyBinary() {
        this.DataDir = MainActivity.data_dir;
    }

    private static String arch() {
        if (Build.CPU_ABI.contains("arm")) return "arm";
            //else if (Build.CPU_ABI.contains("x64")) return "86";
        else if (Build.CPU_ABI.contains("x86")) return "86";
        else return Build.CPU_ABI;
    }

    public static String arch2() {
        return System.getProperty("os.arch");
    }

    public boolean isBinaryExists() {
        return new File(BinPath).exists();
    }

    public static boolean isBinaryExists(Activity activity, String binary) {
        return new File(activity.getApplicationInfo().dataDir + "/" + binary + "_" + arch()).exists();
    }

    public static String GetArch() {
        return System.getProperty("os.arch") + " | " + Build.CPU_ABI;
    }

    public boolean DelBinary() {
        return new File(BinPath).delete();
    }

    public boolean CopyAsset(Activity activity) {
        try {
            InputStream in = activity.getAssets().open(Binary);
            FileOutputStream out = new FileOutputStream(BinPath);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            return new File(BinPath).setExecutable(true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean CopyExternal(String BinPath) {
        this.BinPath = BinPath;
        try {
            File ofile = new File(BinPath);
            String npath = DataDir + "/" + ofile.getName();
            File nfile = new File(npath);
            InputStream in = new FileInputStream(ofile);
            FileOutputStream out = new FileOutputStream(nfile);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            return nfile.setExecutable(true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean CopyBootstrap(final Activity activity) {
        try {
            InputStream in = activity.getAssets().open("bootstrap-" + arch2());
            FileOutputStream out = new FileOutputStream(MainActivity.data_dir);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unzipBootstrap(String name, File bootstrap_dir, Activity activity) {
        ZipInputStream zis = null;
        try {
            InputStream asset = activity.getAssets().open(name);
            zis = new ZipInputStream(asset);
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(bootstrap_dir, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (zis != null) zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
