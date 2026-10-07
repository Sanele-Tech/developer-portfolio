ALTER TABLE achievements
    ADD COLUMN image_urls JSONB NOT NULL DEFAULT '[]'::jsonb;

UPDATE achievements
SET image_urls = jsonb_build_array(image_url)
WHERE image_url IS NOT NULL;

ALTER TABLE education
    ADD COLUMN core_modules JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN experience TEXT,
    ADD COLUMN image_urls JSONB NOT NULL DEFAULT '[]'::jsonb;
