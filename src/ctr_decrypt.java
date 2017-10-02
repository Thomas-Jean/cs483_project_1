package src;

import progcommon.ReadPad;
import progcommon.OptionParser;
import java.security.SecureRandom;
import java.io.FileOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.IllegalArgumentException;
import java.security.InvalidKeyException;
import java.lang.Math;


/*
Thomas Jean
CS 483 Fall 2017

ctr_decrypt.java take the file information
from OptionParser and ReadPad and then
decrypts a message, the message is then
written to the output file specificed.

*/


public class ctr_decrypt{
	

	public byte[] plaintext;
	public byte[] key;
	public byte[] iv;
	public byte[] ciphertext;

	/*
	The constructor passes the options to the input helpers
	and then removes the IV from the ciphertext and starts
	decyption.
	*/

	ctr_decrypt(String args[]){
		OptionParser op = new OptionParser(args, "ctr_dec");
		ReadPad rp = new ReadPad(op, false);

		ciphertext = rp.input;
		key = rp.key;
		
		stripIV();
		decrypt();

		write_ciphertext(op);	


		
	}

	/*
	StripIV takes the ciphertext and and
	takes the first 16 bytes off ciphertext
	and and then reduces the size of the ciphertext
	to account for the removal of the IV.

	*/
	protected void stripIV(){
		


		iv = new byte[16];
		byte[] properCiphertext = new byte[ciphertext.length-16];
		for(int i=0;i<ciphertext.length;i++){
			if(i < 16){
				iv[i] = ciphertext[i];
			}else{
				properCiphertext[i-16] = ciphertext[i];
			}
		}
		ciphertext = properCiphertext;		

	}

	/*
	encyrpt divides the the problem into subsets
	and start up to four threads that then work
	on these subsets.
	*/
	protected void decrypt(){

		plaintext = new byte[ciphertext.length];
		int threads;

		int blocks = (int)Math.ceil((double)plaintext.length/16);

		if(blocks <= 4){
			threads = blocks;
		}else{
			threads = 4;
		}

		double blockperthread = (double)blocks/(double)threads;
		int bptfloor = (int)blockperthread;

		/*
		System.out.printf("DEBUG INFO%n%n");

		System.out.printf("Thread Num: %d%n",threads);
		System.out.printf("Block Num: %d%n",blocks);
		System.out.printf("Blocks Per Thread: %f%n",blockperthread);
		System.out.printf("Blocks Per Thread Floor: %d%n",bptfloor);
		*/

		int[] startingIndex = new int[threads];
		int[] offset = new int[threads];

		double diffvalue = blockperthread - bptfloor;


		/*
		If there is no fractional component of number of blocks per thread
		then then each thread will have the same number of threads. If there
		is a fractional component then the threads have diffrent values but
		also that we are creating 4 threads as some remainder exists, and 
		it will be distrbuited as evenly as possiable, i.e. a thread will
		only have one more than the floor of the blocks per thread.
		*/

		if(diffvalue == 0){
			
			for(int i=0; i < threads;i++){
				startingIndex[i] = bptfloor*i;
			}
			for(int i=0; i< threads; i++){
				offset[i] = bptfloor;
			}

		}else if(diffvalue == 0.25){

			startingIndex[0] = 0;
			startingIndex[1] = bptfloor+1;
			startingIndex[2] = bptfloor*2+1;
			startingIndex[3] = bptfloor*3+1;

			offset[0] = bptfloor + 1;
			offset[1] = bptfloor;
			offset[2] = bptfloor;
			offset[3] = bptfloor;

		}else if(diffvalue == 0.5){

			startingIndex[0] = 0;
			startingIndex[1] = bptfloor+1;
			startingIndex[2] = bptfloor*2+2;
			startingIndex[3] = bptfloor*3+2;

			offset[0] = bptfloor + 1;
			offset[1] = bptfloor + 1;
			offset[2] = bptfloor;
			offset[3] = bptfloor;

		}else if(diffvalue == 0.75){
			startingIndex[0] = 0;
			startingIndex[1] = bptfloor+1;
			startingIndex[2] = bptfloor*2+2;
			startingIndex[3] = bptfloor*3+3;

			offset[0] = bptfloor + 1;
			offset[1] = bptfloor + 1;
			offset[2] = bptfloor + 1;
			offset[3] = bptfloor;
		}
		ctr_thread[] thread_array = new ctr_thread[threads];


		for(int i=0; i < threads;i++){

			thread_array[i] = new ctr_thread(startingIndex[i], offset[i] ,iv, ciphertext, plaintext, key, Cipher.ENCRYPT_MODE,i);
			thread_array[i].start();

		}

		for(int i=0; i < threads;i++){
			try{
				thread_array[i].join();
			}catch(Exception e){

			}

		}

	}

	/*
	writes the ciphertext block to the output file.
	*/

	protected void write_ciphertext(OptionParser op){

		try{
			FileOutputStream out = new FileOutputStream(op.outputFileName);
			out.write(plaintext);
			out.close();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}
	}



	public static void main(String[] args) {
		ctr_decrypt x = new ctr_decrypt(args);

		/*
		System.out.printf("Ciphertext: ");

		for(int i=0;i<x.ciphertext.length;i++){
			System.out.printf("%02x",x.ciphertext[i]);
		}

		System.out.printf("%n");
		*/

	}


}
