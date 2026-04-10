
## Context

We're removing the current 'contentHash' from the Image and Video nodes,
instead, we're going to use fileSize and lastModified timestamp as a
fingerprint to detect if a file has changed.

## Implementation steps

1. Remove the 'contentHash' field from the Image and Video nodes in the schema.
2. Add `Long fileSize` and `Instant lastModified` fields to the Image and Video nodes in the schema.
3. Create a `scanner.service.FingerprintService` class with a method
   `for(Path absPath)`. It should return a `Fingerprint` record containing the file 
   size and last modified timestamp.
4. Update LocalMediaScanner#onImageFound and LocalMediaScanner#onVideoFound to
   use the `FingerprintService` to generate the fingerprint for each found file.
   - When a file is found, call `FingerprintService.for(absPath)` instead of current
     call to content hash generation method.
   - If media file doesn't exist in database, is a new file. Follow existing flow.
   - If media file exists in database, compare the new fingerprint with the stored one:
     - If they are the same, assume file hasn't changed. Follow existing flow
     - If they are different, assume file has changed. Follow existing flow

## Testing

1. Scanner behavior tests (mock `FingerprintService`):
   - Same fingerprint as DB -> media is treated as unchanged.
   - Different fingerprint than DB -> media is treated as updated.
   - Media not found in DB -> media is treated as new.
2. `FingerprintService` tests (real files from test resources):
   - Verify it returns `fileSize` and `lastModified` for a known file.
   - Verify values are stable for repeated reads of the same file.
   - For these tests, Copilot can use placeholder expected values for
     `fileSize` and `lastModified`. I will run/debug locally and update
     those expected values to the correct ones.

