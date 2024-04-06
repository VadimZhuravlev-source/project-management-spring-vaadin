ALTER TABLE user_projects
DROP COLUMN IF EXISTS version;
ALTER TABLE user_projects
ADD version INT DEFAULT 1;