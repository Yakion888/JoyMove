DELETE FROM community_interaction; DELETE FROM moment_like; DELETE FROM family_moment;
DELETE FROM family_plan; DELETE FROM medal_record; DELETE FROM check_in; DELETE FROM notification;
DELETE FROM child_profile; DELETE FROM user WHERE username != 'admin';

INSERT INTO user (username, nickname, password, role, family_role, join_date, total_days, longest_streak) VALUES
('mama', '乐乐妈妈', '$2a$10$DfTO/yl7AgRRzdtYkPFkoesFSG3bvOJUqVVqsxf/idk8JQkiJIMvS', 0, 1, '2026-05-01', 20, 7);
INSERT INTO child_profile (user_id, name, gender, birth_date) VALUES (LAST_INSERT_ID(), '乐乐', 0, '2020-06-15');

INSERT INTO user (username, nickname, password, role, family_role, join_date, total_days, longest_streak) VALUES
('papa', '妞妞爸爸', '$2a$10$DfTO/yl7AgRRzdtYkPFkoesFSG3bvOJUqVVqsxf/idk8JQkiJIMvS', 0, 0, '2026-05-15', 4, 4);
INSERT INTO child_profile (user_id, name, gender, birth_date) VALUES (LAST_INSERT_ID(), '妞妞', 1, '2022-03-20');
