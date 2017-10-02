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

ctr_encrypt.java take the file information
from OptionParser and ReadPad and then
encrypts a message, the message is then
written to the output file specificed.

*/


public class ctr_encrypt{
	

	public byte[] plaintext;
	public byte[] key;
	public byte[] iv;
	public byte[] ciphertext;


	/*
	The constructor will use helper classes to
	parser the command line and read files, and
	then if an IV is not present it will create
	an IV.
	*/

	ctr_encrypt(String args[]){
		OptionParser op = new OptionParser(args, "ctr_enc");
		ReadPad rp = new ReadPad(op, false);

		plaintext = rp.input;
		key = rp.key;
		iv = rp.iv;

		if(iv == null){
			iv = generateIV();
		}

		encrypt();

		write_ciphertext(op);	


		
	}


	/*
	generateIV will if an IV has not
	been provided generate the pseudo-random
	IV for the encyption.	
	*/
	protected byte[] generateIV(){
		SecureRandom srng = new SecureRandom();
		byte[] randIV = new byte[16];

		srng.nextBytes(randIV);
		return randIV;

	}

	/*
	encyrpt divides the the problem into subsets
	and start up to four threads that then work
	on these subsets.
	*/
	protected void encrypt(){

		ciphertext = new byte[plaintext.length];
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

		/*
		each thread is created and started, and then after their operations
		are complete then they are joined and the ciphertext is writen to the
		output file.
		*/
		for(int i=0; i < threads;i++){

			thread_array[i] = new ctr_thread(startingIndex[i], offset[i] ,iv, plaintext, ciphertext, key, Cipher.ENCRYPT_MODE,i);
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
			out.write(iv);
			out.write(ciphertext);
			out.close();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}
	}



	public static void main(String[] args) {
		ctr_encrypt x = new ctr_encrypt(args);

		/*
		System.out.printf("Ciphertext: ");

		for(int i=0;i<x.ciphertext.length;i++){
			System.out.printf("%02x",x.ciphertext[i]);
		}

		System.out.printf("%n");
		*/

	}


}
