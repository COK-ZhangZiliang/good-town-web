-- 清空当前的 monthly_summary 表
TRUNCATE TABLE monthly_summary;
ALTER TABLE monthly_summary AUTO_INCREMENT = 1;

-- 插入宣传用户统计
INSERT INTO monthly_summary (month, region, town_id, type, total_publicity_users, total_assistance_users)
SELECT
    DATE_FORMAT(p.created_at, '%Y%m') AS month,
    CONCAT(t.province, '-', t.city) AS region,
    p.town_id,
    p.type,
    COUNT(DISTINCT p.user_id) AS total_publicity_users,
    0 AS total_assistance_users
FROM
    publicity p
        JOIN
    towns t ON p.town_id = t.town_id
GROUP BY
    month, region, p.town_id, p.type;

-- 更新助力用户统计
UPDATE monthly_summary ms
    JOIN (
        SELECT
            DATE_FORMAT(a.created_at, '%Y%m') AS month,
            CONCAT(t.province, '-', t.city) AS region,
            p.town_id,
            p.type,
            COUNT(DISTINCT a.user_id) AS total_assistance_users
        FROM
            assistance a
                JOIN
            publicity p ON a.publicity_id = p.publicity_id
                JOIN
            towns t ON p.town_id = t.town_id
        WHERE
            a.status = 1 -- 只统计已同意的助力
        GROUP BY
            month, region, p.town_id, p.type
    ) AS a_summary
    ON
        ms.month = a_summary.month
            AND ms.region = a_summary.region
            AND ms.town_id = a_summary.town_id
            AND ms.type = a_summary.type
SET
    ms.total_assistance_users = a_summary.total_assistance_users;


-- 补充 assistance_success 表中存在，但 monthly_summary 表中不存在的数据
INSERT INTO monthly_summary (month, region, town_id, type, total_publicity_users, total_assistance_users)
SELECT
    a_summary.month,
    a_summary.region,
    a_summary.town_id,
    a_summary.type,
    0 AS total_publicity_users, -- 如果宣传数据不存在，则为 0
    a_summary.total_assistance_users
FROM (
         SELECT
             DATE_FORMAT(a.accepted_at, '%Y%m') AS month,
             CONCAT(t.province, '-', t.city) AS region,
             p.town_id,
             p.type,
             COUNT(DISTINCT a.assistance_user_id) AS total_assistance_users
         FROM
             assistance_success a
                 JOIN
             publicity p ON a.publicity_id = p.publicity_id
                 JOIN
             towns t ON p.town_id = t.town_id
         GROUP BY
             month, region, p.town_id, p.type
     ) AS a_summary
         LEFT JOIN monthly_summary ms
                   ON
                       ms.month = a_summary.month
                           AND ms.region = a_summary.region
                           AND ms.town_id = a_summary.town_id
                           AND ms.type = a_summary.type
WHERE ms.month IS NULL;