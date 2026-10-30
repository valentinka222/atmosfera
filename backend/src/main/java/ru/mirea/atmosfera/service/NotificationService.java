package ru.mirea.atmosfera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.mirea.atmosfera.entity.Appointment;

import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final String from;
    private final String baseUrl;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${app.mail.from}") String from,
                               @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    public void sendBookingConfirmation(Appointment a) {
        String text = """
                Здравствуйте, %s!

                Вы записаны в салон красоты «Атмосфера».

                Услуга: %s
                Мастер: %s
                Дата и время: %s
                Стоимость: %s руб.

                Отменить запись можно не позднее чем за 2 часа до начала:
                %s/cancel.html?token=%s
                """.formatted(
                a.getClient().getFullName(),
                a.getService().getName(),
                a.getMaster().getFullName(),
                a.getStartAt().format(FORMAT),
                a.getService().getPrice(),
                baseUrl,
                a.getCancelToken());

        send(a.getClient().getEmail(), "Запись в салон «Атмосфера» подтверждена", text);
    }

    public void sendCancellation(Appointment a) {
        String text = """
                Здравствуйте, %s!

                Ваша запись на услугу «%s» (%s) отменена.

                Будем рады видеть вас снова!
                """.formatted(
                a.getClient().getFullName(),
                a.getService().getName(),
                a.getStartAt().format(FORMAT));

        send(a.getClient().getEmail(), "Запись в салон «Атмосфера» отменена", text);
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Письмо отправлено: «{}» → {}", subject, to);
        } catch (MailException e) {
            // Запись уже создана — из-за неотправленного письма отменять её не нужно
            log.warn("Не удалось отправить письмо на {}: {}", to, e.getMessage());
        }
    }
}