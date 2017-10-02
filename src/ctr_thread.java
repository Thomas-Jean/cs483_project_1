package src;

import java.math.BigInteger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class ctr_thread extends Thread {

	BigInteger bigIV;
	int start;
	int offset;
	byte [] iv;
	byte [] input;
	byte [] output;
	byte [] key;
	int mode;
	int id;

	public ctr_thread(int st, int off, byte[] iv_input, byte[] in, byte[] out, byte[] key_input, int m, int idi){

		/*
		convernts the iv from a byte array to a Big Integer
		to make incrementing the counter easier.
		*/
		bigIV = new BigInteger(iv_input);

		bigIV = bigIV.add(BigInteger.valueOf(start));

		start = st;
		offset = off;
		input = in;
		output = out;
		iv = iv_input;
		key = key_input;
		mode = m;
		id = idi;


	}

	public void run(){

		try{
			/*
			The AES ECB block cipher is initalized and then
			array are intializied to be block spaces for the
			input text and the pad created by the AES Cipher.
			*/
			SecretKeySpec sks = new SecretKeySpec(key,"AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(mode, sks);

			byte[] pad = new byte[16];
			byte[] workingBlock = new byte[16];


			for(int i=start; i<(start+offset);i++){
				bigIV = bigIV.add(BigInteger.ONE);
				int ret = cipher.update(bigIV.toByteArray(),0,16,pad);

				/*
				a block of input text is put into the working block
				and if it is the last block of the input text then,
	 			since CTR doesn't need to be padded then it may
	 			be shorter than the pad and will be scaled down
	 			to the proper size, and then XOR'ed with the pad.
				*/
				int endTest = 0;
				for(int j=(i)*16; (j < (i*16)+16) && j < input.length;j++){
					workingBlock[endTest] = input[j];
					endTest++;
				}


				if(endTest != 16){

					byte [] newWorkingBlock = new byte[endTest];
					for(int k=0;k<endTest;k++){
						newWorkingBlock[k] = workingBlock[k];
					}
					workingBlock = newWorkingBlock;
				}
				byte [] outputBlock = blockXOR(workingBlock,pad);


				/*
				The XOR'ed block is then written to the output space, if
				this will either be the ciphertext for encrypt mode or 
				the plaintext when in decrypt mode.
				*/
				
				int m =0;
				for(int n=(i)*16; (n < (i*16)+16) && (n < input.length);n++){
					output[n] = outputBlock[m];
					m++;
				}
				


			}



 		}catch (Exception e){
 			System.out.printf("Thread %d: %s%n",id,e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}


	}


	byte[] blockXOR(byte[] text, byte[] pad){

		byte[] ret;

		ret = new byte[text.length];
		
		for(int i=0; i <text.length;i++){
				ret[i] = (byte)(text[i] ^ pad[i]);
		}

		return ret;


	}

	byte[] blockXOR(byte[] text,int text_start,int text_offset, byte[] pad, int pad_start){

		byte[] ret;

		ret = new byte[text_offset];
		
		for(int i=0; i <text_offset;i++){
				ret[i] = (byte)(text[text_start+i] ^ pad[pad_start+i]);
		}

		return ret;


	}
	
}