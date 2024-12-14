# Authentication Server

## Description
This will be an authentication server built with Spring Boot.

## Requirements
### Features
* Shall accept a valid username/password and return a JWT access token
* Shall accept an access token refresh request and return a new JWT token if the provided token is not expired
* Shall accept a request to register a new user with a password
  * Newly registered users shall not be allowed to log in until verified
* Shall accept a request to log out with an access token
* Users shall be able to change their password
* Administrators shall be able to add/update/delete roles
* Administrators shall be able to list and search for users
* Administrators shall be able to enable, verify, unlock, add/delete roles and change passwords for users

### Access Tokens 
* Access token shall expire in a configurable period of time
* Access token shall be renewable until expired
* Password shall expire in a configurable period of time
