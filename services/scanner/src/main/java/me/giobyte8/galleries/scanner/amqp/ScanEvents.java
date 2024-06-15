package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dto.FDiscoveryEventType;
import me.giobyte8.galleries.scanner.dto.ScanEventType;
import me.giobyte8.galleries.scanner.dto.ScanRequest;

public interface ScanEvents {

    void onScanProcessEvent(ScanRequest scanReq, ScanEventType evtType);

    void onFileDiscoveryEvent(
            ScanRequest scanReq,
            FDiscoveryEventType evtType,
            String filePath
    );
}
