/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package hash.prac;

import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Library {
    private static String STORE_FILE = "./address.ser";

    public static void main(String[] args) {
        Library lib = new Library();

        File serialPath = new File(STORE_FILE);

        ArrayList<String> list = lib.getFilePathList("./");//;;/tmp/test");

        ArrayList<FileHash> newHashList = lib.calculateHashAll(list);
        ArrayList<FileHash> oldHashList = lib.getOldHashList(serialPath);

        FileDiff fd = new FileDiff();

        lib.calculateDiff(fd, oldHashList, newHashList);

        System.out.println(fd);

        lib.save(newHashList);
    }

    private void save(ArrayList<FileHash> newHashList) {
        try {
            FileOutputStream fout = new FileOutputStream(STORE_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(newHashList);
        } catch (Exception ignored) {
        }
    }

    private void calculateDiff(FileDiff fd, List<FileHash> oldData, List<FileHash> newData) {
        HashMap<String, String> map = new HashMap<>(oldData.size());

        for (FileHash hash : oldData) {
            map.put(hash.getId(), hash.getHashValue());
        }

        for (FileHash hash : newData) {
            if (map.containsKey(hash.getId())) {
                if (map.get(hash.getId()).equals(hash.getHashValue())) {
                    map.remove(hash.getId());
                } else {
                    fd.onModified(hash.getId());
                    map.remove(hash.getId());
                }
            } else {
                fd.onAdded(hash.getId());
            }
        }

        map.keySet().forEach(fd::onDeleted);
    }

    private ArrayList<FileHash> getOldHashList(File filePath) {
        try {
            if (filePath.exists()) {
                //noinspection unchecked
                return (ArrayList<FileHash>) (new ObjectInputStream(new FileInputStream(filePath)).readObject());
            }
        } catch (Exception ignored) {

        }

        return new ArrayList<>();
    }

    private ArrayList<FileHash> calculateHashAll(List<String> pathList) {
        ArrayList<FileHash> fileHashList = new ArrayList<>();

        for (String s : pathList) {
            FileHash fileHash = new FileHash(s, calculateHash(s));
            fileHashList.add(fileHash);
        }

        return fileHashList;
    }

    @Nullable
    String calculateHash(String filePath) {
        FileInputStream fileStream;
        byte[] fileBuffer = new byte[256];
        byte[] hashResult;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fileStream = new FileInputStream(filePath);


            while (fileStream.read(fileBuffer) >= 0) {
                md.update(fileBuffer);
            }

            fileStream.close();

            hashResult = md.digest();
            //System.out.println(Hex.encodeHex(hashResult));

            return new String(Hex.encodeHex(hashResult));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private ArrayList<String> getFilePathList(String dirPath) {
        ArrayList<String> pathListToHash = new ArrayList<>();
        ArrayList<File> dirList = new ArrayList<>();

        dirList.add(new File(dirPath));

        for (int i = 0; i < dirList.size(); i++) {
            File[] currentList = dirList.get(i).listFiles();

            assert currentList != null;
            for (File file : currentList) {
                if (file.isDirectory()) {
                    dirList.add(file);
                } else {
                    pathListToHash.add(file.toString());
                }
            }

        }

        return pathListToHash;
    }

}
