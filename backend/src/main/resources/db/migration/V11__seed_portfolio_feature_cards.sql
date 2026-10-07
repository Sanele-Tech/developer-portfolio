WITH initial_cards (
    card_type, title, issuer, description, achieved_on, url, image_url, sort_order
) AS (
    VALUES
        ('CERTIFICATION', 'Python', 'Circuit Breakers',
            'Python certificate from Circuit Breakers.', NULL::DATE,
            'https://credsverse.com/credentials/c27a3563-c893-4254-96b7-28b6dcac2029?preview=1',
            '/images/portfolio/credential-python.svg', 0),
        ('CERTIFICATION', 'Data Science Methodology (DS0103EN)', 'Cognitive Class',
            'IBM Developer Skills Network course in Data Science Methodology.', NULL::DATE,
            'https://courses.cognitiveclass.ai/certificates/4b9e3db1f07f4ab0a095f3b345a31c36',
            '/images/portfolio/credential-methodology.svg', 1),
        ('CERTIFICATION', 'Data Science 101 (DS0101EN)', 'Cognitive Class',
            'IBM Developer Skills Network course in Data Science 101.', DATE '2025-05-20',
            'https://courses.cognitiveclass.ai/certificates/c22488cbbc4344b490a2fa253ea7a2fb',
            '/images/portfolio/credential-data.svg', 2),
        ('CHALLENGE', 'MICT SETA National Skills Challenge', 'Challenge',
            'Participated as a software developer.', NULL::DATE,
            NULL, '/images/portfolio/skills-challenge.svg', 3),
        ('BUSINESS', 'Varsity Apply', 'Founder · Small business',
            'Independent service helping matric students with university and bursary applications. Manages client communications, application tracking, payments, marketing and outreach, and leads a small team.',
            NULL::DATE, 'https://www.tiktok.com/@sanele_varsity_apply',
            '/images/portfolio/varsity-apply.svg', 4),
        ('COMMUNITY', 'Circuit Breakers Club', 'Member · Community',
            'Member of Circuit Breakers Club.', NULL::DATE,
            NULL, '/images/portfolio/circuit-breakers.svg', 5)
)
INSERT INTO achievements (profile_id, title, card_type, issuer, description, achieved_on, url, image_url, sort_order)
SELECT profile.id, card.title, card.card_type, card.issuer, card.description,
       card.achieved_on, card.url, card.image_url, card.sort_order
FROM (SELECT id FROM profile ORDER BY id LIMIT 1) profile
CROSS JOIN initial_cards card
WHERE NOT EXISTS (
    SELECT 1
    FROM achievements existing
    WHERE existing.profile_id = profile.id
      AND (
          (card.url IS NOT NULL AND existing.url = card.url)
          OR (card.url IS NULL AND existing.card_type = card.card_type AND existing.title = card.title)
      )
);
