import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import io.*.bit.util.BitShow;
import io.*.bit.util.BitUtils;

public class BitStream {

	private byte[] buffer;
	private int currentWriteBit = 0;
	private int currentWriteIndex = 0;
	private int currentReadBit = 0;
	private int currentReadIndex = 0;
	
	public BitStream() {
		this.buffer = new byte[BitConstants.DEFAULT_BYTE_BUFFER_SIZE];
	}
	
	public BitStream(byte[] byteArray) {
		this.buffer = byteArray;
		currentWriteIndex = byteArray.length;
		
		currentReadBit = 0;
		currentReadIndex = 0;
	}
	
	
	public void move(int moveBits) {
		this.buffer = BitUtils.shiftRight(this.buffer, moveBits);
	}
	
	public int getBitsUsed() {
		return (this.currentWriteIndex * 8) + this.currentWriteBit;
	}
	
	public void setReadPointer(int pointer) {
		this.currentReadIndex = pointer / 8;
		this.currentReadBit = pointer % 8;
	}
	
	public void setWritePointer(int pointer) {
		this.currentWriteIndex = pointer / 8;
		this.currentWriteBit = pointer % 8;
	}
	
	private void growCapacity(int bitNeed) {
		int cleanBits = 8 - this.currentWriteBit; //8 - 5
		bitNeed = bitNeed - cleanBits;
		
		if(bitNeed > 0 && this.currentWriteIndex + (bitNeed / 8) >= this.buffer.length) {
			this.buffer = Arrays.copyOf(this.buffer, this.buffer.length + BitConstants.ALLOCATE_BYTE_BUFFER_SIZE);
		}
	}
	
	private byte[] read(int count) {
		byte[] bits = new byte[Math.max(1, count / 8)];
		
		int counter = count;
		
		int writeIndex = 0;
		int writeBit = 0;
		
		while(counter > 0) {
			if(this.currentReadBit > 7) {
				this.currentReadBit = 0;
				this.currentReadIndex += 1;
			}
			if(writeBit > 7) {
				writeBit = 0;
				writeIndex += 1;
			}
			bits[writeIndex] = BitUtils.setBit(bits[writeIndex], writeBit, 
					BitUtils.readBit(this.buffer[this.currentReadIndex], this.currentReadBit));
			writeBit += 1;
			this.currentReadBit += 1;
			counter -= 1;
		}
		return bits;
	}
	
	private void write(byte[] bits, int count) {
		int counter = count;
		
		int readIndex = 0;
		int readBit = 0;
		
		this.growCapacity(count);
		
		while(counter > 0) {
			if(this.currentWriteBit > 7) {
				this.currentWriteBit = 0;
				this.currentWriteIndex += 1;
			}
			if(readBit > 7) {
				readBit = 0;
				readIndex += 1;
			}
			this.buffer[this.currentWriteIndex] = BitUtils.setBit(this.buffer[this.currentWriteIndex], this.currentWriteBit, 
					BitUtils.readBit(bits[readIndex], readBit));
			readBit += 1;
			this.currentWriteBit += 1;
			counter -= 1;
		}
	}
	
	public byte[] getBuffer() {
		return this.buffer;
	}
	
	public void writeString(String string) {
		for(byte b : string.getBytes()) {
			this.writeByte(b);
		}
	}
	
	public void writeInt(int i) {
		this.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(i).array(), Integer.SIZE);
	}
	
	public void writeByte(byte b) {
		this.write(new byte[]{b}, 8);
	}
	
	public void writeBoolean(boolean bool) {
		if(bool) {
			this.write(new byte[] {(byte) 0xFF}, 1);
		}else {
			this.write(new byte[] {(byte) 0x0}, 1);
		}
	}
	
	public boolean readBoolean() {
		return BitUtils.readBit(this.read(1)[0], 0);
	}
	
	public byte readByte() {
		return this.read(8)[0];
	}
	
	public int readInt() {
		return ByteBuffer.wrap(this.read(Integer.SIZE)).getInt();
	}
	
	public String readString(int length) {
		byte[] alloc = new byte[length];
		for(int i = 0; i < length; i++) {
			alloc[i] = readByte();
		}
		return new String(alloc);
	}
	
	@Override
	public String toString() {
		return "BitStream[writeIndex = "+ this.currentWriteIndex + " writeBit = " + this.currentWriteBit + " readIndex = "
	+ this.currentReadIndex + " readBit = " + this.currentReadBit + "]";
	}
	
}
