Diffbot KNIME client
====================

Development
-----------

Prerequisites
-------------

You need an eclipse installation which can be the [KNIME SDK](http://www.knime.org/downloads/overview) or a regular eclipse for plugin development. It is recommended to also install m2e, but a Maven3 installation is also sufficient for diffbot-java-client compilation (you will also need to have internet connection). For development the egit extension is also useful.

Clone the repository containing this file, import the projects to eclipse.

The repository contains `/com.diffbot.knime.devel/com.diffbot.knime.target` target platform definition. Set it as a target platform.

Clone the diffbot-java-client repository too from github: https://github.com/aborg0/diffbot-java-client switch to the knime branch.

Install (or at least `package`) the diffbot-java-client: `mvn install` in its `/diffbot-java` folder.

Everything should compile now.

Creating distributable update site
----------------------------------

Open `/com.diffbot.knime.update/site.xml` and click on `Build` or `Build All`. Share the whole content of `/com.diffbot.knime.update` to the users (either as a website or a zip file).

Troubleshooting
===============

This version of Diffbot client uses a version of httpclient that was not in the last release of eclipse orbit, currently we reference a non-released (but stable) version of eclipse orbit for the selected httpclient bundle. Once it gets unavailable, you should find other source for an httpclient bundle, or just save and distribute the current one.