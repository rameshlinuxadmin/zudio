FROM httpd
LABEL Name="Ramesh Aravind" Version="v1.0.0"
WORKDIR /usr/local/apache2/htdocs
COPY zudio-website.zip  .
RUN apt-get update && apt-get install -y unzip \
    && unzip zudio-website.zip \
    && rm zudio-website.zip
EXPOSE 80
CMD ["httpd-foreground"]