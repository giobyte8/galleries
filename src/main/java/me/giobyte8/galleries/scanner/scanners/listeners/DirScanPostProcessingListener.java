package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.scanner.postprocessors.DirScanPostProcessor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirScanPostProcessingListener implements ScanEventsListener {

    private final List<DirScanPostProcessor> postProcessors;

    @Override
    public void onScanCompleted(Directory dir) {
        postProcessors.forEach(postProcessor -> postProcessor.process(dir));
    }
}
