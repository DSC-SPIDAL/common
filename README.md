# common
Common classes used by other projects in DSC-SPIDAL

#Compile instructions

Please follow the following instructions to build this project with maven
This is needed because of an SSL certificate issue with a dependency maven repo

execute the following commands from the root directory of the code

keytool -import -file ./resources/ricecert/cs.rice.edu.cer -keystore /tmp/riceKeyStore

You can change the name of the key store and the path to it if you prefer to.
This command will first ask for a password, provide any password of your choosing with at least 6 characters
then it will show the following

"Trust this certificate? [no]:" 

type y and then press enter. Now the cert has been properly installed.
Next use the following command to compile the code

mvn -Djavax.net.ssl.trustStore=/tmp/riceKeyStore clean install