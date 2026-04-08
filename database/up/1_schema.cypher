
// Add unique constraint to Directory.path
CREATE CONSTRAINT DirectoryPathUnique
IF NOT EXISTS
FOR (dir:Directory)
REQUIRE dir.path IS UNIQUE;

// Unique constraint to Image.path
CREATE CONSTRAINT ImagePathUnique
IF NOT EXISTS
FOR (img:Image)
REQUIRE img.path IS UNIQUE;

// Unique constraint to Video.path
CREATE CONSTRAINT VideoPathUnique
IF NOT EXISTS
FOR (video:Video)
REQUIRE video.path IS UNIQUE;

