# About
The gateway Helloworld assertion repository is an example repository for a modular assertion.

# Build
In order to build the modular assertion run `gradle build`.
 
This will compile, test, and create the aar file. It will be available in build/libs

# Adding Libraries
In order to build modular assertions some gateway jars are required they need to be put into a `lib` directory. The required jars are:
* layer7-common-9.2.00.jar
* layer7-gateway-common-9.2.00.jar
* layer7-gateway-server-9.2.00.jar
  * This is `Gateway.jar`.
* layer7-policy-9.2.00.jar
* layer7-utility-9.2.00.jar
* layer7-gateway-console-9.2.00.jar
  * This is the `Manager.jar` from the policy manager.

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2017 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md