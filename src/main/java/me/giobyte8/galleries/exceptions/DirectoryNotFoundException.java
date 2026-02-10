package me.giobyte8.galleries.exceptions;

public class DirectoryNotFoundException extends RuntimeException {
    public DirectoryNotFoundException(String parentPath) {
        super("Directory not found: " + parentPath);
    }
}
