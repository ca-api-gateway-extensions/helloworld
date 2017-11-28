# About
The gateway Helloworld assertion repository is an example repository for a modular assertion.

# Build
In order to build the modular assertion run `gradle build`.
 
This will compile, test, and create the aar file. It will be available in build/libs

# Run
To this after you build the assertion:

1) Add a gateway license xml to ```docker/compose/license```
2) cd into docker/compose
3) run docker-compose up

This will start a docker image with the assertion deployed on it.
