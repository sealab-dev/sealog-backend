ALTER TABLE posts
ADD FULLTEXT INDEX IF NOT EXISTS idx_ft_title_excerpt (title, excerpt);
