# flood risk monitor

java library for calculating flood risk based on open data sources.
monitors cities every 2 hours and sends notifications via telegram and email.

## features

- calculates flood risk from 3 open apis (openweathermap, open-meteo)
- color-coded risk: green / yellow / red
- telegram notifications every 2 hours
- email notifications via yandex smtp
- monitors london, edinburgh, brighton
- resilience4j retry with fallback
- rest api documented with openapi 3 (swagger)
- jwt authentication

## how it works

the library uses a weighted sum formula:

    risk = (historicalrisk * 0.4) + (georisk * 0.3) + (weatherrisk * 0.3)

where:

- historicalrisk (40%) - flood events in the last 10 years (open-meteo archive)
- georisk (30%) - elevation above sea level (open-meteo elevation)
- weatherrisk (30%) - current rainfall and wind speed (openweathermap)

## quick start

### prerequisites

- java 17+
- maven 3.9+
- api keys (see configuration section)

### 1. clone the repository

    git clone https://github.com/mtvvrm11/flood-risk-monitor.git
    cd flood-risk-monitor

### 2. create secrets.properties

copy the example file and fill in your values:

    cp secrets.properties.example secrets.properties

then edit secrets.properties with your api keys.

### 3. build and run

    mvn clean package
    mvn exec:java

or run main.java directly in intellij idea.

## configuration

create a file secrets.properties in the project root with the following keys:

| key | description | where to get |
|-----|-------------|--------------|
| openweather.api.key | openweathermap api key | openweathermap.org/api |
| telegram.bot.token | telegram bot token | t.me/botfather |
| telegram.chat.id | telegram chat id | t.me/userinfobot |
| yandex.username | yandex email address | - |
| yandex.app.password | yandex app password | id.yandex.ru/security/app-passwords |
| email.to | recipient email address | - |

never commit secrets.properties to git.

## project structure

    flood-risk-monitor/
    src/main/java/org/example/
        main.java
        floodriskmonitor.java
        config/
            secretsloader.java
            asyncconfig.java
            swaggerconfig.java
            securityconfig.java
        model/
            coordinates.java
            riskscore.java
            weatherdata.java
            geodata.java
            historicaldata.java
            store.java
            notification.java
        provider/
            weatherprovider.java
            geoprovider.java
            historicalprovider.java
            impl/
                openweathermapprovider.java
                openmeteoelevationprovider.java
                openmeteoarchiveprovider.java
        service/
            riskcalculationservice.java
            notificationservice.java
        notification/
            notifier.java
            telegramnotifier.java
            emailnotifier.java
            consolenotifier.java
        scheduler/
            floodriskscheduler.java
    secrets.properties.example
    .gitignore
    pom.xml
    license
    readme.md

## technologies

| category | technology |
|----------|-----------|
| language | java 17 |
| framework | spring boot 3.2 |
| http client | resttemplate |
| json | jackson |
| resilience | resilience4j (retry, fallback) |
| email | javamailsender (yandex smtp) |
| telegram | telegram bot api |
| scheduler | scheduledexecutorservice |
| build | maven |
| testing | junit 5, mockito |
| documentation | openapi 3 (swagger) |

## api endpoints

when running as spring boot application:

| method | endpoint | description |
|--------|----------|-------------|
| get | /api/risks/current?latitude=&longitude= | get risk for coordinates |
| post | /api/risks/batch | get risks for multiple locations |
| get | /api/stores | get all stores |
| post | /api/stores | create a store |
| delete | /api/stores/{id} | delete a store |

swagger ui: http://localhost:8080/swagger-ui.html

## license

mit license. see license file for details.

## author

- github: https://github.com/mtvvrm11
- linkedin: https://linkedin.com/in/mtvvrm