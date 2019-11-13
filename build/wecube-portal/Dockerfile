FROM nginx
LABEL maintainer = "Webank CTB Team"
RUN mkdir -p /root/app/ui-resources
COPY wecube-portal/dist /root/app/
COPY build/wecube-portal/nginx.tpl /etc/nginx/conf.d/nginx.tpl
