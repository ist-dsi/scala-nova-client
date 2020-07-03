# scala-nova-client [![license](http://img.shields.io/:license-MIT-blue.svg)](LICENSE)
[![Scaladoc](http://javadoc-badge.appspot.com/pt.tecnico.dsi/scala-nova-client_2.13.svg?label=scaladoc&style=plastic&maxAge=604800)](https://ist-dsi.github.io/scala-nova-client/api/latest/pt/tecnico/dsi/openstack/nova/index.html)
[![Latest version](https://index.scala-lang.org/ist-dsi/scala-nova-client/scala-nova-client/latest.svg)](https://index.scala-lang.org/ist-dsi/scala-nova-client/scala-nova-client)

[![Build Status](https://travis-ci.org/ist-dsi/scala-nova-client.svg?branch=master&style=plastic&maxAge=604800)](https://travis-ci.org/ist-dsi/scala-nova-client)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/9acc6f6bd2d2448c80c6234850dbbe44)](https://www.codacy.com/gh/ist-dsi/scala-nova-client?utm_source=github.com&utm_medium=referral&utm_content=ist-dsi/scala-nova-client&utm_campaign=Badge_Coverage)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9acc6f6bd2d2448c80c6234850dbbe44)](https://www.codacy.com/gh/ist-dsi/scala-nova-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ist-dsi/scala-nova-client&amp;utm_campaign=Badge_Grade)
[![BCH compliance](https://bettercodehub.com/edge/badge/ist-dsi/scala-nova-client)](https://bettercodehub.com/results/ist-dsi/scala-nova-client)


The Scala client for Openstack Nova.

Currently supported endpoints:
  
- [Servers](https://docs.openstack.org/api-ref/compute/#servers-servers) - only list summary and delete.
- [Quota sets](https://docs.openstack.org/api-ref/compute/#quota-sets-os-quota-sets)

[Latest scaladoc documentation](https://ist-dsi.github.io/scala-nova-client/api/latest/pt/tecnico/dsi/openstack/nova/index.html)

## Install
Add the following dependency to your `build.sbt`:
```sbt
libraryDependencies += "pt.tecnico.dsi" %% "scala-nova-client" % "0.0.0"
```
We use [semantic versioning](http://semver.org).

## License
scala-nova-client is open source and available under the [MIT license](LICENSE).
