package me.giobyte8.galleries.scanner.postprocessors;

import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.stereotype.Service;

@Service
public class EditedVersionsLinker implements DirScanPostProcessor {

    @Override
    public void process(Directory dir) {
        // linking logic here
    }
}
