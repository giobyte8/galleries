package me.giobyte8.galleries.exceptions;

public class DirectoryNotFoundException extends RuntimeException {
    public DirectoryNotFoundException(String msg) {
        super("Directory not found: " + msg);
    }
}
