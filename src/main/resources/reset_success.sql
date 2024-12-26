TRUNCATE TABLE assistance_success;
ALTER TABLE assistance_success AUTO_INCREMENT = 1;

INSERT INTO assistance_success (publicity_id, publicity_user_id, assistance_id, assistance_user_id, accepted_at)
SELECT
    publicity_id,
    user_id AS publicity_user_id,
    assistance_id,
    user_id AS assistance_user_id,
    updated_at AS accepted_at
FROM
    assistance
WHERE
    status = 1;
