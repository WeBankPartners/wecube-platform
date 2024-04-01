FROM nginx
LABEL maintainer = "Webank CTB Team"
RUN mkdir -p /root/app/ui-resources
# RUN mkdir -p /root/wecube-docs
COPY wecube-portal/dist /root/app/
COPY build/wecube-portal/nginx.tpl /etc/nginx/conf.d/nginx.tpl
COPY build/wecube-portal/start_platform_portal.sh /etc/nginx/start_platform_portal.sh
RUN chmod +x /etc/nginx/start_platform_portal.sh
# COPY wecube-docs /root/wecube-docs/
RUN rm -rf /root/wecube-docs/.git