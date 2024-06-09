
// Add unique constraint to Directory.path
CREATE CONSTRAINT DirectoryPathUnique
IF NOT EXISTS
FOR (dir:Directory)
REQUIRE dir.path IS UNIQUE;
