-- 데모/평가용 초기 운영자 계정. 회원가입 API는 과제 범위 밖으로 판단해 시드로 대체한다.
-- (docs/adr/0012-operator-seed-no-signup.md 참고)
-- email: operator@glowuprizz.com / password: glowup1234!
-- 아래 해시는 BCrypt(strength 10)로 생성한 "glowup1234!"의 해시값이다.
INSERT INTO operators (email, password_hash, created_at)
VALUES (
    'operator@glowuprizz.com',
    '$2a$10$idDYC8qpkYL..EoAy45HTuNNB1Zy/StSs2NUSBgx6BUHtk7YF55cu',
    NOW()
);
