sslproxy
========

`sslproxy` is a Jetty based proxy-server to serve image files from non-secure domains, securely to one's client.
Thus, clients at https://example.com can access a resource at http://someotherdomain.com/image.png over SSL using
a proxy at say, https://proxy.example.com/hash/key.

This allows to make sure that your users always view the green lock and never a yellow lock in the browser's address
bar due to insecure content.

Features
--------
* Very light weight and fast proxy server
* Adds cache headers for one year
* Rejects all non-image proxy requests
* Contains security check to disallow non-genuine clients
* In-built memory caching using Google Guava
* Display of cache stats via simple HTML call

Builds
------

The project is still under development.

Dependencies
------------

`sslproxy` depends on the following frameworks directly

* Jetty server at eclipse.org
* Google guava for caching
* `jerry` framework to reduce boiler-plate code

Versioning
----------

For transparency and insight into our release cycle, and for striving to maintain backward compatibility, 
`sslproxy` will be maintained under the Semantic Versioning guidelines as much as possible.

Releases will be numbered with the follow format:

`<major>.<minor>.<patch>`

And constructed with the following guidelines:

* Breaking backward compatibility bumps the major
* New additions without breaking backward compatibility bumps the minor
* Bug fixes and misc changes bump the patch

For more information on SemVer, please visit http://semver.org/.

License
-------
	
Copyright (c) 2013, Sandeep Gupta

The project uses various other libraries that are subject to their
own license terms. See the distribution libraries or the project
documentation for more details.

The entire source is licensed under the Apache License, Version 2.0 
(the "License"); you may not use this work except in compliance with
the LICENSE. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
