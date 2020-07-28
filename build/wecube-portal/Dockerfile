FROM nginx
LABEL maintainer = "Webank CTB Team"
RUN mkdir -p /root/app/ui-resources
RUN mkdir -p /root/wecube-docs
COPY wecube-portal/dist /root/app/
COPY build/wecube-portal/nginx.tpl /etc/nginx/conf.d/nginx.tpl
COPY wecube-docs /root/wecube-docs/