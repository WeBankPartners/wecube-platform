FROM nginx
LABEL maintainer = "Webank CTB Team"
RUN mkdir -p /root/app/ui-resources
# RUN mkdir -p /root/wecube-docs
COPY wecube-portal/dist /root/app/
COPY build/wecube-portal/nginx.tpl /etc/nginx/conf.d/nginx.tpl
COPY build/wecube-portal/start_platform_portal.sh /etc/nginx/start_platform_portal.sh
RUN chmod +x /etc/nginx/start_platform_portal.sh
# COPY wecube-docs /root/wecube-docs/
#RUN rm -rf /root/wecube-docs/.git
RUN cat /etc/nginx/conf.d/nginx.tpl > /etc/nginx/nginx.conf
RUN chmod 755 /usr/sbin/nginx
RUN addgroup --system --gid 6000 apps && adduser --uid 6001 --gid 6000 app
RUN chown -R app:apps /root/app && chown -R app:apps /root/wecube-docs && \
        chown -R app:apps /var/cache/nginx && \
        chown -R app:apps /var/log/nginx && \
        chown -R app:apps /etc/nginx
RUN touch /var/run/nginx.pid && \
        chown -R app:apps /var/run/nginx.pid
RUN chmod 777 /root
USER app