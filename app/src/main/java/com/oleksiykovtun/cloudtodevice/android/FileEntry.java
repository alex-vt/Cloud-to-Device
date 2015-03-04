package com.oleksiykovtun.cloudtodevice.android;

/**
 * FileEntry
 */
public class FileEntry implements Comparable<FileEntry> {

    private String path;
    private String modified;
    private String revision;

    public FileEntry(String path, String modified, String revision) {
        this.path = path;
        this.modified = modified;
        this.revision = revision;
    }

    public int compareTo(FileEntry other) {
        return this.path.compareTo(other.path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

}
