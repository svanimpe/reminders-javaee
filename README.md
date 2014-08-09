# Reminders

This example application has seen a few iterations over the last few years. I finally managed to include everything I wanted to include, so here is the most complete Java EE 7 example so far! It's currently live, running on GlassFish 4 and MySQL 5.5 and hosted on OpenShift: [https://glassfish-svanimpe.rhcloud.com/reminders/](https://glassfish-svanimpe.rhcloud.com/reminders/)

# Topics

This example covers the following topics:

- JPA:
  - Basic mapping of attributes, collections and relationships.
  - Automatically generated primary keys.
  - Embeddables.
  - EntityManager.
  - JPQL and named queries.
- Bean Validation:
  - Built-in validation constraints.
  - Custom validation constraints and validators.
  - Validation groups.
  - Manual validation using Validator.
- JAX-RS:
  - Resource creation using @Path, @Consumes/@Produces, @GET/@POST/@PUT/@DELETE and Response.
  - Parameters using @PathParam, @QueryParam, @HeaderParam and @DefaultValue.
  - Exception handling using built-in exception types.
  - Exception mapping.
  - Custom MessageBodyReaders and MessageBodyWriters.
  - Handling image uploads, downloads and storage.
- JSON:
  - JsonReader and JsonWriter.
  - JsonObject(Builder) and JsonArray(Builder).
- CDI:
  - @RequestScoped
  - @Inject.
- JTA:
  - @Transactional
- Security:
  - HTTP Basic Authentication using a GlassFish JDBC realm.
  - Security constraints in web.xml.
  - Checking credentials in JAX-RS using SecurityContext.
- Testing:
  - Basic unit testing using JUnit.
  - Testing with mock objects using Mockito.
  - Integration testing using Arquillian.
  - End-to-end testing using Arquillian and JAX-RS Client API.

## Getting started

If you're new to Git and/or Maven, see this [blog post](http://asipofjava.blogspot.be/2014/05/installing-and-running-example-projects.html) on how to install and run my example projects.

## Master

The **master** branch is meant to be run on a local GlassFish installation. In order to run it, you will need to:

- Change the JDBC resource and connection pool settings in `glassfish-resources.xml` to point to an empty schema. Make sure you keep the name `jdbc/reminders`.
- Set up the security realm. You can do this via the GlassFish administration console at [http://localhost:4848](http://localhost:4848). Browse to `Configurations` > `server-config` > `Security` > `Realms` and create a realm with the following settings:
  - Realm Name: `remindersRealm`
  - Class Name: `com.sun.enterprise.security.ee.auth.realm.jdbc.JDBCRealm`
  - JAAS Context: `jdbcRealm`
  - JNDI: `jdbc/reminders`
  - User Table: `USER_PASSWORD`
  - User Name Column: `USERNAME`
  - Password Column: `PASSWORD`
  - Group Table: `USER_ROLES`
  - Group Table User Name Column: `USERNAME`
  - Group Name Column: `ROLES`
  - Password Encryption Algorithm: `SHA-256`
- Change the `IMAGES_BASE_DIR` in the Utilities class to point to an existing directory on your machine.
- Copy the `default.png` image (it's included in the files used for testing) to that directory.
- Deploy.
- The service should now be running on [http://localhost:8080/reminders](http://localhost:8080/reminders).

The tests can be run with a simple `Test Project`. As this will connect to your database, make sure you modify `test-glassfish-resources.xml` in the same way you modified `glassfish-resources.xml`. It's best to use a separate schema for your tests, as every run of the tests will drop and create the generated tables. The tests will run on an embedded GlassFish server. I did not change the ports, so make sure GlassFish is not already running or the embedded server will not be able to start. The test files include a `domain.xml` configuration for the embedded server. This configuration already includes the security realm so no setup is required. Upon running the tests, you should see 126 green lights. This is a complete test suite for the Credentials and Users resources, testing every aspect from simple business logic to validation, persistence and complete use case scenario's. The Lists and Reminders resources don't have test suites yet, but as they are very similar to the Users resource, you might want to try writing these tests yourself, as an exercise.

## OpenShift

The **openshift** branch is a slighty modified version, exactly as it's running on OpenShift. See [this post](http://asipofjava.blogspot.be/2013/09/running-glassfish-4-with-mysql-on.html) if you want to find out how I got GlassFish 4 running op OpenShift.
