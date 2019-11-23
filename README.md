# Rsocket full stack app using websocket

* Spring Boot backend
* Rsocket over websocket for browser support (browsers do not support TCP for security reasons)
* MongoDB reactive driver

## how to run Backend
* Start local mongodb running on default port (You can also use docker for same)
* in book-api folder, run `./gradlew bootRun`
* seed data by making `POST` http call to `/seed` endpoint by passing count to create those many books

## how to run UI
* in book-ui folder, run `yarn install` or `npm install`
* run app using `yarn start` or `npm start`

