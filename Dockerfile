# Stage 1
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle fatJar --no-daemon

# Stage 2
FROM openjdk:17-jdk-slim
#RUN apt-get update && apt-get install -y curl unzip
#RUN curl -Lo "/tmp/chromedriver.zip" "https://chromedriver.storage.googleapis.com/113.0.5672.63/chromedriver_linux64.zip" && \
#    curl -Lo "/tmp/chrome-linux.zip" "https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F1135561%2Fchrome-linux.zip?alt=media" && \
#    unzip /tmp/chromedriver.zip -d /opt/ && \
#    unzip /tmp/chrome-linux.zip -d /opt/
#COPY /opt/chrome-linux /opt/chrome
#COPY /opt/chromedriver/ /opt/


# Устанавливаем зависимости для Chrome и Chromedriver
#RUN apt-get update && \
#    apt-get install -y wget unzip && \
#    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
#    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
##    apt install chromium-browser && \
#    rm google-chrome-stable_current_amd64.deb && \
#    wget -q https://chromedriver.storage.googleapis.com/114.0.5735.90/chromedriver_linux64.zip && \
#    unzip chromedriver_linux64.zip -d /usr/local/bin && \
#    rm chromedriver_linux64.zip && \
#    apt-get clean && \
#    rm -rf /var/lib/apt/lists/*


WORKDIR /app
COPY --from=build /app/build/libs/what-is-the-price-now-kotlin-1.0-all.jar app.jar