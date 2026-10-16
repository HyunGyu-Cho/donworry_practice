-- registry_event_publication
-- 생성: 2026-10-16 10:46:59
--
-- 규칙
-- * 이 파일은 공유된 뒤 수정하지 않는다. 변경이 필요하면 새 migration 을 만든다.
-- * 한 migration 은 하나의 배포 가능한 스키마 전환을 담는다. SQL 수만으로 나누거나 합치지 않는다.
-- * PostgreSQL 은 DDL 이 트랜잭션 안에서 돌아, 실패하면 이 파일 전체가 롤백된다.
--
-- Spring Modulith Event Publication Registry 의 기술 테이블이다.
-- 내부 비동기 이벤트를 어느 리스너가 처리했는지 기록해, 실패한 것만 골라 재처리한다.
--
-- 이 DDL 은 손으로 쓴 것이 아니라 spring-modulith-events-jdbc 2.1.1 의
--   org/springframework/modulith/events/jdbc/schemas/v2/schema-postgresql.sql
-- 을 그대로 가져왔다. Modulith 버전을 올릴 때는 그 버전의 파일과 대조해
-- 차이가 있으면 새 migration 으로 추가한다.
--
-- v2 구조를 쓴다. use-legacy-structure 를 켜지 않으면 기본이 v2 다.
-- status · completion_attempts · last_resubmission_date 가 v2 에만 있고,
-- 반복 실패 횟수로 관리자 확인 대상을 가려내려면 이 컬럼들이 필요하다.
--
-- 이 테이블은 업무 후보 테이블 34개에 포함되지 않는다(ERD-03).

CREATE TABLE IF NOT EXISTS event_publication
(
  id                     UUID NOT NULL,
  listener_id            TEXT NOT NULL,
  event_type             TEXT NOT NULL,
  serialized_event       TEXT NOT NULL,
  publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date        TIMESTAMP WITH TIME ZONE,
  status                 TEXT,
  completion_attempts    INT,
  last_resubmission_date TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);
