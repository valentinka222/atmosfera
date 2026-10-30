-- Секретный токен для отмены записи по ссылке из письма.
-- Существующим записям значение сгенерируется автоматически (DEFAULT).
ALTER TABLE appointment
    ADD COLUMN cancel_token UUID NOT NULL DEFAULT gen_random_uuid();

CREATE UNIQUE INDEX ux_appointment_cancel_token ON appointment (cancel_token);