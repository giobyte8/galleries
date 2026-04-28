package me.giobyte8.galleries.scanner.exceptions;

public class UnsupportedMediaFileException extends Exception {

    public UnsupportedMediaFileException(String fileType) {
        var msg = String.format(
                "Unsupported Media File. Type: %s.",
                fileType
        );

        super(msg);
    }
}
