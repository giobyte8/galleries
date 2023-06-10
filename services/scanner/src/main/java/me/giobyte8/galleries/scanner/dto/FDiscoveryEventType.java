package me.giobyte8.galleries.scanner.dto;

public enum FDiscoveryEventType {

    /** Previously scanned file was found, content's did not change */
    EXISTENT_FILE_FOUND,

    /** A file not previously scanned (Not in DB) was found */
    NEW_FILE_FOUND,

    /** A previously scanned file was found, but contents changed */
    FILE_CHANGED,

    /** A previously scanned file does not exist anymore*/
    FILE_NOT_FOUND
}
