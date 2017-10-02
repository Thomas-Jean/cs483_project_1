all: cbc_encrypt.jar cbc_decrypt.jar ctr_encrypt.jar ctr_decrypt.jar

cbc_encrypt.jar: src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_encrypt.jar src/MANIFEST_CBC_ENCRYPT.MF src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

src/cbc_encrypt.class: src/cbc_encrypt.java src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_encrypt.java

cbc_decrypt.jar: src/cbc_decrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_decrypt.jar src/MANIFEST_CBC_DECRYPT.MF src/cbc_decrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

src/cbc_decrypt.class: src/cbc_decrypt.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_decrypt.java

ctr_encrypt.jar: src/ctr_encrypt.class src/ctr_thread.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm ctr_encrypt.jar src/MANIFEST_CTR_ENCRYPT.MF src/ctr_encrypt.class src/ctr_thread.class src/progcommon/OptionParser.class src/progcommon/ReadPad.class

src/ctr_encrypt.class: src/ctr_encrypt.java src/ctr_thread.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/ctr_encrypt.java

ctr_decrypt.jar: src/ctr_decrypt.class src/ctr_thread.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm ctr_decrypt.jar src/MANIFEST_CTR_DECRYPT.MF src/ctr_decrypt.class src/ctr_thread.class src/progcommon/OptionParser.class src/progcommon/ReadPad.class

src/ctr_decrypt.class: src/ctr_decrypt.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/ctr_decrypt.java

src/ctr_thread.class: src/ctr_thread.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/ctr_thread.java

src/progcommon/ReadPad.class: src/progcommon/ReadPad.java src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/progcommon/ReadPad.java

src/progcommon/OptionParser.class: src/progcommon/OptionParser.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/progcommon/OptionParser.java

clean:
	rm -f src/*.class
	rm -f progcommon/*.class

scrub: clean
	rm -f *.jar