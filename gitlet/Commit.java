package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** The commit class.
 * @author Preston Weber
 **/
public class Commit implements Serializable {
    /** the description of the commit. **/
    private String _desc;
    /** the timestamp of the commit. **/
    private String timeStamp;
    /** the unique commit ID. **/
    private String id;
    /** the file list. **/
    private HashMap<String, String> _files;
    /** the parent commit. **/
    private Commit _parent;
    /** Used for merged multiple parents. **/
    private ArrayList<Commit> _parents;
    /** whether this is the initializing commit. **/
    private boolean _init;
    /** the file names. **/
    private ArrayList<String> _fileNames;
    /** the File file list. **/
    private File[] _fileList;

    /**
     * The method to create a new commit.
     * @param desc the description
     * @param files the file list
     * @param fileNames the file names
     * @param fileList the File file list
     * @param init whether this is the initializing commit
     * @param parent the parent commit
     * @param repo the repository
     * @throws IOException
     */
    Commit(String desc, HashMap<String, String> files,
           ArrayList<String> fileNames,
           File[] fileList, boolean init,
           Commit parent, Repo repo) throws IOException {
        _init = init;
        _desc = desc;
        _files = files;
        _parent = parent;
        _fileNames = fileNames;
        _fileList = fileList;
        Date date;
        if (init) {
            timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            date = new Date();
            timeStamp = DATE_FORMAT.format(date) + " -0800";
        }

        ArrayList<String> ogFiles = new ArrayList<String>();
        if (!init) {
            ogFiles = makeCopy(_fileNames);
        }

        ArrayList<String> transferred = new ArrayList<String>();
        if (_parent != null && _parent._fileNames != null) {
            for (int i = 0; i < _parent._files.size(); i++) {
                if (!_files.containsKey(_parent._fileNames.get(i))) {
                    String file = _parent._fileNames.get(i);
                    if (repo.notMark(file)) {
                        _files.put(file, _parent._files.get(file));
                        _fileNames.add(file);
                        transferred.add(file);
                    }
                }
            }
        }

        id = createUID();
        Files.createDirectories(Paths.get(Utils.getCWD()
                + "/.gitlet/commits/" + id));
        if (!init) {
            placeInCommitDir(_parent.id(), transferred);
            for (int i = 0; i < _fileList.length; i++) {
                Files.copy(new File(Utils.getCWD() + "/.gitlet/staging/"
                                + ogFiles.get(i)).toPath(),
                        new File(Utils.getCWD()
                                + "/.gitlet/commits/" + id
                                + "/" + ogFiles.get(i)).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                File stageFile = new File(Utils.getCWD()
                        + "/.gitlet/staging/" + ogFiles.get(i));
                stageFile.delete();
            }
        }
    }

    /**
     * The method to create a new commit for a merged commit.
     * @param desc the description
     * @param files the file list
     * @param fileNames the file names
     * @param fileList the File file list
     * @param init whether this is the initializing commit
     * @param parents the parents commit
     * @param repo the repository
     * @throws IOException
     */
    Commit(String desc, HashMap<String, String> files,
           ArrayList<String> fileNames,
           File[] fileList, boolean init,
           ArrayList<Commit> parents, Repo repo) throws IOException {
        _init = init;
        _desc = desc;
        _files = files;
        _parents = parents;
        _parent = parents.get(0);
        _fileNames = fileNames;
        _fileList = fileList;
        Date date;
        if (init) {
            timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            date = new Date();
            timeStamp = DATE_FORMAT.format(date) + " -0800";
        }

        ArrayList<String> ogFiles = new ArrayList<String>();
        if (!init) {
            ogFiles = makeCopy(_fileNames);
        }

        ArrayList<String> transferred = new ArrayList<String>();
        if (_parent != null && _parent._fileNames != null) {
            for (int i = 0; i < _parent._files.size(); i++) {
                String file = _parent._fileNames.get(i);
                if (repo.notMark(file) && !_fileNames.contains(file)) {
                    _files.put(file, _parent._files.get(file));
                    _fileNames.add(file);
                    transferred.add(file);
                }
            }
        }

        id = createUID();
        Files.createDirectories(Paths.get(Utils.getCWD()
                + "/.gitlet/commits/" + id));
        if (!init) {
            placeInCommitDir(_parent.id(), transferred);
            for (String name : ogFiles) {
                Files.copy(new File(Utils.getCWD() + "/.gitlet/staging/"
                                + name).toPath(),
                        new File(Utils.getCWD()
                                + "/.gitlet/commits/" + id
                                + "/" + name).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                File stageFile = new File(Utils.getCWD()
                        + "/.gitlet/staging/" + name);
                stageFile.delete();
            }
        }
    }

    /**
     * Creates the commitID.
     * @return the id
     */
    public String createUID() {
        String parents = "";
        String fMap = "";
        if (!_init) {
            parents = _parent.id;
            fMap = _files.values().toString();
        }
        id = Utils.sha1(_desc, timeStamp, fMap, parents);
        return id;
    }

    /**
     * The Remove class to remove files.
     * @param theFiles the files
     * @return a copy
     */
    public ArrayList<String> makeCopy(ArrayList<String> theFiles) {
        ArrayList<String> copy = new ArrayList<String>();
        for (int i = 0; i < theFiles.size(); i++) {
            copy.add(theFiles.get(i));
        }
        return copy;
    }

    /**
     * Places parent commit files in new commit directory,
     * overwritten by new file if exists later.
     * @param parentID the parents commit ID
     * @param fileNames files to be added
     */
    public void placeInCommitDir(String parentID, ArrayList<String> fileNames)
            throws IOException {
        if (fileNames != null && fileNames.size() > 0) {
            for (String name : fileNames) {
                Files.copy(new File(Utils.getCWD() + "/.gitlet/commits/"
                                + parentID + "/" + name).toPath(),
                        new File(Utils.getCWD()
                                + "/.gitlet/commits/" + id
                                + "/" + name).toPath());
            }
        }
    }

    /** Returns commit ID. **/
    public String id() {
        return id;
    }

    /** Returns timestamp of commit. **/
    public String timestamp() {
        return timeStamp.toString();
    }

    /** Returns commit description. **/
    public String desc() {
        return _desc;
    }

    /** Returns parent of commit. **/
    public Commit getParent() {
        return _parent;
    }

    /** Returns parents of merged commit. **/
    public ArrayList<Commit> getParents() {
        return _parents;
    }

    /**
     * Formats parents IDs for log command.
     * @return parents IDs formatted for log
     */
    public String getParentsSeven() {
        String parentsID = "";
        for (int i = 0; i < _parents.size(); i++) {
            parentsID += _parents.get(i).id().substring(0, 7);
            parentsID += " ";
        }
        String s = parentsID.trim();
        return s;
    }

    /** Returns file names. **/
    public ArrayList<String> getFileNames() {
        return _fileNames;
    }

    /**
     * Returns the list of files.
     * @return File list
     */
    public File[] getFileList() {
        return _fileList;
    }

    /** Returns the list of files. **/
    public HashMap<String, String> getFiles() {
        return _files;
    }

    /** The formatted date. **/
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
}
