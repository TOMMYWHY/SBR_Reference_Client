========================================
SBR  Reference Client

Java version 2.1.4
========================================

This is a refactored-release of the Java SBR Reference Client which is compatible with the Eclipse IDE but will also run in NetBeans IDE.

------------------------------
Dependencies
------------------------------

Packaged along with the Reference client code are the following dependencies:

Library Name - purpose

ABR_SecurityTokenManager library -  Provides a WS-Trust client capable of making 'Issue' calls and presenting the resulting SAML Assertion and Proof Token/Cryptographic key to the callee.

The ABR KeyStore Manager library -  A JCE provider that implements support for ABR KeyStores.  Credentials for SBR (and presumably other projects in the future) will be distributed in this format.

Bouncy Castle - Provides cryptographic functionality not present in the default JCE and the ABR KeyStore library.

Metro JAXWS - Provides web service interactions between the client and web service.

Commons-codec - Provides logging and encoding

------------------------------
User's Quick start guide
------------------------------

Select from the various options on the GUI to construct a request (it's a programmer's GUI but it should be fairly straight forward with the right pre-requisite knowledge), or choose to send only a ping request using the checkbox.  Select a keystore file to use.  A sample ABR keystore is provided in the ABR_SecurityTokenManager distribution.

------------------------------
Programmer's Quick start guide
------------------------------

To run the application, you can do one of two things. 
Option one is to build the jar file with the ANT script in the root of the SBR Reference Client Java project and simply run the generated executable file. Please ensure that you use the "SBR Reference Client Java build.xml.launch" on the initial build; this ensures that all external configurations relation to the ANT are saved to the project. 
Option two is to build the project through the IDE by navigating to src/au.gov.sbr.core.client.gui and running the SBRJavaClientApp class as a Java Application, alternately you can also use the SBRJavaClientApp.launch file provided.

GUI - The GUI is done in Swing and SwingX.  It was originally created with the NetBeans editor and is backed by Java Beans which are bound using the Beans Binding component of SwingX.  Non-GUI specific actions are implemented on the backing Java Beans.  For example, the "Send Request" button will call the sendRequest method of the ClientBean object.

Web services proxy and XML bindings - JAX-WS is being used as the Web Services library in this client.  The JAX-WS implementation is Metro because this is already a dependency for the project because of the ABR_SecurityTokenManager API.  The JAX-WS artifacts are generated using the wsimport tool from the JAX-WS RI, however.

SBR Security Implementation - The SBR Security Architecture can not be implemented with any existing web services stack.  As such a work around is being developed as part of the SBR Core Services Requester.  This work around is still in development.  It is implemented as a JAX-WS SOAP Handler and can be found in the au.gov.sbr.core.jaxws.handlers.SigningHandler package.

-----------------
Known issues
-----------------

An IncorrectPasswordException/InvalidKeyException may be encountered when attempting to run the Reference Client. This is related to the Java Cryptography Extensions.  By default Java comes with limited strength cryptography due to country related restrictions. SBR requires the unlimited strength cryptography policy files which can be downloaded from the Java website.
Note that the correct version needs to be downloaded for the version of Java being used. The download file for Java 6 is located under Additional Resources -> Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 6
http://java.sun.com/javase/downloads/index.jsp

Once downloaded unzip the files and review the Readme.txt file for additional information on their installation and use.

In summary, rename and replace the files local_policy.jar and US_export_policy.jar in the folder <java-home>\lib\security with those contained in the zip file. Note that it is likely that administrator access will be required to update the files and that there may be more than one version of Java installed.


-----------------
Work In Progress
-----------------

You can't select or change the endpoint URL at this stage (unless you edit the WSDL and re-generate the JAX-WS classes).

-------------------------------------
Third Party Licence Acknowledgements
-------------------------------------

This product includes third party software, licences have been included in the packages as appropriate.

Before downloading this component you would have already agreed to SBR terms and conditions which include acknowledgement of third party software licences.

In particular, we would like to draw your attention to the fact that "SBR distributes JAX-WS, JAXB, JAXP, Netbeans, Swing and SwingX under the CDDL as permitted under the dual licencing arrangments for these products."

-----------------
Contact
-----------------

Any questions, concerns, comments about this software can be directed to SBRServiceDesk@sbr.gov.au
