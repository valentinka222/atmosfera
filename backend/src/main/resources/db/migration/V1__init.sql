CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE master (
    id             BIGSERIAL PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    specialization VARCHAR(50)  NOT NULL,
    phone          VARCHAR(20),
    active         BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE service (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    category     VARCHAR(50)   NOT NULL,
    duration_min INT           NOT NULL CHECK (duration_min > 0),
    price        NUMERIC(10,2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE master_service (
    master_id  BIGINT NOT NULL REFERENCES master(id)  ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES service(id) ON DELETE CASCADE,
    PRIMARY KEY (master_id, service_id)
);

CREATE TABLE work_schedule (
    id          BIGSERIAL PRIMARY KEY,
    master_id   BIGINT NOT NULL REFERENCES master(id) ON DELETE CASCADE,
    day_of_week INT    NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time  TIME   NOT NULL,
    end_time    TIME   NOT NULL,
    CHECK (start_time < end_time),
    UNIQUE (master_id, day_of_week)
);

CREATE TABLE client (
    id        BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone     VARCHAR(20)  NOT NULL,
    email     VARCHAR(100) NOT NULL
);

CREATE TABLE appointment (
    id         BIGSERIAL PRIMARY KEY,
    client_id  BIGINT      NOT NULL REFERENCES client(id),
    master_id  BIGINT      NOT NULL REFERENCES master(id),
    service_id BIGINT      NOT NULL REFERENCES service(id),
    start_at   TIMESTAMP   NOT NULL,
    end_at     TIMESTAMP   NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'BOOKED'
               CHECK (status IN ('BOOKED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    CHECK (start_at < end_at)
);


ALTER TABLE appointment
    ADD CONSTRAINT appointment_no_overlap
    EXCLUDE USING gist (
        master_id WITH =,
        tsrange(start_at, end_at) WITH &&
    ) WHERE (status = 'BOOKED');

CREATE INDEX idx_appointment_master_start ON appointment (master_id, start_at);