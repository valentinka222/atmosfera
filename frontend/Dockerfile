FROM nginx:1.27-alpine

# Заменяем стандартную настройку nginx на нашу
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Копируем только файлы сайта
COPY index.html cancel.html /usr/share/nginx/html/
COPY css /usr/share/nginx/html/css
COPY js /usr/share/nginx/html/js